package tk.kar_programing.ircclient.core.IRC;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.net.ssl.*;

import tk.kar_programing.ircclient.BuildConfig;
import tk.kar_programing.ircclient.core.IRC.SSL.IgnoreSSLTrustManager;

/*
RawIRCClient is a class ofr handling basic IRC protocol
 */
public class RawIRCClient {

    private Socket coreSock;
    private BufferedWriter outC;
    private BufferedReader inC;
    private Handler returnHandler;
    private final String hostname;
    private final int port;
    private final boolean sslEnable;
    public Boolean isConnected = false;

    public RawIRCClient(String host, int port, boolean SSL){
        this.returnHandler = null;
        this.port = port;
        this.hostname = host;
        this.sslEnable = SSL;
    }

    public void Connect(String nickname, String username, String realname) throws IOException, NoSuchAlgorithmException, KeyManagementException{
        if (sslEnable) {
            //A mess for SSL support
            coreSock = new Socket(hostname, port);
            TrustManager[] trust = {new IgnoreSSLTrustManager()};
            SSLContext ssC = SSLContext.getInstance("TLS");
            ssC.init(null, trust, null);
            SSLSocket sslc = (SSLSocket) ssC.getSocketFactory().createSocket(coreSock, hostname, port, false);
            sslc.startHandshake();
            coreSock = sslc;
        } else {
            coreSock = new Socket(hostname, port);
        }
        //Init Writing and reading buffers
        outC = new BufferedWriter(new OutputStreamWriter(coreSock.getOutputStream()));
        inC = new BufferedReader(new InputStreamReader(coreSock.getInputStream()));
        //Send Out nick
        //TODO: Place to the other function
        outC.write("NICK " + nickname + "\r\n");
        outC.write("USER " + username + " 0 * " + realname + "\r\n");
        outC.flush();
        this.isConnected = true;
        //TODO: Move to Run()
        /*String line = null;
        while ((line = inC.readLine( )) != null) { //Success!
            if (line.contains("004")) {
                this.isConnected = true;
                break;
            }
            else if (line.contains("433")) { //Nick is in use
                Log.e("IRCErr","Username is used");
                return;
            }
        }*/
    }

    public void Run() throws IOException{
        String line;
        while (((line = inC.readLine()) != null) && this.isConnected) {
            //Uncontrolled responses
            if (line.toLowerCase( ).startsWith("ping ")) {
                rawSend("PONG " + line.substring(5) + "\r\n");
            }
            //Case for CTCP version
            if(line.contains("\u0001")) {
                String[] split = line.split(" ");
                if (split[3].equals(":\u0001VERSION\u0001")) {
                    Log.w("CTCP", "Got CTCP from " + split[0]);
                    rawSend("NOTICE " + split[0].replace(":", "").split("!")[0] + " \u0001VERSION IRC Client+ version " + BuildConfig.VERSION_NAME + " by Karolis on Android " + Build.VERSION.RELEASE + " SDK: " + Build.VERSION.SDK_INT + "\u0001\r\n");
                }
            }
            if(returnHandler != null){
                IRCPacket p = new IRCPacket(line);
                Message m = returnHandler.obtainMessage(RawIRCHandler.MessageType.IRC_PACKET.GetValue(), p);
                m.sendToTarget();
                IRCPacket p2 = p.TryToPhraseSubType();
                if(p2 != null){
                    m = returnHandler.obtainMessage(RawIRCHandler.MessageType.IRC_PACKET.GetValue(), p2);
                    m.sendToTarget();
                }
            }
            Log.d("IRCOut", line);
        }
    }

    public void AttachHandler(Handler handler){
        if(returnHandler == null){
            returnHandler = handler;
        }
    }

    public void rawSend(String s) throws IOException{
        Log.d("IRCIn", s);
        outC.write(s);
        outC.flush();
    }

    public void Send(String target, String message) throws Exception{
        rawSend("PRIVMSG " + target + " " + message + "\r\n");
    }

    public void Disconnect(String msg) throws IOException{
        rawSend("QUIT :"+ msg + "\r\n");
        coreSock.close();
        isConnected = false;
    }

}
