package client;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.nio.file.Files;
import java.util.HashSet;
import java.nio.file.Path;

public class Client {
    private Map<Integer, Path> cmdChannel;
    private Set<Path> dataSet;
    private Set<Integer> terminateThread;

    public Client(){
        cmdChannel = new HashMap<Integer, Path>();
        dataSet = new HashSet<Path>();
        terminateThread = new HashSet<Integer>();
    }

    public synchronized boolean transfer(Path path) {
        return !dataSet.contains(path);
    }

    //Start the transfer process
    public synchronized void intiateTransfer(Path path, int cmdId) {
        dataSet.add(path);
        cmdChannel.put(cmdId, path);
    }

    //Finish transferring the data
    public synchronized void completeTransfer(Path path, int cmdId) {
        try {
            dataSet.remove(path);
            cmdChannel.remove(cmdId);
        } catch (Exception e) {
        }
    }

    //terminate the upload process
    public synchronized boolean terminateUpload(Path path, int cmdId) {
        try {
            if (terminateThread.contains(cmdId)) {
                cmdChannel.remove(cmdId);
                dataSet.remove(path);
                terminateThread.remove(cmdId);
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    //terminate the download process
    public synchronized boolean terminateDownload(Path path, Path serverPath, int cmdId) {
        try {
            if (terminateThread.contains(cmdId)) {
                cmdChannel.remove(cmdId);
                dataSet.remove(serverPath);
                terminateThread.remove(cmdId);
                Files.deleteIfExists(path);
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }

    //add the current thread to terminate
    public boolean terminateAdd(int trmtId) {
        if (cmdChannel.containsKey(trmtId)) {
            terminateThread.add(trmtId);
            return true;
        } else
            return false;
    }

    //quit operation
    public boolean quit() {
        return dataSet.isEmpty();
    }
}
