import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private static File log;
    private static final String fileName = "log.txt";

    public static void main(String[] args) {

        try {
            log = new File(fileName);
            log.createNewFile();
            ServerSocket ss = new ServerSocket(23);
            Socket s = ss.accept();//establishes connection
            InetSocketAddress clientSocket = (InetSocketAddress) s.getRemoteSocketAddress();
            System.out.println("Connected to " + clientSocket.getAddress() + ":" + clientSocket.getPort() + " successfully!");
            DataInputStream dis = new DataInputStream(s.getInputStream());
            String str = (String) dis.readUTF();
            processMessage(str, s);
            ss.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void processMessage(String message, Socket socket) throws IOException {
//        message = message.replace("telnet", "").trim();
        //String[] command = message.split("\\s+",2);
        String pattern = "telnet (send|upload|exec|history)(-\\w)?(.*)";

        // Create a Pattern object
        Pattern compiledPattern = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = compiledPattern.matcher(message);
        if (!m.find()) sendResponse("Wrong input!", socket);
        log(message, socket);
        switch (m.group(1).toLowerCase()) {
            case "upload":
                String filePath = m.group(3).trim();
                // Send a file
                //readFromFile("test.txt");
                File file = new File(filePath);
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                OutputStream out =  socket.getOutputStream();

                byte[] byteArray = new byte[(int) file.length()];

                bis.read(byteArray,0,byteArray.length);
                out.write(byteArray,0,byteArray.length);
                out.flush();

                OutputStream outputStream = socket.getOutputStream();
                System.out.println("Sending " + filePath + "(" + byteArray.length + " bytes)");
                outputStream.write(byteArray, 0, byteArray.length);
                outputStream.flush();
                System.out.println("Done.");
                if (bis != null) bis.close();
                if (out != null) out.close();
                if (socket!=null) socket.close();
                break;

            case "exec":
                String cmdCommand = m.group(3);
                ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmdCommand);
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line, pOutput = "";
                while ((line = r.readLine()) != null) {
                    pOutput += line + "\n";
                }
                sendResponse(pOutput, socket);
                break;

            case "send":
                if (m.group(2) != null && m.group(2).equals("-e")) {
//                    SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());
//                    InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
//                    SSLSocket sslServer = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));
//                    sslServer.setUseClientMode(false);
//                    sslServer.setEnabledProtocols(sslServer.getSupportedProtocols());
//                    // sslServer.startHandshake();
//                    socket = sslServer;
//                    remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
//                    sf = sslContext.getSocketFactory();
//                    SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));
//                    int len = is.read(command[2].getBytes());
//                    if (len <= 0) {
//                        throw new IOException("No data received!");
//                    }
//                    System.out.println("Server received " + new String(command[2].getBytes(), 0, len));
                } else System.out.println("Server received " + m.group(3));
                sendResponse("Message received successfully!", socket);
                break;

            case "history":
                printLogs(socket);
                break;
        }
    }


    private static void log(String str, Socket client) {
        try {
            InetSocketAddress clientSocket = (InetSocketAddress) client.getRemoteSocketAddress();
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.newLine();
            out.write(clientSocket.getAddress().toString().replace("/","") + ":" + client.getPort() + " Date: " + (new Date()).toString());
            out.newLine();
            out.write(str);
            out.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void printLogs(Socket socket) {
        try {
            Scanner myReader = new Scanner(log);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                dout.writeUTF(data);
                dout.flush();
                System.out.println(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void sendResponse(String message, Socket socket) throws IOException {
        DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
        dout.writeUTF(message);
        dout.flush();
    }
}