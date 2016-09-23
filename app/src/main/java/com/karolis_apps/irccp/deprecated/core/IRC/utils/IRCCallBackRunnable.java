package com.karolis_apps.irccp.deprecated.core.IRC.utils;

import com.karolis_apps.irccp.deprecated.core.IRC.IRCPacket;

public interface IRCCallBackRunnable{
    void run(IRCPacket ircPacket);
}
