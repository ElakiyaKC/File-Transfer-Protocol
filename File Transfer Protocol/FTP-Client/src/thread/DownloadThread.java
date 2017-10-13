package thread;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;

import client.Client;
import gui.MainWindow;
/**
 * Created by Elakiya on 7/4/2017.
 */
public class DownloadThread implements Runnable {
    private Client Client;
    private Socket socket;
    private Path path, serverPath;
    private List<String> inputs;
    private int terminateID;
    private InputStreamReader inputReader;
    private BufferedReader readerBuffer;
    private DataInputStream inputDataStream;
    private OutputStream outputStream;
    private DataOutputStream outputDataStream;
    private MainWindow mainwindow;

    //constructor intialization
    public DownloadThread(Client client1, String hostname, int nPort, List<String> inputs, Path serverPath,
                          Path path, MainWindow mainwindow) throws Exception {
        this.Client = client1;
        this.inputs = inputs;
        this.serverPath = serverPath;
        this.path = path;
        this.mainwindow = mainwindow;

        InetAddress address = InetAddress.getByName(hostname);
        socket = new Socket();
        socket.connect(new InetSocketAddress(address.getHostAddress(), nPort), 1000);

        inputReader = new InputStreamReader(socket.getInputStream());
        readerBuffer = new BufferedReader(inputReader);
        inputDataStream = new DataInputStream(socket.getInputStream());
        outputStream = socket.getOutputStream();
        outputDataStream = new DataOutputStream(outputStream);
    }

    //download operation
    public void download() throws Exception {
        mainwindow.startDownload(inputs.get(1));
        if (!Client.transfer(serverPath.resolve(inputs.get(1)))) {
            System.out.println("File transfer in progress");
            return;
        }

        outputDataStream.writeBytes("retr " + inputs.get(1) + "\n");

        String line;
        if (!(line = readerBuffer.readLine()).equals("")) {
            System.out.println(line);
            return;
        }

        try {
            terminateID = Integer.parseInt(readerBuffer.readLine());
        } catch (Exception e) {
            System.out.println("Invalid Thread ID");
        }

        Client.intiateTransfer(serverPath.resolve(inputs.get(1)), terminateID);

        if (Client.terminateDownload(path.resolve(inputs.get(1)), serverPath.resolve(inputs.get(1)), terminateID)) {
            return;
        }

        byte[] filebuffer = new byte[8];
        inputDataStream.read(filebuffer);
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(filebuffer);
        DataInputStream dis = new DataInputStream(arrayInputStream);
        long fileSize = dis.readLong();

        if (Client.terminateDownload(path.resolve(inputs.get(1)), serverPath.resolve(inputs.get(1)), terminateID)) {
            return;
        }

        FileOutputStream fileOutputStream = new FileOutputStream(new File(path + File.separator + inputs.get(1)));
        int count = 0;
        byte[] buffer = new byte[8192];
        long bytesReceived = 0;
        while (bytesReceived < fileSize) {
            if (Client.terminateDownload(path.resolve(inputs.get(1)), serverPath.resolve(inputs.get(1)), terminateID)) {
                fileOutputStream.close();
                return;
            }
            count = inputDataStream.read(buffer);
            fileOutputStream.write(buffer, 0, count);
            bytesReceived += count;
            mainwindow.downloadInProgress(inputs.get(1), (int) (((float) bytesReceived / fileSize) * 100));
        }
        mainwindow.finishDownload(inputs.get(1));
        fileOutputStream.close();

        Client.completeTransfer  (serverPath.resolve(inputs.get(1)), terminateID);
    }


    @Override
    public void run() {
        try {
            download();
            Thread.sleep(100);
            outputDataStream.writeBytes("quit" + "\n");
        } catch (Exception e) {
            System.out.println("Download Error");
        }

    }
}
