package com.ncuos.promote.session;

import io.netty.channel.Channel;
import lombok.Data;

@Data
public class Session {
    private String id;
    private Channel channel = null;
    private long lastCommunicateTimeStamp = 0L;

    public static Session buildSession(Channel channel) {
        Session session = new Session();
        session.setChannel(channel);

        session.setId(channel.id().asLongText());
        session.setLastCommunicateTimeStamp(System.currentTimeMillis());
        return session;
    }
}