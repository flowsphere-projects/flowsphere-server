package com.flowsphere.server.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Accessors(chain = true)
@Entity(name = "t_cmd")
public class Cmd implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String cmd;

    @Column
    private String applicationName;

    @Column
    private String ip;

    /**
     * 0:待通知
     * 1:已通知
     */
    @Column
    private int status;

    @Column
    private LocalDateTime createTime;

    @Column
    private LocalDateTime lastUpdateTime;

    @Column
    private String extendData;

}
