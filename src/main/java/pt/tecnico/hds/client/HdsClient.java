package pt.tecnico.hds.client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.tecnico.hds.client.exception.HdsClientException;
import pt.tecnico.hds.client.exception.ManInTheMiddleException;
import pt.tecnico.hds.client.exception.ReplayAttackException;

public class HdsClient implements ILibrary {
    public final static Logger logger = LoggerFactory.getLogger(HdsClient.class);
    public String _name;
    public String _folder = "assymetricKeys/";
    public int _port;
    public int NREPLICAS = Main.replicas;
    public int _baseServerPort = 19999;
    public int _serverPort = 19999;
    public Map<String, Integer> _myMap = new HashMap<String, Integer>();
    public String serverPublicKey;
    public JSONArray requests = new JSONArray();
    public Thread serverThread;
    public ByzantineRegister _register = new ByzantineAtomicRegister(this);

    public HdsClient(String name, int port) {
        _name = name;
        _port = port;

        initUsers();
        startServer();
        if (!Files.exists(Paths.get("db"))) {
            new File("db").mkdirs();
        }
        if (Main.debug)
            serverPublicKey = "assymetricKeys/serverDebug.pub";
        else
            serverPublicKey = "assymetricKeys/server.pub";
        DatabaseManager.getInstance().createDatabase();
    }

    public String getPublicKeyPath() {
        return "assymetricKeys/" + _name + ".pub";
    }

    public boolean validateServerRequest(JSONObject serverAnswer) throws HdsClientException {
        String hash = Utils.getSHA256(serverAnswer.getString("Message"));
        String serverSigner = new JSONObject(serverAnswer.getString("Message")).getString("signer");
        Boolean signature = Utils.verifySignWithPubKeyFile(serverAnswer.getString("Message"), serverAnswer.getString("Hash"), _folder + serverSigner + ".pub");
        Boolean notReplayed = DatabaseManager.getInstance().verifyReplay(hash);
        Boolean b = signature && notReplayed;
        //System.out.println(signature);
        if (b) {
            DatabaseManager.getInstance().addToRequests(Utils.getSHA256(serverAnswer.getString("Message")));
            return true;
        }

        if (!notReplayed) {
            throw new ReplayAttackException(serverAnswer);
        }

        if (!signature) {
            throw new ManInTheMiddleException(serverAnswer);
        }
        return false;
    }

    private void initUsers() {
        for (int i = 1; i <= 10; i++) {
            _myMap.put("user" + i, 3999 + i);
        }
        //System.out.println(_myMap);
    }

    private void startServer() {
        Runnable runnable = new HdsServerClientStarter(_port, this);
        Thread thread = new Thread(runnable);

        thread.start();
        serverThread = thread;
    }

    public void shutDown() {
        System.exit(0);
    }

    public String solveChallenge(JSONObject serverAnswer, DataInputStream dis, DataOutputStream dos) throws IOException, HdsClientException {
        validateServerRequest(serverAnswer);
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase(Locale.ROOT);
        String digits = "0123456789";
        char[] alphanum = (digits).toCharArray();
        String message = serverAnswer.getString("Message");
        JSONObject challenge = new JSONObject(message);
        String rs = challenge.getString("RandomString");
        String hash = challenge.getString("SHA512");
        String X;
        System.out.println(serverAnswer.toString());
        //requests.put(serverAnswer);
        //requests += serverAnswer.toString() + "\n";
        for (char a : alphanum) {
            for (char b : alphanum) {
                for (char c : alphanum) {
                    for (char d : alphanum) {
                        X = "" + a + b + c + d;
                        //System.out.println(X);
                        if (Utils.getSHA512(X + rs).substring(0, 32).equals(hash)) {
                            JSONObject jCommand = new JSONObject();
                            jCommand.put("Action", "Challenge");
                            jCommand.put("User", _name);
                            jCommand.put("Timestamp", new java.util.Date().getTime());
                            jCommand.put("Answer", X);
                            String m = jCommand.toString();
                            JSONObject jo = buildFinalMessage(m, new JSONObject());
                            String toSend = jo.toString();
                            System.out.println(toSend);
                            //requests.put(jo);
                            dos.writeUTF(toSend);

                            return dis.readUTF();
                        }
                    }
                }
            }
        }
        return "abcd";
    }

    public void runCommands() {
        try {
            Scanner scn = new Scanner(System.in);

            while (true) {
                try {
                    System.out.println("What do you want?[transferGood | intentionToSell | buyGood | getStateOfGood]..\n" +
                            "Type Exit to terminate connection.");

                    String tosend = scn.nextLine();

                    if (tosend.equals("Exit")) {
                        //dos.close();
                        //dis.close();
                        //s.close();
                        shutDown();
                        break;
                    }
                    //System.out.println(tosend);

                    JSONObject jo = this.sendJson(tosend);
                    //requests.put(jo);
                    sendJson(jo);

                    if (jo.toString().contains("Wrong Syntax")) {
                        System.out.println(new JSONObject(jo.getString("Message")).getString("Action"));
                        continue;
                    }
                    /*if (out.contains("Invalid Command")) {
                        continue;
                    }*/


                } catch (HdsClientException e) {
                    logger.error(e.getMessage());
                    //System.exit(-1);
                } catch (JSONException jE) {
                    logger.error(jE.getMessage());
                    //System.exit(-1);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    //e.printStackTrace();
                    //break;
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            //e.printStackTrace();
        }

    }

    public String connectToClient(String host, int port, JSONObject jo) {
        String answer = null;
        int maxRetries = 10;
        int retries = 0;
        while (true) {
            try {

                // getting localhost ip
                InetAddress ip = InetAddress.getByName(host);

                // establish the connection with server port 5056
                Socket s = new Socket(ip, port);
                //s.setSoTimeout(100 * 1000);
                //s.setKeepAlive(true);
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                // the following loop performs the exchange of
                // information between client and client handler

                try {
                    String received;
                    if (port >= _serverPort) {
                        System.out.println("Client " + s + " sends " + jo.toString());
                        dos.writeUTF(jo.toString());
                        //System.out.println(_port);
                        String receivedChallenge = dis.readUTF();
                        JSONObject challenge = new JSONObject(receivedChallenge);
                        received = solveChallenge(challenge, dis, dos);
                        //requests.put(new JSONObject(received));
                    } else {
                        System.out.println("Client " + s + " sends " + jo.toString());
                        dos.writeUTF(jo.toString());
                        received = dis.readUTF();
                        //requests.put(new JSONObject(received));
                    }
                    //System.out.println(received);

                    answer = received;
                    s.close();
                    dis.close();
                    dos.close();

                } catch (IOException e) {
                    logger.error(e.getMessage() + " on port:" + port);
                    //e.printStackTrace();
                    retries++;
                    dis.close();
                    dos.close();
                    s.close();
                    if (retries == maxRetries)
                        break;
                    continue;
                    //e.printStackTrace();

                } catch (Exception e) {
                    e.printStackTrace();
                    s.close();
                    break;
                }


                break;
                // closing resources

            } catch (IOException e) {
                logger.error(e.getMessage() + " on port:" + port);
                e.printStackTrace();
                retries++;
                if (retries == maxRetries)
                    break;
                continue;
                //e.printStackTrace();
            }
        }
        return answer;
    }

    private boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {

            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    private JSONObject actionGoodSeller(String command, String s, String error) {
        String[] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 2) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put(s, _name);
        } else {
            jo.put("Action", "Wrong Syntax: " + error);
        }
        return jo;
    }

    private JSONObject actionBuyGood(String command, String s) {
        String[] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 3 && _myMap.containsKey(cmds[2])) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put(s, _name);
            jo.put("Seller", cmds[2]);
        } else {
            jo.put("Action", "Wrong Syntax: buyGood <goodId> <sellerId>");
        }
        return jo;
    }

    private JSONObject buildMessageGetStateOfGoodRead(JSONObject _message, int pid) {
        //JSONObject jCommand = buildMessageIntentionToSell("getStateOfGood "+_message.getString("Good"));
        _message.put("pid", pid);
        _message.put("rid", _register.getRid());
        String message = _message.toString();
        //finalMessage.put("Value", message);
        return buildFinalMessage(message, new JSONObject());
    }

    private JSONObject buildMessageTransferGoodWrite(JSONObject request, int pid) {
        JSONObject _message = new JSONObject(request.getString("Message"));
        _message.put("pid", pid);
        _message.put("Timestamp", _register.getWts());
        _message.put("signer", _name);
        String message = _message.toString();
        return buildFinalMessage(message, request);//buildFinalByzantineMessage(message, finalMessage);
    }

    private JSONObject buildMessageIntentionToSellWrite(JSONObject _message, int pid) {
        //JSONObject jCommand = buildMessageIntentionToSell("intentionToSell "+_message.getString("Good"));
        _message.put("pid", pid);
        _message.put("Timestamp", _register.getWts());
        _message.put("signer", _name);
        String message = _message.toString();
        return buildFinalMessage(message, new JSONObject()); /*buildFinalByzantineMessage(message, finalMessage);*/
    }

    public JSONObject buildFinalByzantineMessage(String message, JSONObject finalMessage) {
        finalMessage.put("Value", message);
        finalMessage.put("ValueSignature", Utils.signWithPrivateKey(message, "assymetricKeys/" + _name));
        return finalMessage;
    }

    private JSONObject buildMessageIntentionToSell(String command) {
        return actionGoodSeller(command, "Seller", "intentionToSell <goodId>");
    }


    private JSONObject buildMessageGetStateOfGood(String command) {
        return actionGoodSeller(command, "Buyer", "getStateOfGood <goodId>");
    }

    private JSONObject buildMessageBuyGood(String command) {
        return actionBuyGood(command, "Buyer");
    }

    private JSONObject buildMessageTransferGood(String command) {
        String[] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 3) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put("Seller", _name);
            jo.put("Buyer", cmds[2]);
        } else {
            jo.put("Action", "Wrong Syntax: transferGood <goodId> <buyerId>");
        }
        return jo;
    }

    public JSONObject buildFinalMessage(String message, JSONObject finalMessage) {

        finalMessage.put("Message", message);
        finalMessage.put("Hash", Utils.signWithPrivateKey(message, "assymetricKeys/" + _name));
        return finalMessage;
    }

    public JSONObject additionalStuff(JSONObject jCommand) {
        jCommand.put("Timestamp", new java.util.Date().getTime());
        jCommand.put("wts", _register.getWts());
        jCommand.put("rid", _register.getRid());
        jCommand.put("signer", _name);
        return jCommand;
    }


    public JSONObject sendJson(String command) {


        JSONObject finalMessage = new JSONObject();
        if (command.startsWith("transferGood")) {
            _register._wts++;
            _register._rid++;
            JSONObject jCommand = buildMessageTransferGood(command);
            jCommand = additionalStuff(jCommand);
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        } else if (command.startsWith("intentionToSell")) {
            _register._wts++;
            _register._rid++;
            JSONObject jCommand = buildMessageIntentionToSell(command);
            jCommand = additionalStuff(jCommand);
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        } else if (command.startsWith("getStateOfGood")) {
            _register._rid++;
            JSONObject jCommand = buildMessageGetStateOfGood(command);
            jCommand.put("Timestamp", new java.util.Date().getTime());
            jCommand.put("rid", _register.getRid());
            jCommand.put("signer", _name);
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        } else if (command.startsWith("buyGood")) {
            JSONObject jCommand = buildMessageBuyGood(command);
            jCommand = additionalStuff(jCommand);
            String message = jCommand.toString();
            return buildFinalMessage(message, finalMessage);
        }

        JSONObject jo = new JSONObject();
        jo.put("Action", "Invalid command");
        jo.put("Timestamp", new java.util.Date().getTime());
        jo.put("signer", _name);
        return buildFinalMessage(jo.toString(), finalMessage);
    }

    public JSONObject sendJson(JSONObject command) throws HdsClientException {
        String message = command.getString("Message");
        JSONObject messageJson = new JSONObject(message);
        switch (messageJson.getString("Action")) {
            case "transferGood":
                return transferGood(command);
            case "intentionToSell":
                return intentionToSell(command);
            case "getStateOfGood":
                return getStateOfGood(command);
            case "buyGood":
                return buyGood(command);
        }
        return null;
    }

    public JSONObject sendJson(String command, JSONObject secondMessage) {
        JSONObject j0 = sendJson(command);
        j0.put("Message2", secondMessage.getString("Message"));
        j0.put("Hash2", secondMessage.getString("Hash"));
        return j0;
    }

    public JSONObject checkSignature(String serverResponse) throws HdsClientException {
        JSONObject serverJson = new JSONObject(serverResponse);

        if (validateServerRequest(serverJson)) {
            System.out.println(serverResponse);
            return serverJson;
        }
        throw new ManInTheMiddleException(serverJson);
    }

    @Override
    public JSONObject getStateOfGood(JSONObject request) throws HdsClientException {

        return _register.read(request);
    }

    @Override
    public JSONObject buyGood(JSONObject request) throws HdsClientException {
        System.out.println("BUY SHIT");
        String tosend = new JSONObject(request.getString("Message")).getString("Seller");
        int clientPort = _myMap.get(tosend);

        String answerS = "";

        answerS = connectToClient("localhost", clientPort, request);
        JSONObject serverJson = new JSONObject(answerS);

        if (validateServerRequest(serverJson)) {

            String good = new JSONObject(request.getString("Message")).getString("Good");

            JSONObject refreshGood = sendJson("getStateOfGood " + good);


            JSONObject goodRequest = getStateOfGood(refreshGood);


            if (goodRequest != null && goodRequest.has("Value")) {
                long t = new JSONObject(goodRequest.getString("Value")).getLong("wts");
                if (_register._wts < t)
                    _register._wts = t;
                System.out.println(answerS);
            }

            return serverJson;
        } else {
            throw new ManInTheMiddleException(serverJson);
        }
    }

    @Override
    public JSONObject intentionToSell(JSONObject request) throws HdsClientException {
        return _register.write(request, true);
    }

    @Override
    public JSONObject transferGood(JSONObject request) throws HdsClientException {
        return _register.write(request, false);
    }

}
