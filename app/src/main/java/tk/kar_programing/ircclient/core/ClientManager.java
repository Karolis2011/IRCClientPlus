package tk.kar_programing.ircclient.core;

import java.util.HashMap;
import java.util.Map;

import tk.kar_programing.ircclient.core.IRC.Data.UserDetails;
import tk.kar_programing.ircclient.core.IRC.ManagedIRCClient;
import tk.kar_programing.ircclient.core.IRC.Data.NetworkDetails;
import tk.kar_programing.ircclient.core.IRC.Data.ServerDetails;
import tk.kar_programing.ircclient.exceptions.GeneralException;

public class ClientManager {
    private static ClientManager instance;
    private final Map<String, ManagedIRCClient> ircClientMap;

    public static ClientManager getInstance(){
        if(instance == null){
            instance = new ClientManager();
        }
        return instance;
    }

    private ClientManager (){
        ircClientMap = new HashMap<>();
    }

    public ManagedIRCClient NewClient(String name, NetworkDetails networkDetails) throws GeneralException {
        if(ircClientMap.containsKey(name)){
            throw new GeneralException("There is already a client with same name");
        }
        ManagedIRCClient tmp = new ManagedIRCClient();
        tmp.name = name;
        tmp.designatedNetwork = networkDetails;
        ircClientMap.put(name, tmp);
        return tmp;
    }

    public ManagedIRCClient CreateTestClient(String key) {
        NetworkDetails nd =  new NetworkDetails();
        UserDetails ud = new UserDetails("IRCCPTester", "IRCCPTester", "IRCCPTester");
        ServerDetails sd = new ServerDetails("Circe", "circe.sorcery.net", 6665);
        nd.serverDetailsList.add(sd);
        nd.userDetails = ud;
        ManagedIRCClient mc = null;
        try{
            mc = NewClient(key, nd);
        } catch (Exception E){
            E.printStackTrace();
        }
        return mc;
    }

    public ManagedIRCClient GetClientByName(String name){
        return ircClientMap.get(name);
    }


}
