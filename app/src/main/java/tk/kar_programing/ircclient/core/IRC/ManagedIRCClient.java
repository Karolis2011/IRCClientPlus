package tk.kar_programing.ircclient.core.IRC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tk.kar_programing.ircclient.core.IRC.Data.NetworkDetails;
import tk.kar_programing.ircclient.core.IRC.utils.BufferUpdateRunnable;
import tk.kar_programing.ircclient.core.IRC.utils.IRCCallBackRunnable;
import tk.kar_programing.ircclient.exceptions.GeneralException;

//NOTE: Can be merged with IRCClient in feature
public class ManagedIRCClient {
    public String name;
    public IRCClient unmanagedIRCCLient;
    public Map<String, String> ChannelBuffers;
    public Map<String, Integer> ChannelBufferUpdateTimeouts;
    public NetworkDetails designatedNetwork;
    private List<BufferUpdateRunnable> updatehandles;
    private Thread processingThread;
    private final int UpdateDelay = 50; //In ms
    private Timer timer;

    public ManagedIRCClient () {
        timer = new Timer("Update Count Down");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                UpdateTimerTick();
            }
        }, 0, 10);
        updatehandles = new ArrayList<>();
        ChannelBuffers = new android.support.v4.util.ArrayMap<>();
        ChannelBufferUpdateTimeouts = new android.support.v4.util.ArrayMap<>();
    }

    private void UpdateTimerTick(){
        for (Map.Entry<String, Integer> ent : ChannelBufferUpdateTimeouts.entrySet()) {
            Integer val = ent.getValue();
            String key = ent.getKey();
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

    public void SortAndHandlePacks(IRCPacket ircPacket) {
        String target = ircPacket.GetTarget();
        if(target == null){
            target = "General";
        }
        target = "General"; // Just for test sakes TODO: Remove this line when proper chanel showing will be present
        String buffer = ChannelBuffers.get(target);
        if(buffer == null){
            //Do buffer init stuff, like adding any special styles or scripts
            buffer = "";

        }
        if (ircPacket.GetType() != IRCPacket.Type.RAW){
            buffer += "<b>" + ircPacket.GetSouce() + "</b> ";
        }
        buffer += ircPacket.GetData();
        buffer += "<br>";
        ChannelBuffers.put(target, buffer);
        //Update invoke to just update stuff
        ChannelBufferUpdateTimeouts.put(target, UpdateDelay);
    }

    public void ClearHandles(){
        updatehandles.clear();
    }

    public void AddUpdateHandle(BufferUpdateRunnable callback){
        updatehandles.add(callback);
    }

    public List<String> GetAvavibleChannels(){
        List<String> tmp = new ArrayList<>();
        tmp.addAll(ChannelBuffers.keySet());
        return tmp;
    }

    public void ClearChannelBuffer(String channelName){
        //TODO: Implement
    }
}
