package com.flowsphere.server.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(name = "t_provider_instant")
public class ProviderInstant implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int providerId;

    @Column
    private String providerName;

    @Column
    private String ip;

    @Column
    private int status;

    @Column
    private LocalDateTime lastUpdateTime;

}
