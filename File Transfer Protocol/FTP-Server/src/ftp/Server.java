package ftp;
/**
 * Created by Elakiya on 7/3/2017.
 */
import ftp.FTPServer.FTPServer;
import ftp.threads.ServerThread;
import ftp.threads.TelnetThread;
import ftp.threads.TerminateThread;

import java.io.IOException;
import java.net.ServerSocket;


public class Server {
    private static ServerSocket nSocket, tSocket, telnetSocket;

    /**
     * Main function to start the code
     * Running as a server.
     */
    public static void main(String[] args) {

        int nPort = 0;
        nPort = Integer.parseInt(args[0]);

        int tPort = 0;
        tPort = Integer.parseInt(args[1]);

        try {
            nSocket = new ServerSocket(nPort);
            tSocket = new ServerSocket(tPort);
            telnetSocket = new ServerSocket(23);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            FTPServer server = new FTPServer();
            (new Thread(new ServerThread(server, nSocket))).start();
            (new Thread(new TerminateThread(server, tSocket))).start();
            (new Thread(new TelnetThread(telnetSocket))).start();
            System.out.println("The server start listening at " + nPort + "...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
