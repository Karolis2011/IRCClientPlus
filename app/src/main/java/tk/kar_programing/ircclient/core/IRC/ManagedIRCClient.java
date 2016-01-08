package tk.kar_programing.ircclient.core.IRC;

import android.util.ArrayMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import tk.kar_programing.ircclient.core.IRC.Data.NetworkDetails;
import tk.kar_programing.ircclient.core.IRC.utils.IRCCallBackRunnable;
import tk.kar_programing.ircclient.exceptions.GeneralException;

//NOTE: Can be merged with IRCClient in feature
public class ManagedIRCClient {
    public String name;
    public IRCClient unmanagedIRCCLient;
    public Map<String, String> ChannelBuffers;
    public NetworkDetails designatedNetwork;
    private List<Runnable> updatehandles;
    private Thread processingThread;


    public ManagedIRCClient () {
        updatehandles = new ArrayList<>();
        ChannelBuffers = new android.support.v4.util.ArrayMap<>();
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
        target = "General"; // Just for test sakes
        String buffer = ChannelBuffers.get(target);
        if(buffer == null){
            //Do buffer init stuff
            buffer = "";

        }
        if (ircPacket.GetType() != IRCPacket.Type.RAW){
            buffer += "<b>" + ircPacket.GetSouce() + "</b> ";
        }
        buffer += ircPacket.GetData();
        buffer += "<br>";
        ChannelBuffers.put(target, buffer);
        //Update invoke to just update stuff
        for (Runnable r : updatehandles) {
            r.run();
        }
    }

    public void ClearHandles(){
        updatehandles.clear();
    }

    public void AddUpdateHandle(Runnable callback){
        updatehandles.add(callback);
    }



}
