
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;

public class ClientCDN {
    private static JFrame f;
    private static JPanel leftPanel, rightPanel, choiceDisplayAreaPanel, buttonPanel, leftTopPanel, buttonInfoPanel;
    private static JLabel serverLabel, serverLabel1;
	private static JTextArea textAreaListFiles, textAreaViewContent;
    private static JComboBox<String> fileChoice;
	private static JButton viewContentButton, downloadButton, getInfoButton;
	

	ClientCDN() {
		init();
	}

	public void init() {
        f = new JFrame("Client GUI");
        System.out.println("Creating Client GUI!");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setBounds(500, 500, 1500, 1000);
        Font font = new Font("Serif", Font.BOLD, 25);
        
        leftPanel = new JPanel(new BorderLayout());
        leftTopPanel = new JPanel(new FlowLayout());
        buttonInfoPanel = new JPanel();
        buttonInfoPanel.setLayout(new BoxLayout(buttonInfoPanel, BoxLayout.LINE_AXIS));
        buttonInfoPanel.setBorder((BorderFactory.createEmptyBorder(0, 10, 10, 10)));
        serverLabel = new JLabel();
        serverLabel1 = new JLabel();
        String text = "These are the list of files available on server.";
        String text1 = "Please select one of them to download or view the content.";
        serverLabel.setSize(f.getWidth()/2, 0);
        serverLabel.setText(text);
        serverLabel1.setText(text1);
        textAreaListFiles = new JTextArea(20, 50);
        getInfoButton = new JButton("getInfo");
        
        serverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        serverLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftTopPanel.add(serverLabel);
        leftTopPanel.add(serverLabel1);
        leftTopPanel.add(textAreaListFiles);
        textAreaListFiles.setEditable(false);
        serverLabel.setFont(font);
        serverLabel1.setFont(font);
        getInfoButton.setFont(font);

        buttonInfoPanel.add(Box.createRigidArea(new Dimension(300, 10)));
        buttonInfoPanel.add(getInfoButton);

        leftPanel.add(leftTopPanel, BorderLayout.CENTER);
        leftPanel.add(buttonInfoPanel, BorderLayout.PAGE_END);
        System.out.println("Creating left panel!");

        // Set content features in textAreaListFiles
        textAreaListFiles.setFont(new Font("Monospaced", Font.PLAIN, 23));
        textAreaListFiles.setLineWrap(true);
        textAreaListFiles.setWrapStyleWord(false);
        
        rightPanel = new JPanel(new BorderLayout());

        choiceDisplayAreaPanel = new JPanel();
        choiceDisplayAreaPanel.setLayout(new BoxLayout(choiceDisplayAreaPanel, BoxLayout.PAGE_AXIS));

        fileChoice = new JComboBox<String>(); //JComboBox for choosing the file names available on server
        fileChoice.setSize(30, 20);
        fileChoice.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        textAreaViewContent = new JTextArea(23, 50);
        
        choiceDisplayAreaPanel.add(fileChoice);
        choiceDisplayAreaPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        choiceDisplayAreaPanel.add(textAreaViewContent);
        choiceDisplayAreaPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Set content features in textAreaViewContent
        textAreaViewContent.setFont(new Font("Monospaced", Font.PLAIN, 23));
        textAreaViewContent.setLineWrap(true);
        textAreaViewContent.setWrapStyleWord(true);
        textAreaViewContent.setEditable(false);
        
        // Buttons
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder((BorderFactory.createEmptyBorder(0, 10, 10, 10)));
        viewContentButton = new JButton("ViewContent");
		downloadButton = new JButton("Download");
        buttonPanel.add(Box.createRigidArea(new Dimension(350, 0)));
        buttonPanel.add(viewContentButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(downloadButton);

        rightPanel.add(choiceDisplayAreaPanel, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.PAGE_END);
        System.out.println("Creating right panel!");

        viewContentButton.setFont(font);
        downloadButton.setFont(font);
        fileChoice.setFont(font);

        JRootPane rootPane = f.getRootPane();
        rootPane.setDefaultButton(getInfoButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(f.getWidth()/2);
        f.getContentPane().add(splitPane, BorderLayout.CENTER);
        System.out.println("Create 2 board!");
        
        f.setLocationRelativeTo(null);
        f.setVisible(true);


        viewContentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Doing viewContent action!");
                String neededFileName = fileChoice.getSelectedItem().toString();
                System.out.println("getting needed filename: " + neededFileName);

                File folder = new File("");
                String pathAfterReplace = turnValidPathToStringClient(folder);
                File currentClientFolder = new File(pathAfterReplace);
                File[] listOfFiles = currentClientFolder.listFiles();
                System.out.println(listOfFiles.length);
                if (listOfFiles.length != 0) {
                    if (fileArrayContains(listOfFiles, neededFileName) == true) {
                        System.out.println("Start reading the " + neededFileName);
                        Scanner sc;
                        String fileDownloadPath = getFileDownloadedPath(neededFileName);
                        textAreaViewContent.setText("");
                        try {
                            sc = new Scanner(new File(fileDownloadPath));
                            while(sc.hasNextLine()){
                                String str = sc.nextLine();
                                textAreaViewContent.append(str);
                                textAreaViewContent.append("\r\n");
                            }
                            sc.close();//close the file
                            System.out.println("success view the downloaded file content");
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    else {
                        textAreaViewContent.setText("");
                        textAreaViewContent.append(neededFileName + " is not downloaded. Please download the file first.");
                    }
                }
                else {
                    textAreaViewContent.setText("");
                    textAreaViewContent.append("No downloaded files");
                }
            }
        });

        getInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Socket s1;
                try {
                    s1 = new Socket("localhost", 10001);
                    System.out.println("getInfoButton socket 10001 Connected!");
                    sendMethodPortToCache(s1, "8000");
                    // the sendRequestInfoToServer method with port 8000
                    System.out.println("Send port 8000 successfully");
                } catch (Exception e1) {
                    e1.printStackTrace();
                } 
                
                sendRequestInfoToCache();
                System.out.println("success send the info message request to server");
                System.out.println("click on get info button");
                displayInfo();
                System.out.println("success display the contents");
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Socket s1;
                try {
                    s1 = new Socket("localhost", 10001);
                    System.out.println("downloadButton socket 10001 Connected!");
                    sendMethodPortToCache(s1, "8777");
                    // the sendNeededFileNameToServer method with port 8777
                    System.out.println("Send port 8777 successfully");
                } catch (Exception e1) {
                    e1.printStackTrace();
                } 

                String neededFileName = fileChoice.getSelectedItem().toString();
                String neededFileDownloadedPath = getFileDownloadedPath(neededFileName);
                System.out.println("neededFileName: " + neededFileName);
                System.out.println("neededFileDownloadedPath: " + neededFileDownloadedPath);
                sendNeededFileNameToCache(neededFileName);
                System.out.println("success send file name");
                byte[] neededFileByteArray = getByteFileArrayFromCache();
                System.out.println("success get file byte[]");
                createFileBasedOnByteArray(neededFileDownloadedPath, neededFileByteArray);
                //downloadFile(neededFileDownloadedPath);
                System.out.println("success download needed file");
            }

        });

        f.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                Socket s1;
                try {
                    s1 = new Socket("localhost", 10001);
                    System.out.println("sendRequestInfoToServer socket 10001 Connected!");
                    sendMethodPortToCache(s1, "8001");
                    // the sendRequestMessageWindowCloseToServer method with port 8001
                    System.out.println("Send port 8001 successfully");
                } catch (Exception e1) {
                    e1.printStackTrace();
                } 

                sendRequestMessageWindowCloseToCache();
            }            
        });
    }

    public boolean fileArrayContains(File[] fileArray, String fileName) {
        for (int i = 0; i < fileArray.length; i++) {
            if (fileArray[i].isFile()) {
                if (fileArray[i].getName().equals(fileName)){
                    return true;
                }
            }
        }
        return false;
    }
    
    public void sendMethodPortToCache(final Socket socket, String portString) {
        System.out.println("send Method Port To Server with socket " + socket + " Connected!");
        try {
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            System.out.println("Sending port string " + portString + " to the Cache.");
            dataOutputStreamToServer.writeUTF(portString);
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRequestMessageWindowCloseToCache() {
        System.out.println("window closed is clicked, closing client...");
        Socket socket;
        try {
            socket = new Socket("localhost", 8001);
            System.out.println("sendRequestInfoToCache socket 8001 Connected!");
            System.out.println("Sending string to the Cache.");
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            System.out.println("Sending string to the Server.");
            dataOutputStreamToServer.writeUTF("Close the Windows.");
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            System.out.println("Closing socket 8001...");
            socket.close();
            System.exit(0);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void sendRequestInfoToCache() {
        try {
            Socket socket = new Socket("localhost", 8000);
            System.out.println("sendRequestInfoToCache socket 8000 Connected!");
            System.out.println("Sending string to the Cache.");
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            dataOutputStreamToServer.writeUTF("Get list of available files in server.");
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            System.out.println("socket 8000 is closing...");
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] getByteFileArrayFromCache() {
        Socket socket;
        byte[] byteFileArray = new byte[1];
        try {
            socket = new Socket("localhost", 9999);
            System.out.println("getArrayListFromServer() socket 9999 Connected!");
            DataInputStream dIn = new DataInputStream(socket.getInputStream());
            int length = dIn.readInt();   
            byteFileArray = new byte[length];                 // read length of incoming message
            if(length>0) {
                dIn.readFully(byteFileArray, 0, byteFileArray.length); // read the message
            }
            dIn.close();
            System.out.println("socket 9999 is closing...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteFileArray;
    }

    public void createFileBasedOnByteArray(String neededFileDownloadedPath, byte[] neededFileByteArray) {
        System.out.println("Creating neededFileDownloadedPath: " + neededFileDownloadedPath + "...");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(neededFileDownloadedPath);
            fos.write(neededFileByteArray);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayInfo() {
        System.out.println("displaying info...");
        ArrayList<String> filesAvailableOnServer = getArrayListFromServer();
        for (int i = 0; i < filesAvailableOnServer.size(); i++) {
            System.out.println(filesAvailableOnServer.get(i)); //simpleNumber.txt
        }
        displayFileList(filesAvailableOnServer);
        fileChoice.removeAllItems();
        fileChoice.addItem("Select file...");
        System.out.println("success display file list...");
        for (int i = 0; i < filesAvailableOnServer.size(); i++) {
            System.out.println(filesAvailableOnServer.get(i)); //simpleNumber.txt
            fileChoice.addItem(filesAvailableOnServer.get(i));
        }
    }

    public String getFileDownloadedPath(String neededFileName) {
        File folder = new File("");
        String pathAfterReplace = turnValidPathToStringClient(folder);
        String fileDownloadedPath = pathAfterReplace + '/' + neededFileName;
        return fileDownloadedPath;
    }

    public String turnValidPathToStringClient(File folder){
        String pathString = folder.getAbsolutePath();
        if (pathString.contains("\\clientFile") == false) {
            pathString += "\\clientFile";
        }
        System.out.println(pathString);
        String pathAfterReplace = pathString.replace('\\', '/');
        System.out.println(pathAfterReplace);
        return pathAfterReplace;
    }
    
    private void displayFileList(ArrayList<String> filesAvailableOnServer){
		textAreaListFiles.setText("");
        fileList(filesAvailableOnServer);
	}

    private ArrayList<String> getArrayListFromServer() {
        ArrayList<String> filesAvailableOnServer = new ArrayList<String>();
        try {
            Socket socket = new Socket("localhost", 8999);
            System.out.println("getArrayListFromServer() socket 8999 Connected!");
            ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());
            filesAvailableOnServer = (ArrayList<String>) inFromServer.readObject();
            inFromServer.close();
            System.out.println("Closing socket 8999...");
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return filesAvailableOnServer;
        
    }

    // filesAvailableOnServer = {simpleNumber.txt};
	private static void fileList(ArrayList<String> filesAvailableOnServer) {
        for (int i = 0; i < filesAvailableOnServer.size(); i++) {
            textAreaListFiles.append(filesAvailableOnServer.get(i));
            textAreaListFiles.append("\r\n");
        }
    }

    public void sendNeededFileNameToCache(String neededFileName) {
        try {
            Socket socket = new Socket("localhost", 8777);
            System.out.println("sendNeededFileNameToCache socket 8777 Connected!");
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            System.out.println("Sending string to the Server.");
            dataOutputStreamToServer.writeUTF(neededFileName);
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            System.out.println("Closing socket 8777...");
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String neededFileDownloadedPath) {
        try {
            // Set the ip to be localhost and port to be 8000, matched ServerSocket port
            Socket socket = new Socket("localhost", 8888);
            System.out.println("downloadFile 8888 connected");
            byte[] fileByteArray = new byte[1024];
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(neededFileDownloadedPath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int byteRead = is.read(fileByteArray, 0, fileByteArray.length);
            System.out.println("byteRead: " + byteRead);
            bos.write(fileByteArray, 0, byteRead);
            bos.close();
            System.out.println("socket 8888 is closing...");
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

	public static void main(String[] args) {
		new ClientCDN();

	}
}

    
    