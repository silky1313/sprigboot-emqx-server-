package com.silky.server.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.silky.server.domain.po.Diff;
import com.silky.server.service.IDiffService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * consumer 消费者，接受消息
 */
@Component
@Slf4j
public class MessageCallback implements MqttCallback {
    private final MqttClient client;

    private final IDiffService diffService;

    public MessageCallback(@Lazy MqttClient client, @Lazy IDiffService diffService){
        this.client = client;
        this.diffService = diffService;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        if (client == null || !client.isConnected()) {
            log.info("连接断开，正在重连....");
            try {
                client.reconnect();
                if(client.isConnected()){
                    log.info("mqtt重连完成.");
                }
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("接收消息主题 : " + topic);
        log.info("接收消息内容 : " + new String(message.getPayload()));
        if (topic.equals("save")) {
            save(new String(message.getPayload()));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("deliveryComplete---------" + token.isComplete());
    }

    //{ "status":"ok","data":[{"imei":"shdhello","humi":"35","temp":"58"}] }
    private void save(String message) {
        JSONObject jsonObject = JSONUtil.parseObj(message);
        Diff diff = Diff.builder()
                .imei(jsonObject.getByPath("data[0].imei").toString())
                .humi(jsonObject.getByPath("data[0].temp").toString())
                .temp(jsonObject.getByPath("data[0].temp").toString())
                .time(LocalDateTime.now())
                .build();
        diffService.saveOne(diff);
    }

}

