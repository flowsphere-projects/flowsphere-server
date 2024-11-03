CREATE DATABASE `flowsphere` /*!40100 DEFAULT CHARACTER SET utf8 */;

-- flowsphere.t_consumer definition

CREATE TABLE `t_consumer`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`          varchar(50) NOT NULL DEFAULT '' COMMENT '消费者应用名',
    `provider_name` varchar(50) NOT NULL DEFAULT '' COMMENT '服务提供者应用名',
    `status`        int(11) NOT NULL COMMENT '状态',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


-- flowsphere.t_consumer_instant definition

CREATE TABLE `t_consumer_instant` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `consumer_id` int(11) NOT NULL COMMENT '消费者服务ID',
                                      `url` varchar(100) NOT NULL DEFAULT '' COMMENT 'url',
                                      `last_update_time` datetime NOT NULL COMMENT '最后更新时间',
                                      `status` int(11) NOT NULL COMMENT '状态',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;



-- flowsphere.t_provider definition

CREATE TABLE `t_provider` (
                              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `name` varchar(50) NOT NULL DEFAULT '' COMMENT '服务提供者应用名',
                              `status` int(11) NOT NULL COMMENT '状态',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;


-- flowsphere.t_provider_function definition

CREATE TABLE `t_provider_function` (
                                       `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `provider_id` int(11) NOT NULL COMMENT '服务提供者应用ID',
                                       `provider_name` varchar(100) NOT NULL DEFAULT '' COMMENT '服务提供者应用名',
                                       `url` varchar(100) NOT NULL DEFAULT '' COMMENT 'url',
                                       `last_update_time` datetime NOT NULL COMMENT '最后更新时间',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;

-- flowsphere.t_provider_instant definition

CREATE TABLE `t_provider_instant` (
                                      `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `provider_id` int(11) NOT NULL COMMENT '服务提供者应用ID',
                                      `provider_name` varchar(100) NOT NULL DEFAULT '' COMMENT '服务提供者应用名',
                                      `ip` varchar(20) NOT NULL DEFAULT '' COMMENT 'ip地址',
                                      `status` int(11) NOT NULL COMMENT '状态',
                                      `last_update_time` datetime NOT NULL COMMENT '最后更新时间',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8;