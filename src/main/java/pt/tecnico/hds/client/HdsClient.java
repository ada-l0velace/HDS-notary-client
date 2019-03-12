package pt.tecnico.hds.client;
import org.json.JSONObject;

public class HdsClient {
    private String _name;
    private int _port;

    HdsClient(String name, int port) {
        _name = name;
        _port = port;
    }

    private JSONObject actionGoodSeller(String command, String s) {
        String [] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 2) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put(s, _name);
        }
        else {
            jo.put("Action", "Invalid command");
        }
        return jo;
    }

    private JSONObject intentionToSell(String command) {
        return actionGoodSeller(command, "Seller");
    }

    private JSONObject getStateOfGood(String command) {
        return actionGoodSeller(command, "Buyer");
    }

    private JSONObject buyGood(String command) {
        return actionGoodSeller(command, "Buyer");
    }

    private JSONObject transferGood(String command) {
        String [] cmds = command.split(" ");
        JSONObject jo = new JSONObject();
        if (cmds.length == 3) {
            jo.put("Action", cmds[0]);
            jo.put("Good", cmds[1]);
            jo.put("Seller", _name);
            jo.put("Buyer", cmds[2]);
        }
        else {
            jo.put("Action", "Invalid command");
        }
        return jo;
    }

    public String sendJson(String command) {
        if (command.startsWith("transferGood")) {
           return transferGood(command).toString();
        }
        else if (command.startsWith("intentionToSell")) {
            return intentionToSell(command).toString();
        }
        else if (command.startsWith("getStateOfGood")) {
            return getStateOfGood(command).toString();
        }
        else if (command.startsWith("buyGood")) {
            return buyGood(command).toString();
        }
        return "";
    }
}
