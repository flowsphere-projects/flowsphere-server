package com.flowsphere.server.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ConsumerProviderStatusEnum {

    DELETED(0,"已删除"),
    NORMAL(1, "正常"),;

    private int status;

    private String desc;

}
