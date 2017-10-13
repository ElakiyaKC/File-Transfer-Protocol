package gui;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.net.Socket;
import java.awt.event.ActionListener;
import java.io.DataInputStream;

import ftp.Host;
/**
 * Created by Elakiya on 7/4/2017.
 */
public class ClientLogin extends JFrame{
    private static final long serialVersionUID = -4736802206170115720L;
    private JPasswordField passwordFieldPswd;
    private JTextField textFieldHost,textFieldPath,textFieldUser;

    private static Socket authSocket;
    private static DataInputStream dataInput;
    private static DataOutputStream dataOutput;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        new ClientLogin();
    }

    /**
     * Create the application.
     */
    public ClientLogin() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setTitle("Client Login");
        setBounds(180, 180, 640, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //getContentPane().setBackground(Color.GRAY);
        getContentPane().setLayout(null);

        //Labels initializations
        JLabel lbluser = new JLabel("UserName");
        lbluser.setBounds(49, 82, 74, 14);
        getContentPane().add(lbluser);

        JLabel lblpswd = new JLabel("Password");
        lblpswd.setBounds(49, 113, 74, 14);
        getContentPane().add(lblpswd);

        JLabel lblhost= new JLabel("Host Name");
        lblhost.setBounds(49, 144, 74, 14);
        getContentPane().add(lblhost);

        JLabel lblpath= new JLabel("Client Path");
        lblpath.setBounds(49, 175, 74, 14);
        getContentPane().add(lblpath);

        //Text Field Initialization
        textFieldUser = new JTextField();
        textFieldUser.setText("ruiting");
        textFieldUser.setBounds(197, 79, 227, 20);
        getContentPane().add(textFieldUser);
        textFieldUser.setColumns(10);

        passwordFieldPswd = new JPasswordField();
        passwordFieldPswd.setBounds(197, 110, 227, 20);
        getContentPane().add(passwordFieldPswd);

        textFieldHost = new JTextField();
        textFieldHost.setText(Host.HostName());
        textFieldHost.setBounds(197, 141, 227, 20);
        getContentPane().add(textFieldHost);
        textFieldHost.setColumns(10);

        textFieldPath = new JTextField();
        textFieldPath.setBounds(197, 170, 227, 20);
        getContentPane().add(textFieldPath);
        textFieldHost.setColumns(10);

        //Buttons Initialization
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        btnLogin.setBounds(100, 199, 89, 23);
        getContentPane().add(btnLogin);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        btnCancel.setBounds(280, 199, 89, 23);
        getContentPane().add(btnCancel);
        setVisible(true);
    }

    //Login Authentication
    private void login() {
        try {
            boolean login = false;
            authSocket = new Socket(Host.HostName(), 23);
            dataInput = new DataInputStream(authSocket.getInputStream());
            dataOutput = new DataOutputStream(authSocket.getOutputStream());

            String auth_string = "telnetd_" + textFieldUser.getText() + "_"
                    + new String(passwordFieldPswd.getPassword());
            dataOutput.writeUTF(auth_string);
            String authResult = dataInput.readUTF();
            login = Boolean.parseBoolean(authResult);
            if (login) {
                System.out.println("Welcome "+textFieldUser.getText()+"!!!");
                dispose();
                //String[] args = { Host.HostName(), "20", "C:\\Users\\Ruiti\\Documents", textFieldUser.getText() };
                String[] args = { Host.HostName(), "20", textFieldPath.getText(), textFieldUser.getText() };
                MainWindow.main(args);
            } else {
                JOptionPane.showMessageDialog(this, "Username/Password is incorrect.Please try again.");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
