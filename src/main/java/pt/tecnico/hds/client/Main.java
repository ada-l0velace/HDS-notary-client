package pt.tecnico.hds.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        /** Define a host server */
        String host = "localhost";
        /** Define a port */
        int port = 19999;

        HdsClient client = new HdsClient("user1", 4000);

        try
        {
            Scanner scn = new Scanner(System.in);

            // getting localhost ip
            InetAddress ip = InetAddress.getByName(host);

            // establish the connection with server port 5056
            Socket s = new Socket(ip, port);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler

            while (true) {
                try {
                    System.out.println(dis.readUTF());
                    String tosend = scn.nextLine();
                    dos.writeUTF(client.sendJson(tosend));

                    // If client sends exit,close this connection
                    // and then break from the while loop
                    if (tosend.equals("Exit")) {
                        System.out.println("Closing this connection : " + s);
                        s.close();
                        System.out.println("Connection closed");
                        break;
                    }

                    // printing date or time as requested by client
                    String received = dis.readUTF();
                    System.out.println(received);
                }
                catch (Exception e) {
                    continue;
                }
            }

            // closing resources
            scn.close();
            dis.close();
            dos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
