package gui;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;

import ftp.Host;
import client.Client;
import threadhandler.ClientThread;
/**
 * Created by Elakiya on 7/4/2017.
 */
public class MainWindow implements DownloadProgress, UploadProgress{
    private JFrame mainwindow;
    private JSplitPane splitPane;
    private JScrollPane ClientSide, ServerSide;
    private List<JProgressBar> progressBars;
    private HashMap<JProgressBar, Boolean> progressbarhm;
    private HashMap<String, JProgressBar> progressbarfiles;
    private JList clientList, serverList;
    private DefaultListModel ClientFileModel,ServerFileModel;
    private JPanel clientPanel, serverPanel;
    private JButton btnUpload, btnDownload, btnClientr, btnServerr, btnDelete;
    private JProgressBar progressBar, progressBar_1, progressBar_2, progressBar_3, progressBar_4, progressBar_5;
    protected String uploadfile, downloadfile;
    protected static String[] inputs;
    private ClientThread ClientThreadCopy;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        inputs = args;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow(args);
                    window.mainwindow.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        for (String item: args
                ) {
            System.out.print(item + " ");
        }
        System.out.println();
    }


    public MainWindow(String[] inputs) throws Exception {
        initialize();
        mainwindow.setLocationRelativeTo(null);
    }
    //Initialize the application
    private void initialize() throws Exception {

        progressbarfiles = new HashMap<>();
        progressbarhm = new HashMap<>();
        mainwindow = new JFrame();
        mainwindow.setTitle("CSE6324_Team7_FTP");
        mainwindow.setBounds(100, 100, 700, 800);
        mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainwindow.getContentPane().setLayout(null);
        initializePanelsandButtons();
        initializeProgressBars();
        buttonEvents();
        runApp();
        getClientFilesModel();
        getServerFilesModel();
    }

    //run app defn
    private void runApp() throws Exception {
        Client client1 = new Client();
        ClientThreadCopy = new ClientThread(client1, inputs[0],Integer.parseInt(inputs[1]), inputs[2], inputs[3], this);
        (new Thread(ClientThreadCopy)).start();
    }


    //Initialize the progressbars
    private void initializeProgressBars() {
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setBounds(10, 573, 600, 14);
        mainwindow.getContentPane().add(progressBar);

        progressBar_1 = new JProgressBar();
        progressBar_1.setStringPainted(true);
        progressBar_1.setBounds(10, 599, 600, 14);
        progressBar_1.setVisible(false);
        mainwindow.getContentPane().add(progressBar_1);

        progressBar_2 = new JProgressBar();
        progressBar_2.setStringPainted(true);
        progressBar_2.setBounds(10, 625, 600, 14);
        progressBar_2.setVisible(false);
        mainwindow.getContentPane().add(progressBar_2);

        progressBar_3 = new JProgressBar();
        progressBar_3.setStringPainted(true);
        progressBar_3.setBounds(6, 652, 600, 14);
        progressBar_3.setVisible(false);
        mainwindow.getContentPane().add(progressBar_3);

        progressBar_4 = new JProgressBar();
        progressBar_4.setStringPainted(true);
        progressBar_4.setBounds(10, 678, 600, 14);
        progressBar_4.setVisible(false);
        mainwindow.getContentPane().add(progressBar_4);

        progressBar_5 = new JProgressBar();
        progressBar_5.setStringPainted(true);
        progressBar_5.setVisible(false);
        progressBar_5.setBounds(6, 702, 600, 14);
        mainwindow.getContentPane().add(progressBar_5);

        progressbarhm.put(progressBar, Boolean.FALSE);
        progressbarhm.put(progressBar_1, Boolean.FALSE);
        progressbarhm.put(progressBar_2, Boolean.FALSE);
        progressbarhm.put(progressBar_3, Boolean.FALSE);
        progressbarhm.put(progressBar_4, Boolean.FALSE);
        progressbarhm.put(progressBar_5, Boolean.FALSE);

        progressBars = new ArrayList<>();
        progressBars.add(progressBar);
        progressBars.add(progressBar_1);
        progressBars.add(progressBar_2);
        progressBars.add(progressBar_3);
        progressBars.add(progressBar_4);
        progressBars.add(progressBar_5);
    }

    //initialize the panels and the buttons
    private void initializePanelsandButtons() {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBounds(10, 11, 700, 500);
        splitPane.setDividerLocation(0.5);
        JLabel lblclient = new JLabel("Client Side");
        lblclient.setBounds(49, 82, 74, 14);
        mainwindow.getContentPane().add(lblclient);
        JLabel lblserver = new JLabel("Server Side");
        lblserver.setBounds(49, 282, 100, 64);
        mainwindow.getContentPane().add(lblserver);
        //initialize the buttons
        btnUpload = new JButton("Upload");
        btnUpload.setBounds(500, 80, 100, 36);
        mainwindow.getContentPane().add(btnUpload);

        btnClientr = new JButton("Refresh");
        btnClientr.setBounds(500, 120, 100, 36);
        mainwindow.getContentPane().add(btnClientr);

        btnDownload = new JButton("Download");
        btnDownload.setBounds(500, 300, 100, 36);
        mainwindow.getContentPane().add(btnDownload);

        btnDelete = new JButton("Delete");
        btnDelete.setBounds(500, 340, 100, 36);
        mainwindow.getContentPane().add(btnDelete);


        btnServerr = new JButton("Refresh");
        btnServerr.setBounds(500, 380, 100, 36);
        mainwindow.getContentPane().add(btnServerr);
        mainwindow.getContentPane().add(splitPane);



        ClientSide = new JScrollPane();
        splitPane.setLeftComponent(ClientSide);

        clientPanel = new JPanel();

        ClientSide.setViewportView(clientPanel);

        ServerSide = new JScrollPane();
        splitPane.setRightComponent(ServerSide);

        serverPanel = new JPanel();

        ServerSide.setViewportView(serverPanel);
    }

    //adding events to the buttons
    private void buttonEvents() {
        //events to the upload button
        btnUpload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    upload(uploadfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //refresh button on client side event
        btnClientr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            updateLocalFilesModel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mainwindow.revalidate();
                        mainwindow.repaint();
                    }
                }).start();
            }
        });
        //Download button event on the server side
        btnDownload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    download(downloadfile);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //Delete button event on the server side
        btnDelete.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    deleteServerFile(downloadfile);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //refresh button on server side event
        btnServerr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            updateServerFilesModel();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    //update the progressbar while transfering data
    void updateProgress(final JProgressBar jProgressBar, final int newValue) {

        jProgressBar.setValue(newValue);
    }

    //set the value on the progressbar
    public void setValue(final JProgressBar bar, final int j) {
        updateProgress(bar, j);
        mainwindow.revalidate();
        ServerSide.repaint();
    }

    //display the progressbar
    private JProgressBar findProgressBar() {
        for (Iterator<JProgressBar> iterator = progressBars.iterator(); iterator.hasNext();) {
            JProgressBar jProgressBar = (JProgressBar) iterator.next();
            if (!progressbarhm.get(jProgressBar)) {
                progressbarhm.put(jProgressBar, Boolean.TRUE);
                return jProgressBar;
            }
        }
        return null;
    }

    //read the files on the client side
    private void getClientFilesModel() {

        ArrayList<String> file_list_names = getLocalFiles();
        ClientFileModel = new DefaultListModel();
        for (Iterator iterator = file_list_names.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            ClientFileModel.addElement(string);
        }
        clientList = new JList<>(ClientFileModel);
        clientList.setBounds(0, 0, 202, 114);
        clientList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                uploadfile = (String) clientList.getSelectedValue();
            }
        });
        clientPanel.add(clientList);
        clientList.setVisible(true);
    }
   //display the client files
    private ArrayList<String> getLocalFiles() {
        ArrayList<String> file_list_names = new ArrayList<String>();
        File folder = new File(ClientThreadCopy.getClientDir());
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                file_list_names.add(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
            }
        }
        return file_list_names;
    }

    //update the files on client file list on click on refresh button
    private void updateLocalFilesModel() {
        ArrayList<String> file_list_names = getLocalFiles();
        ClientFileModel.removeAllElements();
        for (Iterator iterator = file_list_names.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            ClientFileModel.addElement(string);
        }

        mainwindow.revalidate();
        mainwindow.repaint();
    }

   //get the server file list
    private void getServerFilesModel() throws Exception {
        ClientThreadCopy.setInput(makeInput(new String[] { "list" }));
        List<String> file_list_names = ClientThreadCopy.list();
        ServerFileModel = new DefaultListModel();
        for (Iterator iterator = file_list_names.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            ServerFileModel.addElement(string);
        }
        serverList = new JList<>(ServerFileModel);
        serverList.setBounds(0, 0, 202, 114);
        serverList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                downloadfile = (String) serverList.getSelectedValue();
            }
        });
        serverPanel.add(serverList);
        serverList.setVisible(true);
    }

   //update the server file list on click of refresh button
    private void updateServerFilesModel() throws Exception {
        setPath();
        ClientThreadCopy.setInput(makeInput(new String[] { "list" }));
        List<String> file_list_names = ClientThreadCopy.list();
        ServerFileModel.removeAllElements();
        for (Iterator iterator = file_list_names.iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            ServerFileModel.addElement(string);
        }

        mainwindow.revalidate();
        mainwindow.repaint();
    }

  //set the server path
    private void setPath() throws Exception {
        String ftpPath = Host.ServerPath() + File.separator + "ftp";
        Path path = Paths.get(ftpPath + File.separator + inputs[3]);
        ClientThreadCopy.setPath(path);
    }

    //set the input parameters on click og upload,download and delet button
    private List<String> makeInput(String[] input) {
        List<String> inputs = new ArrayList<String>();
        for (int i = 0; i < input.length; i++) {
            inputs.add(input[i]);
        }
        return inputs;
    }

    //delete a file on the server
    private void deleteServerFile(String filename) throws Exception {
        ClientThreadCopy.setInput(makeInput(new String[] { "delete", filename }));
        ClientThreadCopy.delete();
        new Thread(new Runnable() {
            public void run() {
                try {
                    updateServerFilesModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

   //upload process
    private void upload(String filename) throws Exception {
        setPath();
        ClientThreadCopy.setInput(makeInput(new String[] { "stor", filename }));
        ClientThreadCopy.upload();
        new Thread(new Runnable() {
            public void run() {
                try {
                    updateServerFilesModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //download process
    private void download(String filename) throws Exception {
        setPath();
        ClientThreadCopy.setInput(makeInput(new String[] { "retr", filename }));
        ClientThreadCopy.download();
        updateLocalFilesModel();
    }

    @Override
    public void startUpload(String filename) {
        JProgressBar findProgressBar = findProgressBar();
        progressbarfiles.put(filename, findProgressBar);
    }

    @Override
    public void uploadInProgress(String filename, int progress) {
        JProgressBar jProgressBar = progressbarfiles.get(filename);
        jProgressBar.setVisible(true);
        setValue(jProgressBar, progress);
    }

    @Override
    public void finishUpload(String filename) {
        JProgressBar jProgressBar = progressbarfiles.get(filename);
        jProgressBar.setVisible(false);
        progressbarhm.put(jProgressBar, Boolean.FALSE);
        progressbarfiles.remove(jProgressBar);
        updateLocalFilesModel();
        try {
            updateServerFilesModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startDownload(String filename) {
        JProgressBar findProgressBar = findProgressBar();
        progressbarfiles.put(filename, findProgressBar);
    }

    @Override
    public void downloadInProgress(String filename, int progress) {
        JProgressBar jProgressBar = progressbarfiles.get(filename);
        jProgressBar.setVisible(true);
        setValue(jProgressBar, progress);
    }

    @Override
    public void finishDownload(String filename) {
        JProgressBar jProgressBar = progressbarfiles.get(filename);
        jProgressBar.setVisible(false);
        progressbarhm.put(jProgressBar, Boolean.FALSE);
        progressbarfiles.remove(jProgressBar);
        updateLocalFilesModel();
    }


}
