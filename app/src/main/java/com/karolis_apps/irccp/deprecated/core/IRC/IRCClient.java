package com.karolis_apps.irccp.deprecated.core.IRC;


import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.karolis_apps.irccp.deprecated.core.IRC.Data.NetworkDetails;
import com.karolis_apps.irccp.deprecated.core.IRC.Data.ServerDetails;
import com.karolis_apps.irccp.deprecated.core.IRC.utils.IRCCallBackRunnable;

public class IRCClient {
    public final RawIRCClient ircCore;
    private final List<IRCCallBackRunnable> packetCallback;
    private NetworkDetails networkDetails;

    public IRCClient(NetworkDetails networkDetails, ServerDetails serverDetails){
        this(serverDetails.address, serverDetails.port, serverDetails.ssl);
        this.networkDetails = networkDetails;
    }

    private IRCClient(String host, int port, boolean ssl){
        packetCallback = new ArrayList<>();
        ircCore = new RawIRCClient(host, port, ssl);
        ircCore.addPacketCallback(new IRCCallBackRunnable() {
            @Override
            public void run(IRCPacket ircPacket) {
                for (IRCCallBackRunnable call: packetCallback) {
                    call.run(ircPacket);
                }
            }
        });
    }

    public void Connect() throws IOException, NoSuchAlgorithmException, KeyManagementException{
        this.ircCore.Connect(networkDetails.userDetails.nickname, networkDetails.userDetails.username, networkDetails.userDetails.realname);
    }

    public void addPacketCallback(IRCCallBackRunnable callback){
        packetCallback.add(callback);
    }
    public void clearPacketCallbacks(){
        packetCallback.clear();
    }

    public void PhraseCommand(String s, String invokingBuffer){
        String cmd = s.split(" ")[0].replace("/", "");
        String params = s.replaceFirst("(\\/)\\w+(\\ )*", "");
        switch (cmd.toLowerCase()){
            case "raw":
                safeRAWSend(params + "\r\n");
                return;
            case "me":
                safeRAWSend("PRIVMSG " + invokingBuffer + " :\u0001ACTION " + params + "\u0001\r\n");
                return;
            case "msg":
                safeRAWSend("PRIVMSG " + params + "\r\n");
                return;
            default:
                safeRAWSend(s.replaceFirst("/", "") + "\r\n");

        }
    }

    public boolean safeRAWSend(String rawLine){
        try {
            ircCore.rawSend(rawLine);
        } catch (Exception ex){
            return false;
        }
        return true;
    }
}
