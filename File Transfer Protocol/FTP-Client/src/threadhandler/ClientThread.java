package threadhandler;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
//importing other classes
import gui.MainWindow;
import ftp.Host;
import ftp.ClientMain;
import client.Client;
import thread.UploadThread;
import thread.DownloadThread;
/**
 * Created by Elakiya on 7/4/2017.
 */
public class ClientThread implements Runnable{
    private Client client;
    private String host;
    private int port;
    private Scanner scanner;
    private InputStreamReader inputReader;
    BufferedReader readBuffer;
    private List<String> input;
    private Socket socket;
    private Path serverPath, userPath;
    private String clientDir;
    private String username;
    private MainWindow mainwindow;
    private DataOutputStream dataoutput;
    private DataInputStream inputDataStream;
    private OutputStream outputDataStream;
    //constructor intialization
    public ClientThread(Client client1, String host, int port, String clientDir, String username,
                        MainWindow mainWindow) throws Exception {
        this.client = client1;
        this.host = host;
        this.port = port;
        this.clientDir = clientDir;
        this.username = username;
        this.mainwindow = mainWindow;
        InetAddress hostAddress = InetAddress.getByName(host);
        socket = new Socket();
        socket.connect(new InetSocketAddress(hostAddress.getHostAddress(), port), 1000);
        inputReader = new InputStreamReader(socket.getInputStream());
        readBuffer = new BufferedReader(inputReader);
        outputDataStream = socket.getOutputStream();
        dataoutput = new DataOutputStream(outputDataStream);
        inputDataStream = new DataInputStream(socket.getInputStream());
        dataoutput.writeBytes("pwd" + "\n");
        String line;
        if (!(line = readBuffer.readLine()).equals("")) {
            serverPath = Paths.get(line);
        }
        userPath = Paths.get(clientDir);
        System.out.println("Connected to: " + hostAddress);
    }

    //Get the client directory
    public String getClientDir() { return clientDir;}

   //set the input
    public void setInput(List<String> input)
    {
        this.input = input;
    }

   //set the path
    public void setPath(Path path) throws Exception {
        dataoutput.writeBytes("setpath " + path.toString() + "\n");
    }

    //Get the input value
    public List<String> getInput() {
        return input;
    }


    @Override
    public void run() {
        try {
            String ftpPath = Host.ServerPath() + File.separator + "ftp";
            Path path = Paths.get(ftpPath + File.separator + username);
            setPath(path);
            scanner = new Scanner(System.in);
            String command = "";
            do {
                System.out.print("FTP$");
                command = scanner.nextLine();
                command = command.trim();

                input = new ArrayList<String>();
                Scanner enteredInput = new Scanner(command);

                if (enteredInput.hasNext()) {
                    input.add(enteredInput.next());
                }

                if (enteredInput.hasNext())
                    input.add(command.substring(input.get(0).length()).trim());
                enteredInput.close();

                if (input.isEmpty())
                    continue;

                switch (input.get(0)) {
                    case "user":
                        user();
                        break;
                    case "pasv":
                        pasv();
                        break;
                    case "stor":
                        upload();
                        break;
                    case "retr":
                        download();
                        break;
                    case "mode":
                        mode();
                        break;
                    case "type":
                        type();
                        break;
                    case "list":
                        list();
                        break;
                    case "quit":
                        quit();
                        break;
                    case "pwd":
                        pwd();
                        break;
                    case "delete":
                        delete();
                        break;
                    default:
                        System.out.println("Invalid command..Please enter a correct one");
                }
            } while (!command.equalsIgnoreCase("quit"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Mode method
    private void mode() throws Exception {
        dataoutput.writeUTF("mode");
        System.out.println("The MODE message from server is " + readBuffer.readLine());
    }

    //type method
    private void type() throws Exception {
        dataoutput.writeUTF("type");
        System.out.println("The TYPE message from server is " + readBuffer.readLine());
    }

    //user method
    private void user() throws Exception {
        dataoutput.writeUTF("user " + username);
    }

    //pasv command method
    private void pasv() throws Exception {
        dataoutput.writeUTF("pasv");
        int data_port = Integer.parseInt(readBuffer.readLine());
        System.out.println("data port is " + data_port);
    }

    //pwd command
    public void pwd() throws Exception {
        if (input.size() != 1) {
            invalid();
            return;
        }

        dataoutput.writeBytes("pwd" + "\n");

        String line;
        String receivedText = readBuffer.readLine();
        if (!(line = receivedText).equals("")) {
            serverPath = Paths.get(line);
        }

        System.out.println(receivedText);
    }

    //download method(RETR command)
    public void download() throws Exception {
        dataoutput.writeBytes("pwd" + "\n");

        String line;
        String receivedText = readBuffer.readLine();
        if (!(line = receivedText).equals("")) {
            serverPath = Paths.get(line);
        }
        ;
        Thread downloadThread = new Thread(
                new DownloadThread(client, host, port, input, serverPath, userPath, mainwindow));
        downloadThread.start();
    }

    //list method
    public List<String> list() throws Exception {
        List<String> files = new ArrayList<>();
        if (input.size() != 1) {
            invalid();
            return files;
        }

        dataoutput.writeBytes("list" + "\n");

        String line;
        while (!(line = readBuffer.readLine()).equals("")) {
            files.add(line);
            System.out.println(line);
        }
        return files;
    }

    //upload function(STOR)
    public void upload() throws Exception {
        dataoutput.writeBytes("pwd" + "\n");

        String line;
        String receivedText = readBuffer.readLine();
        if (!(line = receivedText).equals("")) {
            serverPath = Paths.get(line);
        }
        ;
        (new Thread(new UploadThread(client, host, port, input, serverPath, clientDir, username, mainwindow))).start();
    }



    //Delete command
    public void delete() throws Exception {

        if (input.size() != 2) {
            invalid();
            return;
        }
        dataoutput.writeBytes("delete " + input.get(1) + "\n");

        String delete_line;
        while (!(delete_line = readBuffer.readLine()).equals(""))
            System.out.println(delete_line);
    }

    //quit command
    private void quit() throws Exception {
        if (input.size() != 1) {
            invalid();
            return;
        }

        if (!client.quit()) {
            System.out.println("Transferring data..Please after sometime..");
            return;
        }

        dataoutput.writeBytes("quit" + "\n");
    }
    //invalid args
    public void invalid() {
        System.out.println("Invalid Arguments");
    }

}
