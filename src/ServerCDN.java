import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerCDN {

    ServerCDN() {
        try {
            while (true) {
                ServerSocket ss = new ServerSocket(10000);
                System.out.println("getPort ServerSocket 10000 Server & Cache connections...");
                String requestPort = "";
                Socket s = ss.accept();
                System.out.println("Connection from " + s + " Server & Cache!");

                ArrayList<String> filesAvailableOnServer = listFileAvailableOnServer();
                System.out.println("get list files");

                InputStream is = s.getInputStream();
                DataInputStream disFromClient = new DataInputStream(is);
                requestPort = disFromClient.readUTF();
                System.out.println("The message port sent from the cache was: " + requestPort);

                switch (requestPort) {
                    case "8300":
                        String requestMessageGetInfo = "Get list of available files in server.";
                        String requestMessageGetInfoMatch = getRequestMessageGetInfoFromCache();
                        System.out.println(requestMessageGetInfoMatch);
                        if (requestMessageGetInfoMatch.equals( requestMessageGetInfo)) {
                            System.out.println("request message matched.");
                            sendFileNameArrayList(filesAvailableOnServer);
                            System.out.println("sendFileNameArrayList successfully");
                        }
                        break;
                    case "8005":
                        //String neededFilePath = "c:/.../serverFile/simpleNumber.txt"; example
                        String neededFileName = getNeededFileNameFromCache();
                        String neededFilePath = convertFileNameToPath(neededFileName);
                        System.out.println("required file path: " + neededFilePath);
                        downloadFileToCache(neededFilePath);
                        System.out.println("complete download Request from Cache");
                        break;
                    case "8666":
                        System.out.println("Close the Windows.");
                        System.exit(0);
                        break;
                }
                disFromClient.close();
                s.close();
                ss.close();
            }
        }
        catch (Exception e){
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

    public static String turnValidPathToStringServer(File folder) {
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
        File folder = new File(""); // current path
        //folder.getAbsolutePath() to print the absolute path, e.g. C:\...\CS711 A1\src
        //need to change \ to / to be determine as directory
        System.out.println(folder.getAbsolutePath());
        String pathAfterReplace = turnValidPathToStringServer(folder);
        return pathAfterReplace;
    }

    public static void sendFileNameArrayList(ArrayList<String> filesAvailableOnServer) {
        try {
            ServerSocket serverSocket = new ServerSocket(8100);
            System.out.println("sendFileNameArrayList ServerSocket 8100 connections...");
            //serverSocket.setSoTimeout(10000);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("sendFileNameArrayList socket 8100 Server & Cache is building...");
                ObjectOutputStream outToClient = new ObjectOutputStream(socket.getOutputStream());
                outToClient.writeObject(filesAvailableOnServer);
                outToClient.close();
                socket.close();
                System.out.println("Server socket 8100 closing...");
                serverSocket.close();
                System.out.println("Successfully send the arrayList");
            }
        }
        catch (Exception e) {
            //return;
            e.printStackTrace();
        }
    }

    private static String getRequestMessageGetInfoFromCache() {
        String requestMessage = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8300);
            System.out.println("getRequestMessageGetInfoFromCache ServerSocket 8300 connections...");
            //serverSocket.setSoTimeout(10000);
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + " Server & Cache!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            requestMessage = dataInputStreamFromClient.readUTF();
            System.out.println("Cache: " + requestMessage);
            System.out.println("Closing socket 8300.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            //return "";
            e.printStackTrace();
        }
        return requestMessage;
    }

    private static String getNeededFileNameFromCache() {
        String neededFileName = "";
        try {
            ServerSocket serverSocket = new ServerSocket(8005);
            System.out.println("getNeededFileNameFromCache ServerSocket 8005 connections...");
            //serverSocket.setSoTimeout(10000);
            Socket socket = serverSocket.accept();
            System.out.println("Connection from " + socket + " Server & Cache!");
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStreamFromClient = new DataInputStream(inputStream);
            neededFileName = dataInputStreamFromClient.readUTF();
            System.out.println("Cache-needed file name: " + neededFileName);
            System.out.println("Closing socket 8005.");
            serverSocket.close();
            socket.close();
        }
        catch (Exception e) {
            //return "";
            e.printStackTrace();
        }
        return neededFileName;
    }

    public String convertFileNameToPath(String neededFileName) {
        String pathAfterReplace = getServerFolderDirectory();
        String neededFilePath = pathAfterReplace + "/"+ neededFileName;
        return neededFilePath;
    }

    public void downloadFileToCache(String filePath) {
        try {
            ServerSocket serverSocket = new ServerSocket(8008);
            System.out.println("downloadFileToCache ServerSocket connections 8008...");
            //serverSocket.setSoTimeout(10000);
            File file = new File(filePath);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection from " + socket + " Server & Cache!");
                byte[] fileByteArray = new byte[(int)file.length()];
                System.out.println("Needed file length: " + (int)file.length());
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                bufferedInputStream.read(fileByteArray, 0, fileByteArray.length);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeInt(fileByteArray.length); //send the file length to Cache
                dos.write(fileByteArray, 0, fileByteArray.length);
                dos.flush();
                bufferedInputStream.close();
                System.out.println("Closing socket 8008.");
                socket.close();
                serverSocket.close();
                System.out.println("Successfully sending the file");
            }
        }
        catch (Exception e) {
            return;
        }
    }

    public static void main(String[] args) {
        new ServerCDN();    
    }
}
