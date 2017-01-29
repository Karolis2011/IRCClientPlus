package com.karolis_apps.irccp.core;

public class ClientConfigurationManager {
    private static ClientConfigurationManager instance;

    public static ClientConfigurationManager getInstance(){
        if(instance == null){
            instance = new ClientConfigurationManager();
        }
        return instance;
    }

    
}
