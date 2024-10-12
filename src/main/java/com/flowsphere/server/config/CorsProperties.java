package com.flowsphere.server.config;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties("flowsphere.server.cors")
public class CorsProperties {


    private List<String> allowOrigins = Lists.newArrayList("*");
    private List<String> allowHeaders = Lists.newArrayList("*");
    private List<String> allowMethods = Lists.newArrayList("*");
    private List<String> allowExposeHeaders;

    private String path;

}
