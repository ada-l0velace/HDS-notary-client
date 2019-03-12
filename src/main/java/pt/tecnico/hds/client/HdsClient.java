package pt.tecnico.hds.client;
import org.json.JSONObject;

import java.io.*;
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
                    //System.out.println(dis.readUTF());
                    System.out.println("What do you want?[transferGood | intentionToSell | buyGood | getStateOfGood]..\n" +
                            "Type Exit to terminate connection.");
                    String tosend = "";

                    tosend = scn.nextLine();

                    //System.out.println(tosend);

                    JSONObject jo = this.sendJson(tosend);
                    if (jo.getString("Action").equals("buyGood")) {
                        dos.writeUTF("Exit");
                        tosend = "Exit";
                        clientConnection = true;
                    } else {
                        dos.writeUTF(this.sendJson(tosend).toString());
                    }
                    // If client sends exit,close this connection
                    // and then break from the while loop
                    if (tosend.equals("Exit")) {
                        System.out.println("Closing this connection : " + s);

                        System.out.println("Connection closed");
                        if (clientConnection) {
                            dos.close();
                            dis.close();
                            s.close();
                            connectToClient(host, 4000, jo);
                        }
                        break;
                    }

                    // printing date or time as requested by client
                    String received = dis.readUTF();
                    System.out.println(received);

                }
                catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void connectToClient(String host, int port, JSONObject jo) {
        try
        {
            Boolean clientConnection = false;
            //Scanner scn = new Scanner(System.in);

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
                    dos.writeUTF(jo.toString());
                    //System.out.println("WTF");
                    // printing date or time as requested by client
                    String received = dis.readUTF();
                    System.out.println(received);
                    if (received.equals("Exit")) {
                        System.out.println("Closing this connection : " + s);
                        System.out.println("Connection closed");
                        dos.writeUTF("Exit");
                        dis.close();
                        dos.close();
                        s.close();
                        JSONObject j0 = sendJson("transferGood "+ jo.getString("Good") + " " + jo.getString("Buyer"));
                        System.out.println(j0.toString());
                        connectToClient(host, 19999, j0);
                        break;
                    }
                    else {
                        System.out.println("Closing this connection : " + s);
                        System.out.println("Connection closed");
                        dis.close();
                        dos.close();
                        s.close();
                        connectToServer(host, 19999);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            //s.close();
            // closing resources

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
