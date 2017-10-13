package ftp.handlers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ftp.FTPServer.FTPServer;
/**
 * Created by Elakiya on 7/3/2017.
 */
public class TerminateHandler implements Runnable{
    private FTPServer server;
    private Socket socket;

    /**
     * Constructor to initialize the class with
     *
     * @param server
     * @param socket
     */
    public TerminateHandler(FTPServer server, Socket socket) {
        super();
        this.server = server;
        this.socket = socket;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while (!bufferedReader.ready())
                Thread.sleep(10);

            List<String> inputs = new ArrayList<String>();
            String command = bufferedReader.readLine();
            Scanner scanner = new Scanner(command);

            if (scanner.hasNext()) {
                inputs.add(scanner.next());
            }

            if (scanner.hasNext()) {
                inputs.add(command.substring(inputs.get(0).length()).trim());
            }

            scanner.close();

            switch (inputs.get(0)) {
                case "terminate":
                    server.terminate(Integer.parseInt(inputs.get(1)));
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
