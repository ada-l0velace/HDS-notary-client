package pt.tecnico.hds.client;
import java.io.*;
import java.net.Socket;
import org.json.JSONObject;

public class HdsServerClient implements Runnable {
    private Socket connection;
    private String TimeStamp;
    private int ID;
    private DataInputStream dis;
    private DataOutputStream dos;
    private HdsClient _client;

    HdsServerClient(Socket s, int i, DataInputStream dis, DataOutputStream dos, HdsClient client) {
        this.connection = s;
        this._client = client;
        this.ID = i;
        this.dis = dis;
        this.dos = dos;
    }

    public void run() {
        String received;
        String toreturn;
        System.out.println("Client " + this.connection + " Opens...");
        //while (true) {
        try {
            // receive the answer from client
            received = dis.readUTF();

            this.TimeStamp = new java.util.Date().toString();

            // write on output stream based on the
            // answer from the client
            JSONObject jsonObj = new JSONObject(received);
            JSONObject saved = jsonObj;
            jsonObj = new JSONObject(jsonObj.getString("Message"));
            received = jsonObj.getString("Action");

            switch (received) {
                case "buyGood" :
                    JSONObject j0 = _client.sendJson("transferGood "+ jsonObj.getString("Good") + " " + jsonObj.getString("Buyer"), saved);
                    String answer = _client.connectToClient("localhost", 19999, j0);
                    System.out.println(connection+" "+ answer);
                    dos.writeUTF(answer);
                    //Thread.sleep(1000);
                    System.out.println("Client " + this.connection + " disconnecting");
                    break;

                default:
                    dos.writeUTF("Invalid input");
                    break;
            }
            this.connection.close();
            this.dis.close();
            this.dos.close();
        }
        catch (java.net.SocketException socketEx) {
            //socketEx.printStackTrace();
            //Thread.currentThread().interrupt();
            //break;
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                dos.writeUTF("Invalid input");
            }
            catch (IOException e1) {
                //e1.printStackTrace();
            }
            //break;
        }
        //}

    }

}