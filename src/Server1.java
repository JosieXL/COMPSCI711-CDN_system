import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server1 {

    Server1() {
        try {
            while (true) {
                ServerSocket ss = new ServerSocket(10000);
                System.out.println("getPort ServerSocket 10000 connections...");
            
                String requestPort = "";
                Socket s = ss.accept();
                System.out.println("Connection from " + s + "!");

                ArrayList<String> filesAvailableOnServer = listFileAvailableOnServer();
                System.out.println("get list files");

                InputStream is = s.getInputStream();
                DataInputStream disFromClient = new DataInputStream(is);
                requestPort = disFromClient.readUTF();
                System.out.println("The message port sent from the client was: " + requestPort);

                switch (requestPort) {
                    case "8000":
                        String requestMessageGetInfo = "Get list of available files in server.";
                        String requestMessageGetInfoMatch = getRequestMessageGetInfoFromClient();
                        System.out.println(requestMessageGetInfoMatch);
                        if (requestMessageGetInfoMatch.equals( requestMessageGetInfo)) {
                            System.out.println("request message matched.");
                            sendFileNameArrayList(filesAvailableOnServer);
                            System.out.println("sendFileNameArrayList successfully");
                        }
                        break;
                    case "8777":
                        //String neededFilePath = "c:/Users/josie2018/Desktop/CS711 A1/src/serverFile/simpleNumber.txt"; // example
                        String neededFileName = getNeededFileNameFromClient();
                        String neededFilePath = convertFileNameToPath(neededFileName);
                        System.out.println("required file path: " + neededFilePath);
                        //File file = new File(neededFilePath);
                        DownLoadFileToClient(neededFilePath);
                        System.out.println("complete download Request");
                        break;
                    case "8001":
                        String windowClosed = "Close the Windows.";
                        String requestMessageWindowClosed = getRequestMessageWindowCloseFromClient();
                        if (requestMessageWindowClosed.equals(windowClosed)) {
                            System.out.println("window closed message match, closing server...");
                            //isWindowClosed=true;
                            System.exit(0);
                        }
                        break;
                }
                disFromClient.close();
                s.close();
                ss.close();
            }    
        }
        catch (Exception e) {
            System.out.println("Error!");
            e.printStackTrace();
        }
    }

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

    public static String turnValidPathToString(File folder) {
        String pathString = folder.getAbsolutePath();
        if (pathString.contains("\\serverFile") == false) {
            pathString += "\\serverFile";
        }
        System.out.println(pathString);
        String pathAfterReplace = pathString.replace('\\', '/');
        System.out.println(pathAfterReplace);
        return pathAfterReplace;
    }

    public static File[] getListOfFiles() {
        String pathAfterReplace = getServerFolderDirectory();
        File currentFolder = new File(pathAfterReplace);
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

    public static String getServerFolderDirectory() {
        File folder = new File(""); // current path, using folder.getAbsolutePath() to print the absolute path, C:\Users\josie2018\Desktop\CS711 A1\src, need to change \ to / to be determine as directory
        // Delete later
        try{
            System.out.println(folder.getAbsolutePath());
        }catch(Exception e){}
        //
        String pathAfterReplace = turnValidPathToString(folder);
        return pathAfterReplace;
    }

    public static void sendFileNameArrayList(ArrayList<String> filesAvailableOnServer) {
        try {
            ServerSocket serverSocket = new ServerSocket(8999);
            System.out.println("GetInfo ServerSocket 8999 connections...");
            //serverSocket.setSoTimeout(10000);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("sendFileNameArrayList socket 8999 is building...");
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
            e.printStackTrace();
        }
    }

    private static String getRequestMessageGetInfoFromClient() {
        String requestMessage = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            //serverSocket.setSoTimeout(10000);
            System.out.println("GetInfo ServerSocket 8000connections...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + "!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            requestMessage = dataInputStreamFromClient.readUTF();
            System.out.println("The message sent from the client was: " + requestMessage);
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
            System.out.println("ServerSocket 8001 connections...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + "!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            requestMessage = dataInputStreamFromClient.readUTF();
            System.out.println("The message sent from the client was: " + requestMessage);
            System.out.println("Closing socket 8001.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            //return "";
        }
        return requestMessage;
    }


    private static String getNeededFileNameFromClient() {
        String neededFileName = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8777);
            //serverSocket.setSoTimeout(10000);
            System.out.println("ServerSocket awaiting connections 8777...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + "!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            neededFileName = dataInputStreamFromClient.readUTF();
            System.out.println("The message sent from the client was: " + neededFileName);
            System.out.println("Closing socket 8777.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            //return "";
        }
        return neededFileName;
    }

    public String convertFileNameToPath(String neededFileName) {
        String pathAfterReplace = getServerFolderDirectory();
        String neededFilePath = pathAfterReplace + "/"+ neededFileName;
        return neededFilePath;
    }

    public void DownLoadFileToClient(String filePath) {
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("ServerSocket connections 8888...");
            //serverSocket.setSoTimeout(10000);
            File file = new File(filePath);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Building DownLoadFileToClient Socket connection ...");
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
            //return;
        }
    }

    public static void main(String[] args) {
        new Server1();
    }
}
