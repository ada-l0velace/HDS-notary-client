package pt.tecnico.hds.client;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HdsClient {
    private String _name;
    private int _port;

    HdsClient(String name, int port) {
        _name = name;
        _port = port;
        startServer();
    }

    private void startServer() {
        Runnable runnable = new HdsServerClientStarter(_port);
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void connectToServer(String host, int port) {
        try
        {
            Boolean clientConnection = false;
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
                    if (tosend.equals("sendMessageToClient")) {
                        dos.writeUTF("Exit");
                        tosend = "Exit";
                        clientConnection = true;
                    }
                    else {
                        dos.writeUTF(this.sendJson(tosend).toString());
                    }
                    // If client sends exit,close this connection
                    // and then break from the while loop
                    if (tosend.equals("Exit")) {
                        System.out.println("Closing this connection : " + s);
                        s.close();
                        System.out.println("Connection closed");
                        if (clientConnection)
                            connectToServer(host, 4000);
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

    private JSONObject actionGoodSeller(String command, String s) {
        String [] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 2) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put(s, _name);
        }
        else {
            jo.put("Action", "Invalid command");
        }
        return jo;
    }

    private JSONObject intentionToSell(String command) {
        return actionGoodSeller(command, "Seller");
    }

    private JSONObject getStateOfGood(String command) {
        return actionGoodSeller(command, "Buyer");
    }

    private JSONObject buyGood(String command) {
        return actionGoodSeller(command, "Buyer");
    }

    private JSONObject transferGood(String command) {
        String [] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 3) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put("Seller", _name);
            jo.put("Buyer", cmds[2]);
        }
        else {
            jo.put("Action", "Invalid command");
        }
        return jo;
    }

    public JSONObject sendJson(String command) {
        if (command.startsWith("transferGood")) {
           return transferGood(command);
        }
        else if (command.startsWith("intentionToSell")) {
            return intentionToSell(command);
        }
        else if (command.startsWith("getStateOfGood")) {
            return getStateOfGood(command);
        }
        else if (command.startsWith("buyGood")) {
            return buyGood(command);
        }
        JSONObject jo = new JSONObject();
        jo.put("Action", "Invalid command");
        return jo;
    }
}
