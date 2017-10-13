package ftp.threads;

import java.net.ServerSocket;

import ftp.handlers.TelnetHandler;
/**
 * Created by Elakiya on 7/3/2017.
 */
public class TelnetThread implements Runnable{
    private ServerSocket telnetSocket;

    /**
     * Constructor to initialize the class with
     *
     * @param telnetSocket
     */
    public TelnetThread(ServerSocket telnetSocket) {
        super();
        this.telnetSocket = telnetSocket;
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
                (new Thread(new TelnetHandler(telnetSocket.accept()))).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
