package com.flowsphere.server.notify;

import com.flowsphere.server.entity.Cmd;

import java.util.List;

public interface NotificationListener {

    void handler(List<Cmd> cmdList);


}
