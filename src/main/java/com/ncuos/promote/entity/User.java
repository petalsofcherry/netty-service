package com.ncuos.promote.entity;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class User {
    private String username;
    private String department;
}
