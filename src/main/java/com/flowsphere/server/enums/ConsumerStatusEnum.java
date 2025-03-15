package com.flowsphere.server.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsumerStatusEnum {

    NORMAL(1, "正常"),;

    private int status;

    private String desc;

}
