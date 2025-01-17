package com.flowsphere.server.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(name = "t_consumer_provider")
public class ConsumerProvider implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int consumerId;

    @Column
    private int providerId;

    @Column
    private String providerIp;

    @Column
    private int status;

    @Column
    private LocalDateTime lastUpdateTime;

}
