package com.silky.server.service.impl;

import com.silky.server.domain.po.Diff;
import com.silky.server.domain.mapper.DiffMapper;
import com.silky.server.service.IDiffService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.silky.server.utils.MqttUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author silky
 * @since 2024-01-23
 */
@Service("diffService")
@Slf4j
public class DiffServiceImpl extends ServiceImpl<DiffMapper, Diff> implements IDiffService {

    @Autowired
    MqttUtil mqttUtil;

    @Override
    public boolean saveOne(Diff diff) {
        diff.setTime(LocalDateTime.now());
        return save(diff);
    }

    @Override
    public boolean send(String message) {
        return  mqttUtil.publish("send", message);
    }
}
