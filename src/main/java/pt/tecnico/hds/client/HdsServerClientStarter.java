package pt.tecnico.hds.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HdsServerClientStarter implements Runnable {
    private int _port;
    private HdsClient _client;

    HdsServerClientStarter(int port, HdsClient client) {
        _client = client;
        _port = port;
    }

    public void run() {
        int count = 0;
        while (true) {
            try {
                ServerSocket socket1 = new ServerSocket(_port);
                socket1.setReuseAddress(true);
                //System.out.println("HDS Client Server Starter Initialized");
                while (true) {
                    Socket connection = socket1.accept();

                    // obtaining input and out streams
                    DataInputStream dis = new DataInputStream(connection.getInputStream());
                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                    Runnable runnable = new HdsServerClient(connection, ++count, dis, dos, _client);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            } catch (Exception e) {
                _client.logger.error(e.getMessage());
                //System.exit(-1);
                //System.out.println(e.getMessage());
            }
        }
    }
}
