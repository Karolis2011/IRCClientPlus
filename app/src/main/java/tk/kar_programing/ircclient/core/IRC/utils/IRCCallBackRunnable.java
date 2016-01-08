package tk.kar_programing.ircclient.core.IRC.utils;

import tk.kar_programing.ircclient.core.IRC.IRCPacket;

public interface IRCCallBackRunnable{
    void run(IRCPacket ircPacket);
}
