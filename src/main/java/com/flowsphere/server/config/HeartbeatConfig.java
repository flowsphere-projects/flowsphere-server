package com.flowsphere.server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HeartbeatProperties.class)
public class HeartbeatConfig {
}
