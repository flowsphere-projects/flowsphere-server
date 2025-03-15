package com.flowsphere.server.service;

import com.flowsphere.server.entity.Cmd;
import com.flowsphere.server.entity.ProviderInstance;
import com.flowsphere.server.enums.CmdEnum;
import com.flowsphere.server.enums.CmdStatusEnum;
import com.flowsphere.server.repository.CmdRepository;
import com.flowsphere.server.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CmdService {

    @Autowired
    private CmdRepository cmdRepository;

    public void batchSave(List<ProviderInstance> providerInstantList, String applicationName) {
        List<Cmd> cmdList = new ArrayList<>();
        for (ProviderInstance providerInstant : providerInstantList) {
            Cmd cmd = new Cmd();
            cmd.setCmd(CmdEnum.PROVIDER_OFFLINE.getCmd());
            cmd.setApplicationName(providerInstant.getProviderName());
            cmd.setIp(providerInstant.getIp());
            cmd.setStatus(CmdStatusEnum.NOT_NOTIFY.getStatus());
            cmd.setCreateTime(LocalDateTime.now());
            cmd.setLastUpdateTime(LocalDateTime.now());
            Map<String, String> extendData = new HashMap<>();
            extendData.put("applicationName", applicationName);
            cmd.setExtendData(JacksonUtils.toJson(extendData));
            cmdList.add(cmd);
        }
        cmdRepository.saveAll(cmdList);
    }

    public void updateComplete(Cmd cmd) {
        cmd.setLastUpdateTime(LocalDateTime.now());
        cmd.setStatus(CmdStatusEnum.NOTIFY_COMPLETE.getStatus());
        cmdRepository.save(cmd);
        log.info("命名id={} 已完成更新", cmd.getId());
    }

}
