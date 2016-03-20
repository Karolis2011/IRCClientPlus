package com.karolis_apps.irccp.core.IRC;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Trace;
import android.support.annotation.Nullable;

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
import com.karolis_apps.irccp.core.IRC.utils.IRCCallBackRunnable;
import com.karolis_apps.irccp.core.IRC.utils.IRCProtocol.Colours;
import com.karolis_apps.irccp.core.IRC.utils.IRCProtocol.IRCName;
import com.karolis_apps.irccp.exceptions.GeneralException;

import org.apache.commons.lang.StringEscapeUtils;

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
        if(Build.VERSION.SDK_INT >= 18){
            Trace.beginSection("bufferAllUpdate");
        }
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
        if(Build.VERSION.SDK_INT >= 18){
            Trace.endSection();
        }
    }

    public void Connect() throws GeneralException{
        unmanagedIRCCLient = new IRCClient(designatedNetwork, designatedNetwork.GetAvailableServer());

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
        if(Build.VERSION.SDK_INT >= 18){
            Trace.beginSection("sortAndHandlePacks");
        }
        String target = ircPacket.GetTarget();
        if(target == null){
            target = "!General";
        }
        if(target.equals(unmanagedIRCCLient.ircCore.currentNick) && (ircPacket.GetType() == IRCPacket.Type.PRIVMSG || ircPacket.GetType() == IRCPacket.Type.NOTICE)) {
            target = IRCName.PhraseName(ircPacket.GetSource()).getProperName();
        }
        if(Build.VERSION.SDK_INT >= 18){
            Trace.beginSection("bufferInit");
        }
        String buffer = ChannelBuffers.get(target);
        if(buffer == null){
            //Do buffer init stuff, like calling new buffer callbacks
            buffer = Colours.ITALICS + "Buffer with " + Colours.BOLD + target + Colours.BOLD + " opened" + Colours.ITALICS + "\r\n";
            ChannelBuffers.put(target, buffer); //We have to but new buffer with data NOW!
            Handler h = new Handler(Looper.getMainLooper());
            for (final BufferUpdateRunnable call: newbuffercalbacks) {
                final String tmpTarget = target;
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        call.run(tmpTarget);
                    }
                });
            }
        }
        if(Build.VERSION.SDK_INT >= 18){
            Trace.endSection();
            Trace.beginSection("namePhrase");
        }
        if (ircPacket.GetType() != IRCPacket.Type.RAW){
            buffer += "<" + Colours.BOLD + IRCName.PhraseName(ircPacket.GetSource()).getProperName()+ Colours.BOLD + "> ";
        }
        if(Build.VERSION.SDK_INT >= 18){
            Trace.endSection();
            Trace.beginSection("HTML escape");
        }
        buffer += ircPacket.GetData();
        buffer += Colours.RESET + "\r\n";
        if(Build.VERSION.SDK_INT >= 18){
            Trace.endSection();
        }
        ChannelBuffers.put(target, buffer);
        //Update invoke to just update stuff
        ChannelBufferUpdateTimeouts.put(target, UpdateDelay);
        if(Build.VERSION.SDK_INT >= 18){
            Trace.endSection();
        }
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
                unmanagedIRCCLient.safeRAWSend("PRIVMSG " + invokingBuffer + " :" + input + Colours.RESET + "\r\n");
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
                unmanagedIRCCLient.ircCore.Disconnect("IRCClientPlus " + BuildConfig.VERSION_NAME);
            } else {
                unmanagedIRCCLient.ircCore.Disconnect(message);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ClientManager.getInstance().DropClient(this.name);
    }

}
