## 结构

Server side 构成

- broker (mqtt核心：用于消息的发送管理)
- Application Server用于处理RestFul的请求，转发为Mqtt消息
  - Publisher **本质是Mqtt client**用于发布server端消息
  - Subscriber **本质是Mqtt client**用于订阅client端消息，并显示
- Client side
  - Publisher用于发布client端消息
  - Subscriber用于订阅server端的消息
  - Client 用于发送RestFul 请求给Application Server触发消息pub/sub

**总结**：从结构上Broker算是Mqtt的本质上的Server端，从业务上讲封装了Mqtt Client pub/sub的Application server和Broker共同构成了业务上的Server端

### 构建springboot项目

#### 1. 使用idea springboot initializer 初始化springboot工程

使用springboot版本**2.1.5.RELEASE**

#### 2. pom中添加

```xml

<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-stream</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-mqtt</artifactId>
</dependency>

```

#### 3. MQTT Configuration

* 配置broker地址，
* 端口号，
* 是否使用ssl，
* 用户名
* 密码

~~~java
public abstract class MQTTConfig {

    protected String ip = "127.0.0.1";

    /**
     * qos0 对于client而言，有且仅发一次publish包，对于broker而言，有且仅发一次publish，简而言之，就是仅发一次包，是否收到完全不管，适合那些不是很重要的数据。
     * qos1 这个交互就是多了一次ack的作用，但是会有个问题，尽管我们可以通过确认来保证一定收到客户端或服务器的message，但是我们却不能保证message仅有一次，
     * 也就是当client没收到service的puback或者service没有收到client的puback，那么就会一直发送publisher
     * qos2可以实现仅仅接受一次message，其主要原理(对于publisher而言)，
     * publisher和broker进行了缓存，其中publisher缓存了message和msgID，而broker缓存了msgID，两方都做记录所以可以保证消息不重复，
     * 但是由于记录是需要删除的，这个删除流程同样多了一倍
     */
    protected int qos = 2;

    protected Boolean hasSSL = false; //默认SSL关闭

    protected Integer port = 1883; //默认端口

    protected String username = "账号";

    protected String password = "密码";

    protected String TCP = "tcp://";

    protected String SSL = "ssl://";


    /**
     * Custom Configuration
     */
    protected abstract void config(String ip, Integer port, Boolean ssl, Boolean withUserNamePass);

    /**
     * Default Configuration
     */
    protected abstract void config();


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public Boolean getHasSSL() {
        return hasSSL;
    }

    public void setHasSSL(Boolean hasSSL) {
        this.hasSSL = hasSSL;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTCP() {
        return TCP;
    }

    public void setTCP(String TCP) {
        this.TCP = TCP;
    }

    public String getSSL() {
        return SSL;
    }

    public void setSSL(String SSL) {
        this.SSL = SSL;
    }
}

~~~



#### 4. Publisher推送者

定义接口

```java
public interface IMQTTPublisher {
    /**
     * 发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    public void publishMessage(String topic, String message);

    /**
     * 断开MQTT客户端
     */
    public void disconnect();
}


```

定义类

```java
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author gaoyf
 * @since 2020/4/9 0009 16:02
 */
@Component
public class MQTTPublisher extends MQTTConfig implements MqttCallback, IMQTTPublisher {

    private String ipUrl = null;

    final private String colon = ":";//冒号分隔符
    final private String clientId = "mqtt_server_pub";//客户端ID  这里可以随便定义

    private MqttClient mqttClient = null;
    private MqttConnectOptions connectionOptions = null;
    private MemoryPersistence persistence = null;

    private static final Logger logger = LoggerFactory.getLogger(MQTTPublisher.class);

    /**
     * Private default constructor
     */
    private MQTTPublisher() {
        this.config();
    }

    /**
     * Private constructor
     */
    private MQTTPublisher(String ip, Integer port, Boolean ssl, Boolean withUserNamePass) {
        this.config(ip, port, ssl, withUserNamePass);
    }

    /**
     * Factory method to get instance of MQTTPublisher
     *
     * @return MQTTPublisher
     */
    public static MQTTPublisher getInstance() {
        return new MQTTPublisher();
    }

    /**
     * 获取MQTTPublisher实例的工厂方法
     *
     * @param ip               ip地址
     * @param port             断开
     * @param ssl              是否ssl
     * @param withUserNamePass 用户名密码
     * @return MQTTPublisher
     */
    public static MQTTPublisher getInstance(String ip, Integer port, Boolean ssl, Boolean withUserNamePass) {
        return new MQTTPublisher(ip, port, ssl, withUserNamePass);
    }


    protected void config() {

        this.ipUrl = this.TCP + this.ip + colon + this.port;
        this.persistence = new MemoryPersistence();
        this.connectionOptions = new MqttConnectOptions();
        try {
            this.mqttClient = new MqttClient(ipUrl, clientId, persistence);
            this.connectionOptions.setCleanSession(true);
            this.mqttClient.connect(this.connectionOptions);
            this.mqttClient.setCallback(this);
        } catch (MqttException me) {
            logger.error("ERROR", me);
        }
    }


    protected void config(String ip, Integer port, Boolean ssl, Boolean withUserNamePass) {
        String protocal = this.TCP;
        if (ssl) {
            protocal = this.SSL;
        }
        this.ipUrl = protocal + this.ip + colon + port;
        this.persistence = new MemoryPersistence();
        this.connectionOptions = new MqttConnectOptions();

        try {
            this.mqttClient = new MqttClient(ipUrl, clientId, persistence);
            this.connectionOptions.setCleanSession(true);
            if (withUserNamePass) {
                if (password != null) {
                    this.connectionOptions.setPassword(this.password.toCharArray());
                }
                if (username != null) {
                    this.connectionOptions.setUserName(this.username);
                }
            }
            this.mqttClient.connect(this.connectionOptions);
            this.mqttClient.setCallback(this);
        } catch (MqttException me) {
            logger.error("ERROR", me);
        }
    }


    @Override
    public void publishMessage(String topic, String message) {

        try {
            MqttMessage mqttmessage = new MqttMessage(message.getBytes());
            mqttmessage.setQos(this.qos);
            this.mqttClient.publish(topic, mqttmessage);
        } catch (MqttException me) {
            logger.error("ERROR", me);
        }

    }

    @Override
    public void connectionLost(Throwable arg0) {
        logger.info("Connection Lost");

    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        logger.info("delivery completed");

    }


    @Override
    public void messageArrived(String arg0, MqttMessage arg1) {
        // Leave it blank for Publisher

    }

    @Override
    public void disconnect() {
        try {
            this.mqttClient.disconnect();
        } catch (MqttException me) {
            logger.error("ERROR", me);
        }
    }
}

```



#### 5. Subscriber 订阅者

定义接口

```java
public interface IMQTTSubscriber {

    /**
     * 订阅消息
     *
     * @param topic
     */
    public void subscribeMessage(String topic);

    /**
     * 断开MQTT客户端
     */
    public void disconnect();
}

```

类定义

```java

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * @author gaoyf
 * @since 2020/4/9 0009 16:05
 */
@Component
public class MQTTSubscriber extends MQTTConfig implements MqttCallback, IMQTTSubscriber {

    private String brokerUrl = null;
    final private String colon = ":";//冒号分隔符
    final private String clientId = "mqtt_server_sub";//客户端ID  这里可以随便定义

    private MqttClient mqttClient = null;
    private MqttConnectOptions connectionOptions = null;
    private MemoryPersistence persistence = null;

    private static final Logger logger = LoggerFactory.getLogger(MQTTSubscriber.class);

    public MQTTSubscriber() {
        this.config();
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.info("Connection Lost");

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Called when a message arrives from the server that matches any subscription made by the client
        String time = new Timestamp(System.currentTimeMillis()).toString();
        System.out.println();
        System.out.println("***********************************************************************");
        System.out.println("消息到达时间：" + time + "  Topic: " + topic + "  Message: "
                + new String(message.getPayload()));
        System.out.println("***********************************************************************");
        System.out.println();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Leave it blank for subscriber

    }

    @Override
    public void subscribeMessage(String topic) {
        try {
            this.mqttClient.subscribe(topic, this.qos);
        } catch (MqttException me) {
            me.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            this.mqttClient.disconnect();
        } catch (MqttException me) {
            logger.error("ERROR", me);
        }
    }

    protected void config(String ip, Integer port, Boolean ssl, Boolean withUserNamePass) {
        String protocal = this.TCP;
        if (ssl) {
            protocal = this.SSL;
        }
        this.brokerUrl = protocal + this.ip + colon + port;
        this.persistence = new MemoryPersistence();
        this.connectionOptions = new MqttConnectOptions();
        try {
            this.mqttClient = new MqttClient(brokerUrl, clientId, persistence);
            this.connectionOptions.setCleanSession(true);
            if (withUserNamePass) {
                if (password != null) {
                    this.connectionOptions.setPassword(this.password.toCharArray());
                }
                if (username != null) {
                    this.connectionOptions.setUserName(this.username);
                }
            }
            this.mqttClient.connect(this.connectionOptions);
            this.mqttClient.setCallback(this);
        } catch (MqttException me) {
            me.printStackTrace();
        }

    }

    protected void config() {

        this.brokerUrl = this.TCP + this.ip + colon + this.port;
        this.persistence = new MemoryPersistence();
        this.connectionOptions = new MqttConnectOptions();
        try {
            this.mqttClient = new MqttClient(brokerUrl, clientId, persistence);
            this.connectionOptions.setCleanSession(true);
            this.mqttClient.connect(this.connectionOptions);
            this.mqttClient.setCallback(this);
        } catch (MqttException me) {
            me.printStackTrace();
        }

    }

}

```

#### 6. 构建 RestFul接口

构建Controller

```java
import com.gaoyf.mqtt.core.IMQTTPublisher;
import com.gaoyf.mqtt.core.IMQTTSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * @author gaoyf
 * @since 2020/4/9 0009 16:14
 * <p>
 * 测试controller
 */
@RestController
public class DemoRestController {
    public static String TOPIC_LOOP_TEST = "mqtt/loop/message";

    @Autowired
    IMQTTPublisher publisher;

    @Autowired
    IMQTTSubscriber subscriber;

    /**
     * 被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     * PostConstruct在构造函数之后执行，init（）方法之前执行。PreDestroy（）方法在destroy（）方法知性之后执行
     * 这里初始化订阅一个主题
     */
    @PostConstruct
    public void init() {
        subscriber.subscribeMessage(TOPIC_LOOP_TEST);
    }


    /**
     * 向指定主题发送消息
     *
     * @param data 数据
     * @return 响应
     */
    @RequestMapping(value = "/mqtt/loop/message", method = RequestMethod.POST)
    public String index(@RequestBody String data) {
        publisher.publishMessage(TOPIC_LOOP_TEST, data);
        return "Success";
    }

}
```

#### 7. 使用postman 调用8080 端口调试或者使用MQTTX工具进行调试。
[MQTTX 下载地址]( https://github.com/emqx/MQTTX/releases/tag/v1.3.0)



