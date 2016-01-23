package tk.kar_programing.ircclient.core.IRC;


import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import tk.kar_programing.ircclient.core.IRC.Data.NetworkDetails;
import tk.kar_programing.ircclient.core.IRC.Data.ServerDetails;
import tk.kar_programing.ircclient.core.IRC.utils.IRCCallBackRunnable;

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
    }

    public void Connect() throws IOException, NoSuchAlgorithmException, KeyManagementException{
        this.ircCore.Connect(networkDetails.userDetails.nickname,networkDetails.userDetails.username,networkDetails.userDetails.realname);
    }

    public void EnableInternalHandler(){
        ircCore.AttachHandler(GetHandler());
    }

    public void addPacketCallback(IRCCallBackRunnable callback){
        packetCallback.add(callback);
    }
    public void clearPacketCallbacks(){
        packetCallback.clear();
    }

    public void DataReturn(RawIRCHandler.MessageType messageType, Object object){
        switch (messageType){
            case UNKNOWN:
                break;
            case IRC_PACKET:
                IRCPacket p = (IRCPacket) object;
                for(IRCCallBackRunnable r : packetCallback){
                    r.run(p);
                }
                break;
        }
    }

    public void PhraseCommand(String s){
        try {
            ircCore.rawSend(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Handler GetHandler(){
        return new RawIRCHandler(Looper.getMainLooper(), this);
    }

}
