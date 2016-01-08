package tk.kar_programing.ircclient.core.IRC.Data;


import java.net.InetAddress;

public class ServerDetails {
    public String name;
    public String address;
    public int port;
    public boolean ssl;

    public ServerDetails(String name, String address, int port, boolean ssl){
        this.name = name;
        this.address = address;
        this.port = port;
        this.ssl = ssl;
    }

    public ServerDetails(String name, String address, int port){
        this(name, address, port, false);
    }

    public boolean IsAvailable(){
        try{
            InetAddress address = InetAddress.getByName(this.address);
            if(address.isReachable(60)){
                return true;
            }
        } catch (Exception ex){
            return false;
        }
        return false;
    }
}
