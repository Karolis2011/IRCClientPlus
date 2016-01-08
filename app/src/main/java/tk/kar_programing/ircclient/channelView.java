package tk.kar_programing.ircclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import tk.kar_programing.ircclient.core.ClientManager;
import tk.kar_programing.ircclient.core.IRC.IRCPacket;
import tk.kar_programing.ircclient.core.IRC.ManagedIRCClient;
import tk.kar_programing.ircclient.core.IRC.utils.IRCCallBackRunnable;
import tk.kar_programing.ircclient.exceptions.GeneralException;

public class channelView extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_view);
        EditText inputText = (EditText) findViewById(R.id.inputBox);
        //Let's check if there is already
        ManagedIRCClient ircClient = ClientManager.getInstance().GetClientByName("Main");
        if(ircClient == null){
            //Let's create new client
            ircClient = ClientManager.getInstance().CreateTestClient("Main");
            try{
                ircClient.Connect();
            } catch (GeneralException e){
                e.printStackTrace();
            }

        } else {
            try {
                TextView v = (TextView) findViewById(R.id.chanOutput);
                Spanned sp = Html.fromHtml(ircClient.ChannelBuffers.get("General"));
                v.setText(sp);
            } catch (Exception ex){
                ex.printStackTrace();
            }

        }

        // Handling "Send" action
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean h = false;
                final String vv = v.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ClientManager.getInstance().GetClientByName("Main").unmanagedIRCCLient.PhraseCommand(vv + "\r\n");
                        }
                    });
                    t.start();
                    v.setText("");
                    h = true;
                }
                return h;
            }
        });
        ircClient.ClearHandles(); //To make sure we are only ones subscribed to this client
        ircClient.AddUpdateHandle(new Runnable() {
            @Override
            public void run() {
                //General
                ManagedIRCClient client = ClientManager.getInstance().GetClientByName("Main");
                TextView v = (TextView) findViewById(R.id.chanOutput);
                Spanned sp = Html.fromHtml(client.ChannelBuffers.get("General"));
                v.setText(sp);
        }
        });
        /*
        Old Code:
        ClientManager.getInstance().Init();
        TextView v = (TextView) findViewById(R.id.chanOutput);
        Spanned sp = Html.fromHtml(ClientManager.getInstance().htmlBuffer);
        v.setText(sp);

        if(savedInstanceState == null){
            //Let's add packet handler
            ClientManager.getInstance().ircClient.addPacketCallback(new IRCCallBackRunnable() {
                @Override
                public void run(IRCPacket ircPacket) {
                    TextView v = (TextView) findViewById(R.id.chanOutput);
                    ClientManager.getInstance().htmlBuffer += ircPacket.GetData() + "<br/>";
                    Spanned sp = Html.fromHtml(ClientManager.getInstance().htmlBuffer);
                    //v.setText(ircPacket.GetData());
                    v.setText(sp);
                }
            });
        }
        */

    }

}
