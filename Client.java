import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private final static int FILE_SIZE = 6022386;

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 23);
            boolean useFile;
            Scanner scan = new Scanner(System.in);
            //checkAvailablePorts("localhost");

            String message = "telnet upload test.txt";
//            String message = "telnet exec dir";
//            String message = "telnet send delaram";
//            String message = "telnet history";

            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
/*            System.out.print("Do you want to send file?(y/n) ");
            useFile = (scan.nextLine().toLowerCase().equals("y")) ? true : false;
            if (useFile) {
                System.out.print("Your file name: ");
                File myFile = new File(scan.nextLine() + ".txt");
                Scanner myReader = new Scanner(myFile);
                int i = 0;
                while (myReader.hasNextLine()) {
                    message += myReader;
                    message += "\n";
                }
            } else {
                System.out.print("Message: ");
                message = scan.nextLine();
            }*/
            dout.writeUTF(message);
            dout.flush();
            if (message.contains("upload")) {
                String fileName = "C:\\Users\\Delaram\\Desktop\\test" + Math.random() + ".txt";
                receiveFile(s, fileName);
            } else if (message.contains("history")) {
                DataInputStream dis = new DataInputStream(s.getInputStream());
                String str;
                while ((str = dis.readUTF()) != null) {
                    System.out.println(str);
                }
            } else receiveMessage(s);
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void receiveMessage(Socket s) throws IOException {
        DataInputStream dis = new DataInputStream(s.getInputStream());
        String str = (String) dis.readUTF();
        System.out.println(str);
    }

    private static void checkAvailablePorts(String host) {
        int stopPortRange = 65365;
        ArrayList<Integer> availablePorts = new ArrayList<>();
        ServerSocket ss = null;
        DatagramSocket ds = null;
        //TODO timeout + multi thread
        for (int port = 0; port < stopPortRange; port++) {
            try {
                (new Socket(host, port)).close();
                availablePorts.add(port);
            } catch (IOException e) {
            }
        }
        System.out.println(availablePorts);
    }

    private static void receiveFile(Socket client, String path) {
        try {
            byte[] byteArray = new byte[FILE_SIZE];
            InputStream inputStream = client.getInputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(path));
            int bytesRead = inputStream.read(byteArray, 0, byteArray.length);
            int current = bytesRead;

            do {
                bytesRead = inputStream.read(byteArray, current, (byteArray.length - current));
                if (bytesRead >= 0) current += bytesRead;
            } while (bytesRead > -1);

            bufferedOutputStream.write(byteArray, 0, current);
            bufferedOutputStream.flush();
            System.out.println("File downloaded (" + current + " bytes read)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
