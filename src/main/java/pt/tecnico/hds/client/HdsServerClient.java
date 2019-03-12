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

    HdsServerClient(Socket s, int i, DataInputStream dis, DataOutputStream dos) {
        this.connection = s;
        this.ID = i;
        this.dis = dis;
        this.dos = dos;
    }

    public void run() {
        String received;
        String toreturn;
        System.out.println("Client " + this.connection + " Opens...");
        while (true) {
            try {

                // Ask user what he wants
                //dos.writeUTF("What do you want?[buyGood]..\n" +
                //        "Type Exit to terminate connection.");

                // receive the answer from client
                received = dis.readUTF();

                if (received.equals("Exit")) {
                    System.out.println("Client " + this.connection + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.connection.close();
                    System.out.println("Connection closed");
                    this.dis.close();
                    this.dos.close();
                    //Thread.currentThread().interrupt();
                    break;
                }

                this.TimeStamp = new java.util.Date().toString();

                // write on output stream based on the
                // answer from the client
                JSONObject jsonObj = new JSONObject(received);
                /*if (jsonObj.isNull("Action"))
                    received = jsonObj.get("Action").toString();
                else
                    received = "";*/
                received = jsonObj.get("Action").toString();
                toreturn = jsonObj.toString();
                switch (received) {
                    case "buyGood" :
                        //dos.writeUTF("Exit");
                        JSONObject j0 = Main.client.sendJson("transferGood "+ jsonObj.getString("Good") + " " + jsonObj.getString("Buyer"));
                        Main.client.connectToClient("localhost", 19999, j0);
                        System.out.println("Client " + this.connection + " sends exit...");
                        this.connection.close();
                        this.dis.close();
                        this.dos.close();
                        break;

                    default:
                        dos.writeUTF("Invalid input");
                        break;
                }
            }
            catch (java.net.SocketException socketEx) {
                socketEx.printStackTrace();
                //Thread.currentThread().interrupt();
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    dos.writeUTF("Invalid input");
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        //Main.client.connectToServer("localhost", 19999);

    }

}