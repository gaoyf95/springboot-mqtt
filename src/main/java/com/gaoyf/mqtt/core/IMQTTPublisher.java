package com.gaoyf.mqtt.core;

/**
 * @author gaoyf
 * @since 2020/4/9 0009 16:02
 * <p>
 * 发布者接口
 */
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

