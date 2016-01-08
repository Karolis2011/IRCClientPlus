package tk.kar_programing.ircclient;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import tk.kar_programing.ircclient.core.IRC.IRCClient;
import tk.kar_programing.ircclient.core.IRC.IRCPacket;
import tk.kar_programing.ircclient.core.IRC.utils.IRCCallBackRunnable;

public class Testing extends AppCompatActivity {
    private IRCClient testC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

    }
    public void TestIRC(View v){
        /*testC = new IRCClient("gaia.sorcery.net", 9999, true);
        testC.EnableInternalHandler();
        testC.addPacketCallback(new IRCCallBackRunnable() {
            @Override
            public void run(IRCPacket ircPacket) {
                AddToView(ircPacket.GetData());
            }
        });
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    testC.ircCore.Connect("Karol's", "Karolis", "Karolis");
                    Thread.sleep(100);
                    testC.ircCore.rawSend("JOIN #ircclient+test\r\n");
                    while(testC.ircCore.isConnected){
                        testC.ircCore.Run();
                    }
                } catch (Exception e) {
                    Log.e("TestingERR", e.toString());
                    e.printStackTrace();
                }
            }
        });
        t.start();
        v.setEnabled(false);
        findViewById(R.id.endTest).setEnabled(true); */
    }

    public void EndTest(View v){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testC.ircCore.Disconnect("Test is done!");
                } catch (Exception e){
                    e.printStackTrace();
                    Log.e("IRCTest", e.toString());
                }
            }
        });
        t.start();
        v.setEnabled(false);
        findViewById(R.id.testBtn).setEnabled(true);
    }

    public void SendInp(View v){
        EditText te = (EditText)findViewById(R.id.sendText);
        final String SendMsg = te.getText().toString();
        te.setText("");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    testC.ircCore.rawSend(SendMsg + "\r\n");
                } catch (Exception e){
                    Log.e("IRCErr", e.toString());
                }
            }
        });
        t.start();
    }

    private void AddToView(String s){
        EditText e = (EditText)findViewById(R.id.Output);
        e.getText().append(s).append("\r\n");
    }

}
