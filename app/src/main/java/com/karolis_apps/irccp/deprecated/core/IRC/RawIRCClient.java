package com.karolis_apps.irccp.deprecated.core.IRC;

import android.os.Build;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.*;

import com.karolis_apps.irccp.BuildConfig;
import com.karolis_apps.irccp.deprecated.core.IRC.SSL.IgnoreSSLTrustManager;
import com.karolis_apps.irccp.deprecated.core.IRC.utils.IRCCallBackRunnable;

/*
RawIRCClient is a class for handling basic IRC protocol,
 most of this class methods must be executed outside main thread
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
    public String currentNick;
    private final List<IRCCallBackRunnable> callBackRunnableList;

    public RawIRCClient(String host, int port, boolean SSL){
        this.returnHandler = null;
        this.port = port;
        this.hostname = host;
        this.sslEnable = SSL;
        this.callBackRunnableList = new ArrayList<>();
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
        currentNick = nickname;
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
        while (this.isConnected  && ((line = inC.readLine()) != null)) {
            //Uncontrolled responses
            if(Build.VERSION.SDK_INT >= 18){
                Trace.beginSection("ircMessageProcessing");
            }
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
            List<IRCPacket> packetsToDeliver = new ArrayList<>();
            IRCPacket p = new IRCPacket(line);
            packetsToDeliver.add(p);
            IRCPacket p2 = p.TryToPhraseSubType();
            if(p2 != null){
                packetsToDeliver.add(p2);
            }
            for (IRCPacket pack: packetsToDeliver) {
                for (IRCCallBackRunnable call: callBackRunnableList) {
                    call.run(pack);
                }
            }
            Log.d("IRCOut", line);
            if(Build.VERSION.SDK_INT >= 18){
                Trace.endSection();
            }
        }
    }
    @Deprecated
    public void AttachHandler(Handler handler){
        if(returnHandler == null){
            returnHandler = handler;
        }
    }

    public void addPacketCallback(IRCCallBackRunnable ircCallBackRunnable){
        this.callBackRunnableList.add(ircCallBackRunnable);
    }

    public void rawSend(String s) throws IOException{
        Log.d("IRCIn", s);
        outC.write(s);
        outC.flush();
        if(s.toLowerCase().startsWith("nick")){
            currentNick = s.substring(4).replace("\r\n", "").trim();
        }
    }

    public void Disconnect(String msg) throws IOException{
        rawSend("QUIT :"+ msg + "\r\n");
        coreSock.close();
        isConnected = false;
    }

}
