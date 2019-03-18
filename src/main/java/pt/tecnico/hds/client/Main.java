package pt.tecnico.hds.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        if(args.length == 0) {
            HdsClient client = new HdsClient("user1", 3999 + 1);
            client.connectToServer("localhost", 19999);
        }
        else{
            HdsClient client = new HdsClient("user" + args[0], 3999 + Integer.parseInt(args[0]));
            client.connectToServer("localhost", 19999);
        }
    }
}
