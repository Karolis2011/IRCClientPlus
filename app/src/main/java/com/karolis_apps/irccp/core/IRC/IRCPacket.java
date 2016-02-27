package com.karolis_apps.irccp.core.IRC;

public class IRCPacket {
    private final Type type;
    private final String contents;
    private final String target;
    private final String source;
    public enum Type{
        RAW(0, "Raw"),
        PRIVMSG(1, "Private Message"),
        NOTICE(2, "Notice");

        public final int typeInt;
        public final String typeName;

        Type(int typeInteger, String typeFancyName){
            typeInt = typeInteger;
            typeName = typeFancyName;
        }
    }

    public IRCPacket(String RawLine){
        this(Type.RAW, RawLine, null, null);
    }
    public IRCPacket(Type type, String contents, String target, String source) {
        this.type = type;
        this.contents = contents;
        this.target = target;
        this.source = source;
    }
    public IRCPacket TryToPhraseSubType(){
        IRCPacket ircp;
        String[] split = this.contents.split(" "); //0 - Source, 1 - Type, 2 - Target
        if (split.length != 0 && split.length > 2 && split[0] != null && split[1] != null && split[2] != null){
            int infoLen = split[0].length() + split[1].length() + split[2].length() + 3;
            String cont = null;
            if(infoLen <= this.contents.length()){
                cont = this.contents.substring(infoLen);
            }
            switch (split[1]){
                case ("PRIVMSG"):
                    ircp = new IRCPacket(Type.PRIVMSG, cont.substring(1), split[2], split[0]);
                    return ircp;
                case ("NOTICE"):
                    ircp = new IRCPacket(Type.NOTICE, cont.substring(1), split[2], split[0]);
                    return ircp;
            }
        }
        return null;
    }
    public Type GetType(){
        return this.type;
    }

    public String GetTarget(){
        return this.target;
    }

    public String GetSource(){
        return this.source;
    }

    public String GetData(){
        return this.contents;
    }

}
