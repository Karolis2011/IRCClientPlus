package com.karolis_apps.irccp.core.IRC.utils;

import com.karolis_apps.irccp.core.IRC.IRCPacket;

public interface IRCCallBackRunnable{
    void run(IRCPacket ircPacket);
}
