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
    public HdsClient(String name, int port) {
        _name = name;
        _port = port;
        startServer();
    }

    private void startServer() {
        Runnable runnable = new HdsServerClientStarter(_port, this);
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

                    if (tosend.equals("Exit")) {
                        dos.close();
                        dis.close();
                        s.close();
                        System.exit(0);
                        break;
                    }
                    //System.out.println(tosend);

                    JSONObject jo = this.sendJson(tosend);
                    if (jo.getString("Action").equals("buyGood")) {
                        connectToClient(host, 4000, jo);
                        continue;
                    }
                    String out = this.sendJson(tosend).toString();
                    dos.writeUTF(out);
                    System.out.println(s.toString() + " "+ out);
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

    public String connectToClient(String host, int port, JSONObject jo) {
        String answer = "";
        try {

            // getting localhost ip
            InetAddress ip = InetAddress.getByName(host);

            // establish the connection with server port 5056
            Socket s = new Socket(ip, port);
            s.setSoTimeout(10*1000);
            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler


            try {
                System.out.println("Client " + s + " sends " + jo.toString());
                dos.writeUTF(jo.toString());
                String received = dis.readUTF();
                if(port != 19999)
                    dos.writeUTF(received);
                answer = received;
                s.close();
                dis.close();
                dos.close();

            }
            catch (java.net.SocketTimeoutException timeout) {
                timeout.printStackTrace();
                //break;
            }
            catch (java.io.EOFException e0) {
                e0.printStackTrace();
                //break;
            }
            catch (Exception e) {
                e.printStackTrace();
                //break;
            }

            //s.close();
            // closing resources

        } catch(IOException e){
            e.printStackTrace();
        }
        return answer;
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
