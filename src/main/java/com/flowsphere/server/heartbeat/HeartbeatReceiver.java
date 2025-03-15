package com.flowsphere.server.heartbeat;

public interface HeartbeatReceiver {

    void receive(String applicationName, String ip, int port);

}
