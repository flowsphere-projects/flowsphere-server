package com.flowsphere.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CmdStatusEnum {

    NOT_NOTIFY(0, "未通知"),
    NOTIFY_COMPLETE(1, "已通知"),;

    private int status;

    private String desc;


}
