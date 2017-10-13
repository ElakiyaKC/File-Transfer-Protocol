package thread;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import client.Client;
import gui.MainWindow;

/**
 * Created by Elakiya on 7/4/2017.
 */
public class UploadThread implements Runnable{
    private Client client;
    private Socket socket;
    private Path clientDirPath, serverPath;
    private List<String> inputs;
    private int terminateID;

    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    private OutputStream outputStream;
    private DataOutputStream dataOutputStream;

    private MainWindow mainwindow;
    //constructor intialization
    public UploadThread(Client client1, String hostname, int nPort, List<String> inputs, Path serverPath,
                        String clientDir, String username, MainWindow mainwindow1) throws Exception {
        this.client = client1;
        this.inputs = inputs;
        this.serverPath = serverPath;
        this.mainwindow = mainwindow1;
        InetAddress address = InetAddress.getByName(hostname);
        socket = new Socket();
        socket.connect(new InetSocketAddress(address.getHostAddress(), nPort), 1000);
        inputStreamReader = new InputStreamReader(socket.getInputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        outputStream = socket.getOutputStream();
        dataOutputStream = new DataOutputStream(outputStream);
        clientDirPath = Paths.get(clientDir);

    }

    //upload thread
    private void upload() throws Exception {
        mainwindow.startUpload(inputs.get(1));
        if (!client.transfer(serverPath.resolve(inputs.get(1)))) {
            System.out.println("Download in Progress");
            return;
        }

        if (Files.notExists(clientDirPath.resolve(inputs.get(1)))) {
            System.out.println("File not found");
        } else if (Files.isDirectory(clientDirPath.resolve(inputs.get(1)))) {
            System.out.println("Diectory cannot be uploaded");
        } else {            System.out.println("UploadHandler clientDir: " + clientDirPath.toString());
                            System.out.println("UploadHandler clientDir: " + serverPath.toString());

            dataOutputStream.writeBytes("stor " + inputs.get(1) + "\n");

            try {
                terminateID = Integer.parseInt(bufferedReader.readLine());
            } catch (Exception e) {
                e.printStackTrace();
            }

            client.intiateTransfer(serverPath.resolve(inputs.get(1)), terminateID);

            bufferedReader.readLine();

            Thread.sleep(100);

            byte[] fileBuffer = new byte[1000];
            try {
                File file = new File(clientDirPath.resolve(inputs.get(1)).toString());

                long fileSize = file.length();
                byte[] fileSizeBytes = ByteBuffer.allocate(8).putLong(fileSize).array();
                dataOutputStream.write(fileSizeBytes, 0, 8);

                Thread.sleep(100);

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                int count = 0;
                long uploaded = 0;
                while ((count = bis.read(fileBuffer)) > 0) {
                    uploaded += count;
                    dataOutputStream.write(fileBuffer, 0, count);
                    mainwindow.uploadInProgress(inputs.get(1), (int) (((float) uploaded / fileSize) * 100));
                }
                bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainwindow.finishUpload(inputs.get(1));
            client.completeTransfer(serverPath.resolve(inputs.get(1)), terminateID);
        }
    }

    @Override
    public void run() {
        try {
            upload();
            Thread.sleep(100);
        } catch (Exception e) {
            System.out.println("upload handler error");
        }
    }
}
