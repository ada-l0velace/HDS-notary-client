package pt.tecnico.hds.client;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HdsClient {
    public String _name;
    public int _port;
    public Map<String,Integer> _myMap = new HashMap<String,Integer>();
    public HdsClient(String name, int port) {
        _name = name;
        _port = port;
        initUsers();
        startServer();
        if (!Files.exists(Paths.get("db"))) {
            new File("db").mkdirs();
        }
        DatabaseManager.getInstance().createDatabase();
    }

    public boolean validateServerRequest(JSONObject serverAnswer) {
        String hash = Utils.getSHA256(serverAnswer.getString("Message"));
        return Utils.verifySignWithPubKeyFile(serverAnswer.getString("Message"), serverAnswer.getString("Hash"), "assymetricKeys/server.pub")
                && DatabaseManager.getInstance().verifyReplay(hash);
    }

    private void initUsers() {
        for (int i = 1; i <=10;i++) {
            _myMap.put("user"+i,3999+i);
        }
        //System.out.println(_myMap);
    }

    private void startServer() {
        Runnable runnable = new HdsServerClientStarter(_port, this);
        Thread thread = new Thread(runnable);
        thread.start();
    }
    public void shutDown() {
        System.exit(0);
    }

    public void connectToServer(String host, int port) {
        try {
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
                        shutDown();
                        break;
                    }
                    //System.out.println(tosend);

                    JSONObject jo = this.sendJson(tosend);
                    String out = jo.toString();

                    if(out.contains("Wrong Syntax")) {
                        System.out.println(new JSONObject(jo.getString("Message")).getString("Action"));
                        continue;
                    }
                    if (out.contains("Invalid Command")) {
                        continue;
                    }

                    if (new JSONObject(jo.getString("Message")).getString("Action").equals("buyGood")) {

                        int clientPort = _myMap.get(tosend.split(" ")[2]);
                        connectToClient(host, clientPort, jo);
                        continue;
                    }

                    dos.writeUTF(out);
                    System.out.println(s.toString() + " "+ out);
                    // printing date or time as requested by client
                    String received = dis.readUTF();
                    JSONObject serverAnswer = new JSONObject(received);

                    if(validateServerRequest(serverAnswer)) {
                        DatabaseManager.getInstance().addToRequests(Utils.getSHA256(serverAnswer.getString("Message")));
                        System.out.println(received);
                    }
                    else {
                        System.out.println("The reply from the server is not signed by the server or there was a replay attack!");
                    }

                }
                catch (JSONException jE) {}
                catch (Exception e) {
                    //e.printStackTrace();
                    //break;
                }
            }

        }catch(IOException e){
            //e.printStackTrace();
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
                System.out.println(received);
                /*if(port != 19999)h {
                    System.out.println("Client " + s + " sends " + received);
                    dos.writeUTF(received);
                }*/
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

    private JSONObject actionGoodSeller(String command, String s, String error) {
        String [] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 2) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put(s, _name);
        }
        else {
            jo.put("Action", "Wrong Syntax: ");
        }
        return jo;
    }

    private JSONObject actionBuyGood(String command, String s) {
        String [] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        System.out.println(cmds.length);
        if (cmds.length == 3 && _myMap.containsKey(cmds[2])) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put(s, _name);
        }
        else {
            jo.put("Action", "Wrong Syntax: buyGood <goodId> <sellerId>");
        }
        return jo;
    }

    private JSONObject intentionToSell(String command) {
        return actionGoodSeller(command, "Seller", "intentionToSell <goodId>");
    }

    private JSONObject getStateOfGood(String command) {
        return actionGoodSeller(command, "Buyer", "getStateOfGood <goodId>");
    }

    private JSONObject buyGood(String command) {
        return actionBuyGood(command, "Buyer");
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
            jo.put("Action", "Wrong Syntax: transferGood <goodId> <buyerId>");
        }
        return jo;
    }

    public JSONObject buildFinalMessage(String message, JSONObject finalMessage) {
        finalMessage.put("Message", message);
        finalMessage.put("Hash", Utils.signWithPrivateKey(message, "assymetricKeys/"+_name));
        return finalMessage;
    }

    public JSONObject sendJson(String command) {
        JSONObject finalMessage = new JSONObject();
        if (command.startsWith("transferGood")) {
            JSONObject jCommand = transferGood(command);
            jCommand.put("Timestamp", new java.util.Date().toString());
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        }
        else if (command.startsWith("intentionToSell")) {
            JSONObject jCommand = intentionToSell(command);
            jCommand.put("Timestamp", new java.util.Date().toString());
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        }
        else if (command.startsWith("getStateOfGood")) {
            JSONObject jCommand = getStateOfGood(command);
            jCommand.put("Timestamp", new java.util.Date().toString());
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        }
        else if (command.startsWith("buyGood")) {
            //System.out.println(command.split(" ").length);
            JSONObject jCommand = buyGood(command);
            jCommand.put("Timestamp", new java.util.Date().toString());
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        }

        JSONObject jo = new JSONObject();
        jo.put("Action", "Invalid command");
        finalMessage.put("Message", jo.toString());
        finalMessage.put("Hash", Utils.getSHA256(jo.toString()));
        return finalMessage;
    }

    public JSONObject sendJson(String command, JSONObject secondMessage) {
        JSONObject j0 = sendJson(command);
        j0.put("Message2",secondMessage.getString("Message"));
        j0.put("Hash2",secondMessage.getString("Hash"));
        return j0;
    }
}
