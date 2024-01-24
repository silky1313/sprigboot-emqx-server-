package com.silky.server.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

//MQTT订阅
@Slf4j
@Configuration
@ConfigurationProperties("mqtt")
@Data
@Component
public class MqttConfig {

    String host;
    String clientId;
    List<String> topic;
    String username;
    String password;
    Integer timeout;
    Integer keepalive;

    @Autowired
    MessageCallback messageCallback;

    @Bean
    @ConditionalOnProperty(prefix = "mqtt",name="open",havingValue = "true")
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setConnectionTimeout(timeout);
        options.setKeepAliveInterval(keepalive);
        return options;
    }

    //基于这个bean做连接就行
    @Bean("mqttClient")
    @ConditionalOnProperty(prefix = "mqtt",name="open",havingValue = "true")
    public MqttClient mqttClient(MqttConnectOptions mqttConnectOptions) {
        try {
            MqttClient client = new MqttClient(host, clientId);
            /*设置回调函数，和js的概念差不多，就是如果接受到消息就调用回调函数*/
            client.setCallback(messageCallback);
            IMqttToken iMqttToken = client.connectWithResult(mqttConnectOptions);
            boolean complete = iMqttToken.isComplete();
            log.info("mqtt建立连接：{}", complete);

            // 订阅主题
            topic.stream().forEach(topic -> {
                try {
                    client.subscribe(topic, 0);
                    log.info("已订阅topic：{}", topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return client;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("mqtt 连接异常");
        }
    }
}

