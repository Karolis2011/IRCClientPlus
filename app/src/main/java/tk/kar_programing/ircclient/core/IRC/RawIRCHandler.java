package tk.kar_programing.ircclient.core.IRC;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class RawIRCHandler extends Handler {
    private final IRCClient myIRC;

    public enum MessageType{
        UNKNOWN(0),
        IRC_PACKET(1);

        private final int _type;

        MessageType(int type){
            _type = type;
        }

        public int GetValue(){
            return _type;
        }

        public static MessageType fromInt(int integer){
            for(MessageType t : MessageType.values()){
                if(t.GetValue() == integer){
                    return t;
                }
            }
            return UNKNOWN;
        }
    }

    public RawIRCHandler(Looper mainLooper, IRCClient ircClient) {
        super(mainLooper);
        myIRC = ircClient;
    }

    @Override
    public void handleMessage(Message msg) {
        myIRC.DataReturn(MessageType.fromInt(msg.what), msg.obj);
    }
}
