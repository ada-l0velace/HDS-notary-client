package pt.tecnico.hds.client.integration;


import org.junit.*;
import pt.tecnico.hds.client.HdsClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientServiceTest {


    private static HdsClient client;
    /**
     * Run once before each test class
     * @throws Exception
     */
    @BeforeClass
    public static void setUpBeforeAll() throws Exception {
        // run tests with a clean database!!!
        //client = new HdsClient("user"+"1", 3999+ Integer.parseInt("1"));
        //client.connectToServer("localhost", 19999);
    }

    /**
     * Run before each test
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

    }

    /**
     * Rollback after each test
     */
    @After
    public void tearDown() {

    }

    public String sendTo(String hostname, int port, String payload) {
        boolean sent = false;

        try {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName(hostname);

            // establish the connection with server port 5056
            Socket s = new Socket(ip, port);

            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(payload);

            String value = dis.readUTF();
            s.close();
            return value;
        } catch (UnknownHostException e) {
            // TODO
        } catch (IOException e) {
            // TODO
        }
        return "";
    }

    protected Socket createSocket() {
        return new Socket();
    }

    @Test
    public void invalidInputTest() {
        Assert.assertEquals("Invalid input", sendTo("localhost", 19999, "qweqweqwewq"));
    }


}
