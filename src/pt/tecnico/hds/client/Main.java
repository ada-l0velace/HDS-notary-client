package pt.tecnico.hds.client;

import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        /** Define a host server */
        String host = "localhost";
        /** Define a port */
        int port = 19999;

        StringBuffer instr = new StringBuffer();
        String TimeStamp;
        System.out.println("SocketClient initialized");
        try {
            /** Obtain an address object of the server */
            InetAddress address = InetAddress.getByName(host);
            /** Establish a socket connetion */
            Socket connection = new Socket(address, port);
            /** Instantiate a BufferedOutputStream object */
            BufferedOutputStream bos = new BufferedOutputStream(connection.
                    getOutputStream());

            /** Instantiate an OutputStreamWriter object with the optional character
             * encoding.
             */
            OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF8");
            TimeStamp = new java.util.Date().toString();
            String process = "Calling the Socket Server on "+ host + " port " + port +
                    " at " + TimeStamp +  (char) 13;

            /** Write across the socket connection and flush the buffer */
            osw.write(process);
            osw.flush();
            /** Instantiate a BufferedInputStream object for reading
             /** Instantiate a BufferedInputStream object for reading
             * incoming socket streams.
             */

            BufferedInputStream bis = new BufferedInputStream(connection.
                    getInputStream());
            /**Instantiate an InputStreamReader with the optional
             * character encoding.
             */

            InputStreamReader isr = new InputStreamReader(bis, "US-ASCII");

            /**Read the socket's InputStream and append to a StringBuffer */
            int c;
            while ( (c = isr.read()) != 13)
                instr.append( (char) c);

            /** Close the socket connection. */
            connection.close();
            System.out.println(instr);
        }
        catch (IOException f) {
            System.out.println("IOException: " + f);
        }
        catch (Exception g) {
            System.out.println("Exception: " + g);
        }
    }
}
