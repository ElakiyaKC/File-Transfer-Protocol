package ftp.threads;
import java.net.ServerSocket;

import ftp.handlers.ServerHandler;
import ftp.FTPServer.FTPServer;

/**
 * Created by Elakiya on 7/3/2017.
 */
public class ServerThread implements Runnable {
    private FTPServer ftpServer;
    private ServerSocket socket;

    /**
     * Constructor to initialize the class with
     *
     * @param ftpServer
     * @param socket
     */
    public ServerThread(FTPServer ftpServer, ServerSocket socket) {
        super();
        this.ftpServer = ftpServer;
        this.socket = socket;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        while (true) {
            try {
                new Thread(new ServerHandler(ftpServer, socket.accept())).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
