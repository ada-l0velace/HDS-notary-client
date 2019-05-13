package pt.tecnico.hds.client;


import org.json.JSONObject;

public class AnswerThread extends Thread {
        public String auxS;
        private HdsClient client;
        private JSONObject request;
        private int index;

        public AnswerThread(int index, HdsClient client, JSONObject request) {
            this.index = index;
            this.client = client;
            this.request = request;
        }

        public void run(){
            auxS = client.connectToClient("localhost",
                    client._serverPort + index,
                    request);
        }


}
