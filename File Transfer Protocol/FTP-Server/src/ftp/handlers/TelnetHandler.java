package ftp.handlers;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import ftp.Host;
/**
 * Created by Elakiya on 7/3/2017.
 */
public class TelnetHandler implements Runnable{
    private DataInputStream telDataInputStream;
    private DataOutputStream telDataOutputStream;
    private String usernameLogin;
    private Socket telSocket;

    private String ftpPath = Host.getServerPath() + File.separator + "ftp";

    /**
     * Constructor to initialize the class with socket
     *
     */
    public TelnetHandler(Socket telnetSocket) throws Exception {
        this.telSocket = telnetSocket;
        telDataInputStream = new DataInputStream(telnetSocket.getInputStream());
        telDataOutputStream = new DataOutputStream(telnetSocket.getOutputStream());
    }

    public String getUsernameLogin() {
        return usernameLogin;
    }

    @Override
    public void run() {
        boolean run = true;
        while (run) {
            try {
                boolean success = false;
                String user_input_UTF = telDataInputStream.readUTF();
                System.out.println("server path is " + ftpPath);
                BufferedReader authenticationBuffer = new BufferedReader(
                        new FileReader(ftpPath + File.separator + "users.txt"));

                StringTokenizer tokens = new StringTokenizer(user_input_UTF, "_");
                tokens.nextToken();
                String userName = tokens.nextToken();
                String pwd = tokens.nextToken();
//                System.out.println("The username is " + userName + " and the password is " + pwd);

                String LoginInfo;
                while ((LoginInfo = authenticationBuffer.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(LoginInfo, " ");
                    if (userName.equalsIgnoreCase(st.nextToken()) && pwd.equalsIgnoreCase(st.nextToken())) {
                        success = true;
                        usernameLogin = userName;
                        break;
                    }
                }
                telDataOutputStream.writeUTF(String.valueOf(success));
                if (success) {
                    run = false;

                    String serverPath = Host.getServerPath() + File.separator + "ftp";
                    Path userPath = Paths.get(serverPath + File.separator + usernameLogin);

                    if (Files.notExists(userPath)) {
                        Files.createDirectories(userPath);
                    }
                    telDataOutputStream.writeUTF(userPath.toString());
                    telSocket.close();
                    authenticationBuffer.close();
                }
                authenticationBuffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
