package pt.tecnico.hds.client;

public class Main {

    public static Boolean debug = true;
    public static int f =  1;
    public static int replicas =  3*f+1;
    public static void main(String[] args) {
        if(args.length == 0) {
            HdsClient client = new HdsClient("user1", 3999 + 1);
            client.runCommands();
        }
        else if (Integer.parseInt(args[0]) <= 10){
            HdsClient client = new HdsClient("user" + args[0], 3999 + Integer.parseInt(args[0]));
            client.runCommands();
        }
    }
}


