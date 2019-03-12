package pt.tecnico.hds.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    static HdsClient  client;
    public static void main(String[] args) {
        client = new HdsClient("user"+args[0], 3999+ Integer.parseInt(args[0]));
        client.connectToServer("localhost", 19999);
    }
}
