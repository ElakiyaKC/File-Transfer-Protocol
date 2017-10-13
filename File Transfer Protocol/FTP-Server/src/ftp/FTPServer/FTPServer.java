package ftp.FTPServer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * Created by Elakiya on 7/3/2017.
 * This is the main function of the FTP server.
 */
public class FTPServer {

    private Map<Path, ReentrantReadWriteLock> dataThreadMap;
    private Map<Integer, Path> commandThreadMap;
    private LinkedList<Integer> writingQue;
    private Set<Integer> terminateSet;

    /**
     * Constructor to initialize the declared variables
     */
    public FTPServer() {
        dataThreadMap = new HashMap<>();
        commandThreadMap = new HashMap<>();
        writingQue = new LinkedList<>();
        terminateSet = new HashSet<>();
    }

    /**
     * Return the random generated thread id of a upload data thread.
     *
     */
    public synchronized int uploadID(Path path) {
        int threadID = 0;
        while (commandThreadMap.containsKey(threadID = generateID()))
            // generate a random ID which is not in the commandThreadMap.
            // make sure all the thread IDs are unique.
            ;
        commandThreadMap.put(threadID, path);
        writingQue.add(threadID);
        return threadID;
    }

    /**
     * When new upload data thread comes in, start a new data thread on the server side.
     *
     */
    public synchronized boolean uploadStart(Path path, int threadID) {
        if (writingQue.peek() == threadID) {
            // if the thread is the first one of the writing queue, then process
            // otherwise, the thread will wait.
            if (dataThreadMap.containsKey(path)) {
                // if the Map has the path, then try to get the lock and start to write.
                if (dataThreadMap.get(path).writeLock().tryLock()) {
                    return true;
                } else
                    return false;
            } else {
                // if the Map doesn't has the path, then put the path into the Map first
                // then try to get the lock and start to write.
                dataThreadMap.put(path, new ReentrantReadWriteLock());
                dataThreadMap.get(path).writeLock().lock();
                return true;
            }
        }
        return false;
    }

    /**
     * when a upload thread complete, then end the thread.
     */
    public synchronized void uploadEnd(Path path, int threadID) {

        try {
            // When the upload data thread complete, then release the lock.
            dataThreadMap.get(path).writeLock().unlock();

            commandThreadMap.remove(threadID);
            writingQue.poll();

            System.out.println("The thread has the read Lock count" + dataThreadMap.get(path).getReadLockCount());
            if (dataThreadMap.get(path).getReadLockCount() == 0 && !dataThreadMap.get(path).isWriteLocked())
            // Only when the count of lock and unclock are same, and the Thread is not writing,
            // The upload path can be removed from the Map.
                dataThreadMap.remove(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * When a download command comes in, start start a down load thread, and notify.
     */
    public synchronized int downloadStart(Path path) {
        int threadID = 0;

        if (dataThreadMap.containsKey(path)) {
            // when the dataThreadMap found the path, then the data thread try to enter the critical section
            // call the Lock.
            if (dataThreadMap.get(path).readLock().tryLock()) {
                while (commandThreadMap.containsKey(threadID = generateID()))
                    commandThreadMap.put(threadID, path);
                return threadID;
            } else {
                return -1;
            }
        } else {
            // if the dataThreadMap doesn't found the path, then put the path into the dataThreadMap
            // and try to enter the critical section, call the lock.
            dataThreadMap.put(path, new ReentrantReadWriteLock());
            dataThreadMap.get(path).readLock().lock();

            while (commandThreadMap.containsKey(threadID = generateID()))
                ;
            commandThreadMap.put(threadID, path);
            return threadID;
        }
    }

    /**
     * When download thread complete, then end the download thread.
     */
    public synchronized void downloadEnd(Path path, int threadID) {
        try {
            // after finish the download process, the data thread release the read lock.
            dataThreadMap.get(path).readLock().unlock();
            // The commandThreadMap remove the threadID
            commandThreadMap.remove(threadID);

            if (dataThreadMap.get(path).getReadLockCount() == 0 && !dataThreadMap.get(path).isWriteLocked()) {
                // Only when all the Lock calls released (the count of lock and unlock are same
                // Then complete the thread, and remove the task from the dataThreadMap.
                dataThreadMap.remove(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Generate a random Number.
     */
    public int generateID() {
        return new Random().nextInt(90000) + 10000;
    }

    /**
     * Add a thread ID to the terminateSet.
     */
    public synchronized void terminate(int threadID) {
        terminateSet.add(threadID);
    }

    /**
     * method used to check the terminate of download
     */
    public synchronized boolean terminateDOWNLOAD(Path path, int threadID) throws Exception {
        try {
            if (terminateSet.contains(threadID)) {
                terminateSet.remove(threadID);
                commandThreadMap.remove(threadID);
                dataThreadMap.get(path).readLock().unlock();

                if (dataThreadMap.get(path).getReadLockCount() == 0 && !dataThreadMap.get(path).isWriteLocked()) {
                    dataThreadMap.remove(path);
                }
                return true;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * method used to check for termination of upload
     */
    public synchronized boolean terminateUPLOAD(Path path, int threadID) {
        try {
            if (terminateSet.contains(threadID)) {
                terminateSet.remove(threadID);
                commandThreadMap.remove(threadID);
                dataThreadMap.get(path).writeLock().unlock();
                writingQue.poll();
                Files.deleteIfExists(path);

                if (dataThreadMap.get(path).getReadLockCount() == 0 && !dataThreadMap.get(path).isWriteLocked())
                    dataThreadMap.remove(path);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * remove files from the files map
     */
    public boolean delete(Path path) {
        return !dataThreadMap.containsKey(path);
    }
}

