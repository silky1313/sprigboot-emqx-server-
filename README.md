## 1. 首先是docker安装emqx

```shell
docker pull emqx:latest
docker run --restart=always -d --name emqx -p 1883:1883 -p 8083:8083 -p 8084:8084 -p 8883:8883 -p 18083:18083 emqx:latest
# 如果需要使用网页控制台的websocket记得加上8083端口号。
```



## 2.然后就是springboo整合emqx。

基于[这个链接](https://juejin.cn/post/7172380395194253343)

然后大概把代码都看懂就行。主要使用的是其中的com.silky.server.utils.MqttUtil，com.silky.server.config.MessageCallback。里面可以订阅和发布消息。看你们组怎么安排前后端的任务。可能订阅和发布全是后端负责, 所以就要都会使用。

接下来讲讲连接的具体步骤。

### 2.1 首先是引入依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!--  mqtt的相关依赖 -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-mqtt</artifactId>
        <version>5.5.13</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-stream</artifactId>
        <version>5.5.13</version>
    </dependency>
    <!-- 监控执行sql语句的包 -->
    <dependency>
        <groupId>p6spy</groupId>
        <artifactId>p6spy</artifactId>
        <version>${p6spy.version}</version>
    </dependency>
    <!-- 持久层框架mybatis-plus，在idea里面下个插件名字就叫做MybatisPlus，可以生成三层代码 -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>${mybatis-plus.version}</version>
    </dependency>
    <!-- 接口调试框架knife4j，具体怎么使用，自行搜索 -->
    <dependency>
        <groupId>com.github.xiaoymin</groupId>
        <artifactId>knife4j-openapi2-spring-boot-starter</artifactId>
        <version>${knife4j.version}</version>
    </dependency>
    <!-- hutools依赖，静态方法包，具体怎么使用，自行搜索 -->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>${hutool.version}</version>
    </dependency>
</dependencies>
```

### 2.2然后是application.yml中配置配置mqtt

```
mqtt:
  open: true
  #MQTT-服务器连接地址，如果有多个，用逗号隔开
  host: tcp://192.168.228.100:1883
  #MQTT-连接服务器默认客户端ID
  clientId: mqtt_id
  #MQTT-用户名
  username: admin
  #MQTT-密码
  password: admin
  #MQTT的订阅主题，发布的时候可以自己自定义主题
  topic:
    - save
    - send
  #连接超时
  timeout: 2000
  #设置会话心跳时间
  keepalive: 100
```

### 2.3 然后是配置MqttConfig

```java
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


```

### 2.4然后写MessageCallback这个就是前面的回调函数类

```java
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


```

然后我们惊奇的发现这个类无法自动注入，产生了循环依赖，具体解决方案，https://zhuanlan.zhihu.com/p/638625895 看这篇文章。

我们采用的是延迟加载策略，对MessageCallback采用延迟加载策略。通过构造函数注入。

```java
public MessageCallback(@Lazy MqttClient client, @Lazy IDiffService diffService){
    this.client = client;
    this.diffService = diffService;
}
```

### 2.5 然后保存订阅`save`的消息

```java
@Override
public void messageArrived(String topic, MqttMessage message) throws Exception {
    log.info("接收消息主题 : " + topic);
    log.info("接收消息内容 : " + new String(message.getPayload()));
    if (topic.equals("save")) {
        save(new String(message.getPayload()));
    }
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
```

### 2.6 controller处理具体的逻辑

这个就看你们小组的了，我写了一个向订阅`send`发送消息的方法。在DiffController里面。



## 3.后续处理

我会把代码放在github上，如果出现bug直接提pr就行。

github：https://github.com/silky1313/sprigboot-emqx-server-





