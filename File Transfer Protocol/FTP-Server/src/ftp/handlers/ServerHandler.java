package ftp.handlers;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ftp.FTPServer.FTPServer;
/**
 * Created by Elakiya on 7/4/2017.
 */

public class ServerHandler implements Runnable {
    private FTPServer FTPserver;
    private Socket serverSocket;
    private static Path serverPath;
    private List<String> clientInput;
    private BufferedReader commandBuffer;
    private DataInputStream serverDataInputStream;
    private DataOutputStream serverDataOutputStream;

    /**
     * constructor to initialize the comm handler
     */
    public ServerHandler(FTPServer server, Socket socket) throws Exception {
        this.FTPserver = server;
        this.serverSocket = socket;
        InputStreamReader commandsReader = new InputStreamReader(socket.getInputStream());
        this.commandBuffer = new BufferedReader(commandsReader);
        this.serverDataInputStream = new DataInputStream(socket.getInputStream());
        OutputStream dataOutputStream = socket.getOutputStream();
        this.serverDataOutputStream = new DataOutputStream(dataOutputStream);
    }

    /**
     * PWD command
     * send the server path
     */
    public void pwd() throws Exception {
        this.serverDataOutputStream.writeBytes(serverPath + "\n");
    }

    /**
     * RETR command
     * Process the download command.
     */
    public void download() throws Exception {

        // if the file doesn't exit, then return the message 'not exists' to the client.
        if (Files.notExists(serverPath.resolve(this.clientInput.get(1)))) {
            System.out.println("file not exits");
            this.serverDataOutputStream
                    .writeBytes("retr " + serverPath.resolve(this.clientInput.get(1)).getFileName() + " : does not exist" + "\n");
            return;
        }

        // the the path send to the server is a directory, then return the message 'is a directory' to the client.
        if (Files.isDirectory(serverPath.resolve(this.clientInput.get(1)))) {
            System.out.println("is directory");
            this.serverDataOutputStream
                    .writeBytes("retr " + serverPath.resolve(this.clientInput.get(1)).getFileName() + " : is a directory" + "\n");
            return;
        }

        // Check the lock id.
        int lockID = this.FTPserver.downloadStart(serverPath.resolve(this.clientInput.get(1)));
        if (lockID == -1) {
            System.out.println("Invalid Lock ID.");
            this.serverDataOutputStream
                    .writeBytes("retr " + serverPath.resolve(this.clientInput.get(1)).getFileName() + " : Invalid Lock ID." + "\n");
            return;
        }

        this.serverDataOutputStream.writeBytes("\n");
        this.serverDataOutputStream.writeBytes(lockID + "\n");

        Thread.sleep(100);

        // Check whether this thread is terminated.
        if (this.FTPserver.terminateDOWNLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
            System.out.println("Terminated the Download Stream.");
            quit();
            return;
        }

        byte[] bufferWindow = new byte[1000]; // download stream window

        // start download.
        try {
            File downloadFile = new File(serverPath.resolve(this.clientInput.get(1)).toString());
            long fileSize = downloadFile.length();
            byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileSize).array();
            this.serverDataOutputStream.write(fileSizeBytes, 0, 8);

            // check the download status.
            if (this.FTPserver.terminateDOWNLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
                System.out.println("The Download Thread is terminated.");
                quit();
                return;
            }

            BufferedInputStream bufferedDownloadStream = new BufferedInputStream(new FileInputStream(downloadFile));
            int count = 0;
            while ((count = bufferedDownloadStream.read(bufferWindow)) > 0) {
                if (this.FTPserver.terminateDOWNLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
                    System.out.println("The Download Thread is terminated.");
                    bufferedDownloadStream.close();
                    quit();
                    return;
                }
                this.serverDataOutputStream.write(bufferWindow, 0, count);
            }
            bufferedDownloadStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.FTPserver.downloadEnd(serverPath.resolve(this.clientInput.get(1)), lockID);

    }

    /**
     * STOR command
     *
     * @throws Exception
     */
    public void upload() throws Exception {
        int lockID = this.FTPserver.uploadID(serverPath.resolve(this.clientInput.get(1)));

        this.serverDataOutputStream.writeBytes(lockID + "\n");

        while (!this.FTPserver.uploadStart(serverPath.resolve(this.clientInput.get(1)), lockID)) {
            Thread.sleep(10);
        }

        if (this.FTPserver.terminateUPLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
            System.out.println("The Upload Thread is terminated.");
            quit();
            return;
        }

        this.serverDataOutputStream.writeBytes("\n");

        if (this.FTPserver.terminateUPLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
            System.out.println("The Upload Thread is terminated.");
            quit();
            return;
        }

        byte[] fileSizeBuffer = new byte[8];
        this.serverDataInputStream.read(fileSizeBuffer);
        ByteArrayInputStream bis = new ByteArrayInputStream(fileSizeBuffer);
        DataInputStream dis = new DataInputStream(bis);
        long fileSize = dis.readLong();

        if (this.FTPserver.terminateUPLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
            System.out.println("The Upload Thread is terminated.");
            quit();
            return;
        }

        FileOutputStream fileOutputStream = new FileOutputStream(
                new File(serverPath + File.separator + this.clientInput.get(1)).toString());
        int count = 0;
        byte[] filebuffer = new byte[1000]; // reading buffer window
        long bytesReceived = 0;
        while (bytesReceived < fileSize) { // read the entire file
            if (this.FTPserver.terminateUPLOAD(serverPath.resolve(this.clientInput.get(1)), lockID)) {
                System.out.println("The Upload Thread is terminated.");
                fileOutputStream.close();
                quit();
                return;
            }
            count = this.serverDataInputStream.read(filebuffer);
            fileOutputStream.write(filebuffer, 0, count);
            bytesReceived += count;
        }
        fileOutputStream.close();

        this.FTPserver.uploadEnd(serverPath.resolve(this.clientInput.get(1)), lockID);
    }

    /**
     * LIST command
     *
     * @throws Exception
     */
    public void list() throws Exception {
        try {
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(this.serverPath);
            System.out.println("serverPath : " + this.serverPath);
            for (Path entry : dirStream)
                this.serverDataOutputStream.writeBytes(entry.getFileName() + "\n");
            this.serverDataOutputStream.writeBytes("\n");
        } catch (Exception e) {
            this.serverDataOutputStream.writeBytes("list: failed" + "\n");
            this.serverDataOutputStream.writeBytes("\n");
        }
    }

    /**
     * QUIT command
     *
     * @throws IOException
     * @throws Exception
     */
    private void quit() throws Exception {
        this.serverSocket.close();
        throw new Exception();
    }

    @Override
    public void run() {
        System.out.println("Server communication thread started : " + Thread.currentThread().getName());
        finishThread: while (true) {
            try {
                while (!commandBuffer.ready())
                    Thread.sleep(10);

                this.clientInput = new ArrayList<String>();
                String command = commandBuffer.readLine();
                Scanner enteredInput = new Scanner(command);

                if (enteredInput.hasNext()) {
                    this.clientInput.add(enteredInput.next());
                }

                if (enteredInput.hasNext())
                    this.clientInput.add(command.substring(this.clientInput.get(0).length()).trim());
                enteredInput.close();

                System.out.println("Input Command: " + this.clientInput.get(0));
                switch (this.clientInput.get(0)) {
                    case "setpath":
                        setPath();
                        break;

                    case "retr":
                        download();
                        break;

                    case "stor":
                        upload();
                        break;

                    case "pwd":
                        pwd();
                        break;

                    case "test":
                        System.out.println("printing test in server");
                        break;

                    case "list":
                        list();
                        break;

                    case "quit":
                        break finishThread;

                    case "delete":
                        delete();
                        break;

                    case "mode":
                        mode();
                        break;

                    case "type":
                        type();
                        break;

                    case "pasv":
                        pasv();
                        break;

                    case "user":
                        user(command);
                        break;

                    default:
                        System.out.println("invalid command");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * USER command
     *
     * @param command
     */
    private void user(String command) {
        String username = checkUser(command);
        listFiles(username);
    }

    /**
     * Method used to check the user
     */
    private String checkUser(String command) {
        return null;
    }

    /**
     * Method used to list the files for a username
     *
     * @param username
     */
    private void listFiles(Object username) {

    }

    /**
     * PASV command
     *
     * @throws IOException
     */
    private void pasv() throws IOException {
        int data_port = generateDataPort();
        System.out.println("The data port is " + data_port);
        this.serverDataOutputStream.writeUTF(String.valueOf(data_port));
        ServerSocket serverSocket = new ServerSocket(data_port);
        System.out.println("Server listening on port " + data_port);
        Socket requ_socket = serverSocket.accept();
    }

    /**
     * TYPE command
     *
     * @throws IOException
     */
    private void type() throws IOException {
        System.out.println("in TYPE");
        this.serverDataOutputStream.writeUTF("200 OK Message : Type is ASCII");
    }

    /**
     * MODE command
     *
     * @throws IOException
     */
    private void mode() throws IOException {
        System.out.println("in mode");
        this.serverDataOutputStream.writeUTF("200 OK Message : Mode is Stream");
    }

    /**
     * Method used to generate random data port for the PASV command
     */
    private int generateDataPort() {
        return 0;
    }

    /**
     * DELETE command
     *
     * @throws Exception
     */
    private void delete() throws Exception {
        if (!this.FTPserver.delete(this.serverPath.resolve(this.clientInput.get(1)))) {
            this.serverDataOutputStream.writeBytes("delete: FAILED '" + this.clientInput.get(1) + "': The file is locked" + "\n");
            this.serverDataOutputStream.writeBytes("\n");
            return;
        }

        try {
            boolean confirm = Files.deleteIfExists(this.serverPath.resolve(this.clientInput.get(1)));
            if (!confirm) {
                this.serverDataOutputStream.writeBytes("delete: FAILED '" + this.clientInput.get(1) + "': No such file" + "\n");
                this.serverDataOutputStream.writeBytes("\n");
            } else
                this.serverDataOutputStream.writeBytes("\n");
        } catch (DirectoryNotEmptyException enee) {
            this.serverDataOutputStream
                    .writeBytes("delete: FAILED '" + this.clientInput.get(1) + "': Directory not empty" + "\n");
            this.serverDataOutputStream.writeBytes("\n");
        } catch (Exception e) {
            this.serverDataOutputStream.writeBytes("delete: FAILED '" + this.clientInput.get(1) + "'" + "\n");
            this.serverDataOutputStream.writeBytes("\n");
        }
    }

    /**
     * Method used to set the path in the server
     */
    private void setPath() {
        this.serverPath = Paths.get(this.clientInput.get(1));
    }

}
