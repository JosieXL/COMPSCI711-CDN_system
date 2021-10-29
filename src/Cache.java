import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;
import java.io.*;
import java.net.*;

import java.text.SimpleDateFormat;


public class Cache {
    private static JFrame f;
    private static JPanel leftPanel, rightPanel, buttonRightPanel, cacheFilesPanel;
    private static JLabel cacheLogLabel, cacheFilesLabel;
	private static JTextArea textAreaListFiles, textAreaLog;
	private static JButton clearCacheContentButton;
    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;
    // Two arrayList to store Cached file name and content
    ArrayList<String> cachedFileNameList = new ArrayList<>();
    ArrayList<byte[]> cachedFileContentList = new ArrayList<>(); 

    public static void main(String[] args) {
        new Cache();
        
	}

	public Cache() {
        init();
	}

	public void init() {
        f = new JFrame("Cache GUI");
        System.out.println("Creating Cache GUI!");
        f.setBounds(500, 500, 1250, 800);
        Font font = new Font("Serif", Font.BOLD, 25);

        leftPanel = new JPanel(new BorderLayout());
        cacheLogLabel = new JLabel();
        String text = "Cache's Log";
        cacheLogLabel.setSize(f.getWidth()/2, 0);
        cacheLogLabel.setText(text);
        textAreaLog = new JTextArea(20, 50);
        cacheLogLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textAreaLog.setEditable(false);
        cacheLogLabel.setFont(font);
        

        leftPanel.add(cacheLogLabel, BorderLayout.NORTH);
        leftPanel.add(textAreaLog, BorderLayout.CENTER);
        System.out.println("Creating left panel!");

        // Set content features in textAreaListFiles
        textAreaLog.setFont(new Font("Monospaced", Font.PLAIN, 23));
        textAreaLog.setLineWrap(true);
        textAreaLog.setWrapStyleWord(false);
        textAreaLog.setEditable(false);
        
        rightPanel = new JPanel(new BorderLayout());

        cacheFilesPanel = new JPanel(new FlowLayout());
        cacheFilesPanel.setLayout(new BoxLayout(cacheFilesPanel, BoxLayout.PAGE_AXIS));       
        cacheFilesLabel = new JLabel();
        String text1 = "Cached list of files on the Server.";
        cacheFilesLabel.setText(text1);
        cacheFilesLabel.setFont(font);
        cacheFilesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textAreaListFiles = new JTextArea(23, 50);
        textAreaListFiles.setFont(new Font("Monospaced", Font.PLAIN, 23));
        textAreaListFiles.setLineWrap(true);
        textAreaListFiles.setWrapStyleWord(false);
        textAreaListFiles.setEditable(false);
        cacheFilesPanel.add(cacheFilesLabel);
        cacheFilesPanel.add(textAreaListFiles);
        
        // Button
        buttonRightPanel = new JPanel();
        buttonRightPanel.setLayout(new BoxLayout(buttonRightPanel, BoxLayout.LINE_AXIS));
        buttonRightPanel.setBorder((BorderFactory.createEmptyBorder(0, 10, 10, 10)));
        clearCacheContentButton = new JButton("clearCache");
        buttonRightPanel.add(Box.createRigidArea(new Dimension(350, 0)));
        buttonRightPanel.add(clearCacheContentButton);

        rightPanel.add(cacheFilesPanel, BorderLayout.CENTER);
        rightPanel.add(buttonRightPanel, BorderLayout.PAGE_END);
        System.out.println("Creating right panel!");

        clearCacheContentButton.setFont(font);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(f.getWidth()/2);
        f.getContentPane().add(splitPane, BorderLayout.CENTER);
        System.out.println("Create 2 board!");
        
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        
        f.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });

        clearCacheContentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                clearMethod(cachedFileNameList, cachedFileContentList);
            }
        });

        try {
            while (true) {

                //Cache as Client - only for cache to server
                Socket s = new Socket("localhost", 10000);
                System.out.println("sendRequestInfoToServer socket 10000 Connected!");
                sendMethodPortToServer(s, "8300");
                // the sendRequestInfoToServer method with port 8300
                System.out.println("Send port 8300 successfully");

                sendRequestInfoToServer();
                ArrayList<String> filesAvailableOnServer = getArrayListFromServer();
                for (int i = 0; i < filesAvailableOnServer.size(); i++) {
                    System.out.println(filesAvailableOnServer.get(i)); //simpleNumber.txt
                }
                displayFileList(filesAvailableOnServer);
                System.out.println("success display the contents");

                ServerSocket ss1 = new ServerSocket(10001);
                System.out.println("getPort ServerSocket 10001 Client & Cache connections...");
                String requestPort = "";
                Socket s1 = ss1.accept();
                System.out.println("Connection from " + s1 + " Client & Cache!");

                InputStream is = s1.getInputStream();
                System.out.println("is is fine");
                DataInputStream disFromClient = new DataInputStream(is);
                System.out.println("dis is fine");
                requestPort = disFromClient.readUTF();
                System.out.println("The message port sent from the client was: " + requestPort);

                switch (requestPort) {
                    case "8000":
                        String requestMessageGetInfo = "Get list of available files in server.";
                        String requestMessageGetInfoMatch = getRequestMessageGetInfoFromClient();
                        System.out.println(requestMessageGetInfoMatch);
                        if (requestMessageGetInfoMatch.equals(requestMessageGetInfo)) {
                            System.out.println("request message matched.");
                            sendFileNameArrayList(filesAvailableOnServer);
                            System.out.println("sendFileNameArrayList successfully");
                        }
                        break;
                    case "8777":
                        String neededFileName = getNeededFileNameFromClient();
                        System.out.println("required file path: " + neededFileName);
                        downloadNeedFileToClientFromCache(neededFileName, cachedFileNameList, cachedFileContentList);
                        //String neededFilePath = convertFileNameToPath(neededFileName);
                        //System.out.println("required file path: " + neededFilePath);
                        //downloadNeedFileToClient(neededFileName, neededFilePath);
                        System.out.println("complete download " + neededFileName);
                        break;
                    case "8001":
                        String windowClosed = "Close the Windows.";
                        String requestMessageWindowClosed = getRequestMessageWindowCloseFromClient();
                        if (requestMessageWindowClosed.equals(windowClosed)) {
                            System.out.println("window closed message match, closing server...");
                            s = new Socket("localhost", 10000);
                            System.out.println("sendRequestInfoToServer socket 10000 Connected!");
                            sendMethodPortToServer(s, "8666");
                            // the sendRequestMessageWindowCloseToServer with port 8666
                            System.out.println("Send port 8666 successfully");
                            sendRequestMessageWindowCloseToServer(s);
                            System.exit(0);
                        }
                        break;
                }
                disFromClient.close();
                s1.close();
                ss1.close();
                s.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            //break;
        }
    }

    public void clearMethod(final ArrayList<String> cachedFileNameList, final ArrayList<byte[]> cachedFileContentList) {
        cachedFileNameList.removeAll(cachedFileNameList);
        cachedFileContentList.removeAll(cachedFileContentList);
        textAreaLog.setText("");
    }

    public void sendMethodPortToServer(final Socket socket, String portString) {
        System.out.println("send Method Port To Server with socket " + socket + " Connected!");
        try {
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            System.out.println("Sending port string " + portString + " to the Server.");
            dataOutputStreamToServer.writeUTF(portString);
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            //socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void downloadNeedFileToClient(String neededFileName, String neededFilePath) {
        File folder = new File("");
        String pathAfterReplace = turnValidPathToStringCache(folder);
        File currentClientFolder = new File(pathAfterReplace);
        File[] listOfFiles = currentClientFolder.listFiles();
        System.out.println(listOfFiles.length);
        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    if (listOfFiles[i].getName().equals(neededFileName)){
                        // Cache folder contain needed file
                        DownLoadFileRequest(neededFilePath);
                        System.out.println("complete download Request");
                    }
                    else {
                        // Cache folder not contain needed file
                        sendNeededFileNameToServer(neededFileName);
                        System.out.println("success send file name");
                        String neededFileDownloadedPath = getFileDownloadedPath(neededFileName);
                        downloadFile(neededFileDownloadedPath);
                    }
                }
            }
        }          
    }

    private void downloadNeedFileToClientFromCache(String neededFileName, ArrayList<String> cachedFileNameList, ArrayList<byte[]> cachedFileContentList) {
        if (!cachedFileNameList.contains(neededFileName)) {
            Socket s;
            try {
                s = new Socket("localhost", 10000);
                System.out.println("sendRequestInfoToServer socket 10000 Connected!");
                sendMethodPortToServer(s, "8005");
                // the sendNeededFileNameToServer with port 8005
                System.out.println("Send port 8005 successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            sendNeededFileNameToServer(neededFileName);
            System.out.println("success send file name");
            byte[] neededFileByteArray = getDownloadFileByteArray();
            cachedFileNameList.add(neededFileName);
            cachedFileContentList.add(neededFileByteArray);
            
            for (int i=0; i<cachedFileNameList.size(); i++) {
                System.out.println(cachedFileNameList.get(i));
            }
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
            String date = df.format(new Date());
            String str1 = "user request: file " + neededFileName + " " + date;
            String str2 = "response: file " + neededFileName + " downloaded from the server";
            textAreaLog.append(str1);
            textAreaLog.append("\r\n");
            textAreaLog.append(str2);
            textAreaLog.append("\r\n");
        }
        else {
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
            String date = df.format(new Date());
            String str1 = "user request: file " + neededFileName + " " + date;
            String str2 = "response: cached file " + neededFileName;
            textAreaLog.append(str1);
            textAreaLog.append("\r\n");
            textAreaLog.append(str2);
            textAreaLog.append("\r\n");
        }
        sendByteFileArrayToClient(cachedFileContentList.get(cachedFileNameList.indexOf(neededFileName)));
        System.out.println("complete download Request to Client");
    }


    public void sendRequestInfoToServer() {
        try {
            Socket socket = new Socket("localhost", 8300);
            System.out.println("sendRequestInfoToServer socket 8300 Connected!");
            System.out.println("Sending string to the Server.");
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            dataOutputStreamToServer.writeUTF("Get list of available files in server.");
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            System.out.println("socket 8300 is closing...");
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFileDownloadedPath(String neededFileName) {
        File folder = new File("");
        String pathAfterReplace = turnValidPathToStringCache(folder);
        String fileDownloadedPath = pathAfterReplace + '/' + neededFileName;
        return fileDownloadedPath;
    }

    public static String turnValidPathToStringCache(File folder){
        String pathString = folder.getAbsolutePath();
        if (pathString.contains("\\cacheFile") == false) {
            pathString += "\\cacheFile";
        }
        System.out.println(pathString);
        String pathAfterReplace = pathString.replace('\\', '/');
        System.out.println(pathAfterReplace);
        return pathAfterReplace;
    }

    private void displayFileList(ArrayList<String> filesAvailableOnServer){
		textAreaListFiles.setText("");
        for (int i = 0; i < filesAvailableOnServer.size(); i++) {
            textAreaListFiles.append(filesAvailableOnServer.get(i));
            textAreaListFiles.append("\r\n");
        }
	}

    private ArrayList<String> getArrayListFromServer() {
        ArrayList<String> filesAvailableOnServer = new ArrayList<String>();
        try {
            Socket socket = new Socket("localhost", 8100);
            System.out.println("getArrayListFromServer() socket 8100 Connected!");
            ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());
            filesAvailableOnServer = (ArrayList<String>) inFromServer.readObject();
            inFromServer.close();
            System.out.println("socket 8100 is closing...");
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return filesAvailableOnServer;
        
    }

    public void sendRequestMessageWindowCloseToServer(Socket s) {
        System.out.println("window closed is clicked, closing client...");
        try {
            //socket = new Socket("localhost", 8666);
            //System.out.println("sendRequestInfoToServer socket 8666 Connected!");
            System.out.println("Sending string window closed to the Server.");
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            System.out.println("Sending string to the Server.");
            dataOutputStreamToServer.writeUTF("Close the Windows.");
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            //System.out.println("Closing socket 8666...");
            //socket.close();
            System.exit(0);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void sendNeededFileNameToServer(String neededFileName) {
        try {
            Socket socket = new Socket("localhost", 8005);
            System.out.println("sendNeededFileNameToServer socket 8005 Connected!");
            OutputStream outputStreamFromSocket = socket.getOutputStream();
            DataOutputStream dataOutputStreamToServer = new DataOutputStream(outputStreamFromSocket);
            System.out.println("Sending string to the Server.");
            dataOutputStreamToServer.writeUTF(neededFileName);
            dataOutputStreamToServer.flush();
            dataOutputStreamToServer.close();
            System.out.println("Closing socket...");
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String neededFileDownloadedPath) {
        try {
            Socket socket = new Socket("localhost", 8008);
            System.out.println("downloadFile 8008 connected");
            byte[] fileByteArray = new byte[1024];
            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(neededFileDownloadedPath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            int byteRead = is.read(fileByteArray, 0, fileByteArray.length);
            System.out.println("byteRead: " + byteRead);
            bos.write(fileByteArray, 0, byteRead);
            bos.close();
            System.out.println("socket is closing...");
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getDownloadFileByteArray() {
        byte[] fileByteArray = new byte[1024];
        try {
            Socket socket = new Socket("localhost", 8008);
            System.out.println("downloadFile 8008 connected");
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            int length = dis.readInt();
            System.out.println("fileByte array length: " + length);
            fileByteArray = new byte[length];
            if (length > 0) {
                dis.readFully(fileByteArray, 0, fileByteArray.length);
            }
            dis.close();
            System.out.println("socket 8008 is closing...");
            socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return fileByteArray;
    }

    // cache - client
    /*
    public static ArrayList<String> listFileAvailableOnServer() {
        File[] listOfFiles = getListOfFiles();
        System.out.println();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
              System.out.println("File " + listOfFiles[i].getName());
            }
        }
        // filesAvailableOnServer contains the list of available on server files
        ArrayList<String> filesAvailableOnServer = new ArrayList<String>();
        for (int i = 0; i < listOfFiles.length; i++) {
            if ((listOfFiles[i].isFile()) && (listOfFiles[i].getName().substring(listOfFiles[i].getName().length() - 4)).equals(".txt")) {
              filesAvailableOnServer.add(listOfFiles[i].getName());
            } 
        }

        // Delete later
        for (int i = 0; i < filesAvailableOnServer.size(); i++) {
            System.out.println(filesAvailableOnServer.get(i)); //simpleNumber.txt
        }
        //
        return filesAvailableOnServer;
    }
    

    public static File[] getListOfFiles() {
        String pathAfterReplace = getCurrentDirectory();
        File currentFolder = new File(pathAfterReplace);
        //File[] listOfFiles = folder.listFiles();
        File[] listOfFiles = currentFolder.listFiles();
        System.out.println(listOfFiles.length);
        if (listOfFiles != null) {
            for(File path:listOfFiles) {
               // prints file and directory paths
               System.out.println(path);
            }
        }
        return listOfFiles;
    }
*/
    public static String getCacheFolderDirectory() {
        File folder = new File(""); // current path, using folder.getAbsolutePath() to print the absolute path, C:\Users\josie2018\Desktop\CS711 A1\src, need to change \ to / to be determine as directory
        // Delete later
        try{
            System.out.println(folder.getCanonicalPath());
            System.out.println(folder.getAbsolutePath());
        }catch(Exception e){}
        //
        String pathAfterReplace = turnValidPathToStringCache(folder);
        return pathAfterReplace;
    }

    public static void sendFileNameArrayList(ArrayList<String> filesAvailableOnServer) {
        try {
            ServerSocket serverSocket = new ServerSocket(8999);
            //serverSocket.setSoTimeout(10000);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("sendFileNameArrayList socket 8999 is building...");
                //ObjectInputStream inFromClient = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream outToClient = new ObjectOutputStream(socket.getOutputStream());
                outToClient.writeObject(filesAvailableOnServer);
                outToClient.close();
                socket.close();
                System.out.println("Server socket 8999 closing...");
                serverSocket.close();
                System.out.println("Successfully send the arrayList");
            }
        }
        catch (Exception e) {
            //return;
            e.printStackTrace();;
        }
    }

    public static void sendByteFileArrayToClient(byte[] neededFileByteArray) {
        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            System.out.println("sendByteFileArrayToClient ServerSocket 9999 connections...");
            //serverSocket.setSoTimeout(10000);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("sendFileNameArrayList socket 9999 is building...");
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(neededFileByteArray.length); // write length of the byte array
                dos.write(neededFileByteArray);           // write the byte array
                socket.close();
                System.out.println("Server socket 9999 closing...");
                serverSocket.close();
                System.out.println("Successfully send the arrayList");
            }
        }
        catch (Exception e) {
            //return;
            e.printStackTrace();
        }
    }

    private static String getRequestMessageGetInfoFromClient() {
        String requestMessage = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("getRequestMessageGetInfoFromClient ServerSocket 8000 connections...");
            //serverSocket.setSoTimeout(10000);
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + " Client & Cache!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            requestMessage = dataInputStreamFromClient.readUTF();
            System.out.println("Client: " + requestMessage);
            System.out.println("Closing socket 8000.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            //return "";
            e.printStackTrace();
        }
        return requestMessage;
    }

    private static String getRequestMessageWindowCloseFromClient() {
        String requestMessage = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8001);
            //serverSocket.setSoTimeout(10000);
            System.out.println("getRequestMessageWindowCloseFromClient ServerSocket 8001 connections...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + " Client & Cache!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            requestMessage = dataInputStreamFromClient.readUTF();
            System.out.println("Client: " + requestMessage);
            System.out.println("Closing socket 8001.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            return "";
        }
        return requestMessage;
    }


    private static String getNeededFileNameFromClient() {
        String neededFileName = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8777);
            System.out.println("getNeededFileNameFromClient ServerSocket 8777 connections...");
            //serverSocket.setSoTimeout(10000);
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + " Client & Cache!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            neededFileName = dataInputStreamFromClient.readUTF();
            System.out.println("Client: " + neededFileName);
            System.out.println("Closing socket 8777.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            return "";
        }
        return neededFileName;
    }

    public String convertFileNameToPath(String neededFileName) {
        String pathAfterReplace = getCacheFolderDirectory();
        String neededFilePath = pathAfterReplace + "/"+ neededFileName;
        return neededFilePath;
    }

    public void DownLoadFileRequest(String filePath) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("ServerSocket connections 8888...");
            //serverSocket.setSoTimeout(10000);
            File file = new File(filePath);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Building DownLoadFileRequest Socket connection ...");
                byte[] fileByteArray = new byte[(int)file.length()];
                System.out.println("Needed file length: " + (int)file.length());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(fileByteArray, 0, fileByteArray.length);
                OutputStream os = socket.getOutputStream();
                os.write(fileByteArray, 0, fileByteArray.length);
                os.flush();
                bufferedInputStream.close();
                System.out.println("Closing socket 8888.");
                socket.close();
                serverSocket.close();
                System.out.println("Successfully download the file");
            }
        }
        catch (Exception e) {
            return;
        }
    }

}

