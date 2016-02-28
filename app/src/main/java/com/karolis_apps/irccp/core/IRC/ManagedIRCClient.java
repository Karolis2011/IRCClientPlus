package com.karolis_apps.irccp.core.IRC;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.karolis_apps.irccp.BuildConfig;
import com.karolis_apps.irccp.core.ClientManager;
import com.karolis_apps.irccp.core.IRC.Data.NetworkDetails;
import com.karolis_apps.irccp.core.IRC.utils.BufferUpdateRunnable;
import com.karolis_apps.irccp.core.IRC.utils.HtmlUtils;
import com.karolis_apps.irccp.core.IRC.utils.IRCCallBackRunnable;
import com.karolis_apps.irccp.core.IRC.utils.IRCProtocol.IRCName;
import com.karolis_apps.irccp.exceptions.GeneralException;

//NOTE: Can be merged with IRCClient in feature
public class ManagedIRCClient {
    public String name;
    public IRCClient unmanagedIRCCLient;
    public final Map<String, String> ChannelBuffers;
    private final Map<String, Integer> ChannelBufferUpdateTimeouts;
    public NetworkDetails designatedNetwork;
    private final List<BufferUpdateRunnable> updatehandles;
    private final List<BufferUpdateRunnable> newbuffercalbacks;
    private Thread processingThread;
    private final int UpdateDelay = 50; //In ms
    private final Timer timer;

    public ManagedIRCClient () {
        timer = new Timer("Update Count Down");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                UpdateTimerTick();
            }
        }, 0, 10);
        updatehandles = new ArrayList<>();
        newbuffercalbacks = new ArrayList<>();
        ChannelBuffers = new android.support.v4.util.ArrayMap<>();
        ChannelBufferUpdateTimeouts = new android.support.v4.util.ArrayMap<>();
    }

    private void UpdateTimerTick(){
        for (Map.Entry<String, Integer> ent : ChannelBufferUpdateTimeouts.entrySet()) {
            Integer val = ent.getValue();
            String key = ent.getKey();
            if(key != null && val != null){
                if (val <= 0){
                    for (BufferUpdateRunnable callback : updatehandles) {
                        callback.run(key);
                    }
                    ChannelBufferUpdateTimeouts.remove(key);
                } else {
                    ChannelBufferUpdateTimeouts.put(key, val - 10);
                }
            }

        }
    }

    public void Connect() throws GeneralException{
        unmanagedIRCCLient = new IRCClient(designatedNetwork, designatedNetwork.GetAvailableServer());
        unmanagedIRCCLient.EnableInternalHandler();

        processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    unmanagedIRCCLient.Connect();
                } catch (Exception ex){
                    //TODO: Handle connect Exception
                }
                try {
                    while (unmanagedIRCCLient.ircCore.isConnected){
                        unmanagedIRCCLient.ircCore.Run();
                    }
                } catch (IOException ex){
                    //TODO: Handle read exception
                }

            }
        });
        processingThread.start();
        unmanagedIRCCLient.addPacketCallback(new IRCCallBackRunnable() {
            @Override
            public void run(IRCPacket ircPacket) {
                SortAndHandlePacks(ircPacket);
            }
        });
    }

    private void SortAndHandlePacks(IRCPacket ircPacket) {
        String target = ircPacket.GetTarget();
        if(target == null){
            target = "!General";
        }
        if(target.equals(unmanagedIRCCLient.ircCore.currentNick) && (ircPacket.GetType() == IRCPacket.Type.PRIVMSG || ircPacket.GetType() == IRCPacket.Type.NOTICE)) {
            target = IRCName.PhraseName(ircPacket.GetSource()).getProperName();
        }
        Log.d("SortAndHandlePacks", "Target: " + target + " CurrentNick: " + unmanagedIRCCLient.ircCore.currentNick);
        //target = "General"; // Just for test sakes TODO: Remove this line when proper chanel showing will be present
        String buffer = ChannelBuffers.get(target);
        if(buffer == null){
            //Do buffer init stuff, like calling new buffer callbacks
            buffer = "<i>Buffer with <b>" + target +"</b> opened</i><br>";
            ChannelBuffers.put(target, buffer); //We have to but new buffer with data NOW!
            for (BufferUpdateRunnable call: newbuffercalbacks) {
                call.run(target);
            }
        }
        if (ircPacket.GetType() != IRCPacket.Type.RAW){
            buffer += "&#60;<b>" + IRCName.PhraseName(ircPacket.GetSource()).getProperName() + "</b>&#62; ";
        }
        buffer += HtmlUtils.escapeHtml(ircPacket.GetData());
        buffer += "<br>";
        ChannelBuffers.put(target, buffer);
        //Update invoke to just update stuff
        ChannelBufferUpdateTimeouts.put(target, UpdateDelay);
    }

    public void ClearHandles(){
        updatehandles.clear();
        newbuffercalbacks.clear();
    }

    public void AddUpdateHandle(BufferUpdateRunnable callback){
        updatehandles.add(callback);
    }

    public void AddNewBufferCallback(BufferUpdateRunnable callback) {
        newbuffercalbacks.add(callback);
    }

    public List<String> GetAvailableChannels(){
        List<String> tmp = new ArrayList<>();
        tmp.addAll(ChannelBuffers.keySet());
        return tmp;
    }

    public void PhraseInput(String input, String invokingBuffer){
        if(input.startsWith("/")){
            unmanagedIRCCLient.PhraseCommand(input, invokingBuffer);
        } else {
            if(invokingBuffer != "!General") {
                unmanagedIRCCLient.safeRAWSend("PRIVMSG " + invokingBuffer + " :" + input);
                IRCPacket phantomPacket = new IRCPacket(IRCPacket.Type.PRIVMSG, input, invokingBuffer, unmanagedIRCCLient.ircCore.currentNick);
                SortAndHandlePacks(phantomPacket);

            } else {
                unmanagedIRCCLient.PhraseCommand("/" + input, invokingBuffer);
            }
        }
    }

    public void Disconnect(@Nullable String message){
        try{
            if(message == null){
                unmanagedIRCCLient.ircCore.Disconnect("IRCClieantPlus " + BuildConfig.VERSION_NAME);
            } else {
                unmanagedIRCCLient.ircCore.Disconnect(message);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ClientManager.getInstance().DropClient(this.name);
    }

}
