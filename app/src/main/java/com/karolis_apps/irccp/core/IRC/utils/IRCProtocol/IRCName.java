package com.karolis_apps.irccp.core.IRC.utils.IRCProtocol;


import java.util.regex.Pattern;

public class IRCName {
    public static final int[] nickColours = {4,5,6,10,11,12,13};
    public final String nickname;
    public final String username;
    public final String host;

    IRCName (String nick, String user, String host){
        this.nickname = nick;
        this.username = user;
        this.host = host;
    }

    public String getProperName() {
        if(nickname != null) {
            return nickname;
        }
        return host;
    }

    public static IRCName PhraseName(String name){
        name = name.replace(":", ""); //Removes ':' at start
        String[] splitup = name.split("[!@]");
        if(splitup.length < 3) {
            return new IRCName(null, null, splitup[0]);
        }
        return new IRCName(splitup[0],splitup[1],splitup[2]);
    }

    public int getNickColour(){
        int hash = getProperName().hashCode();
        hash &= 0x7FFFFFFF;
        return nickColours[(hash % nickColours.length) - 1];
    }
}
