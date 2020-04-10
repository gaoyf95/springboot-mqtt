package com.gaoyf.mqtt.core;

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