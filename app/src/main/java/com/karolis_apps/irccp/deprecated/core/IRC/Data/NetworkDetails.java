package com.karolis_apps.irccp.deprecated.core.IRC.Data;


import java.util.ArrayList;
import java.util.List;

import com.karolis_apps.irccp.deprecated.exceptions.GeneralException;

public class NetworkDetails {
    public String networkName;
    public final List<ServerDetails> serverDetailsList;
    public UserDetails userDetails;

    public NetworkDetails(){
        serverDetailsList = new ArrayList<>();
    }

    public ServerDetails GetAvailableServer() throws GeneralException {
        for(ServerDetails details : serverDetailsList){
            /*if(details.IsAvailable()){
                return details;
            }*/
            return details;
        }
        throw new GeneralException("No server available");
    }
}
