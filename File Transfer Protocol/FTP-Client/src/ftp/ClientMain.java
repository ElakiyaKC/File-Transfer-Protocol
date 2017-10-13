package ftp;

import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.Socket;

//import other classes
import client.Client;
import threadhandler.ClientThread;
/**
 * Created by Elakiya on 7/4/2017.
 */
public class ClientMain {
    public static String hostname;
    public static int port1, port2;
    public static String clientDir;
    private static Socket authSocket;
    private static DataInputStream authInput;
    private static DataOutputStream authOutput;
    private static Scanner scanner1;

    /**
     * Main function to run this class
     *
     * @param args
     */
    public static void main(String[] args) {

        scanner1 = new Scanner(System.in);

        System.out.println("Enter the username:");
        String username = scanner1.nextLine();

        try {
            hostname = args[0];
            InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        port1 = Integer.parseInt(args[1]);
        port2 = Integer.parseInt(args[2]);
        clientDir = args[3];

        try {
            boolean loginVal = false;
            authSocket = new Socket(hostname, 23);
            authInput = new DataInputStream(authSocket.getInputStream());
            authOutput = new DataOutputStream(authSocket.getOutputStream());

            while (loginVal == false) {
                System.out.println("Enter the pswd:");
                String pswd = scanner1.nextLine();
                String user_auth = "telnetd_" + username + "_" + pswd;
                authOutput.writeUTF(user_auth);
                String telOutput = authInput.readUTF();
                loginVal = Boolean.parseBoolean(telOutput);
                if (loginVal) {
                    System.out.println("Login successful");
                    System.out.println("Welcome"+username+" !!!");
                } else {
                    System.out.println("Username/Password is incorrect.Please try again ");
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try {
            Client client1 = new Client();
            (new Thread(new ClientThread(client1, hostname, port1, clientDir, username, null))).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
