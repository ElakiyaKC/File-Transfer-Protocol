package ftp.threads;
import java.net.ServerSocket;

import ftp.handlers.TerminateHandler;
import ftp.FTPServer.FTPServer;
/**
 * Created by Elakiya on 7/4/2017.
 */
public class TerminateThread implements Runnable{

    private FTPServer server;
    private ServerSocket serverSocket;

    /**
     * Constructor to initialize the class with
     */
    public TerminateThread(FTPServer server, ServerSocket serverSocket) {
        this.server = server;
        this.serverSocket = serverSocket;
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
                (new Thread(new TerminateHandler(server, serverSocket.accept()))).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
