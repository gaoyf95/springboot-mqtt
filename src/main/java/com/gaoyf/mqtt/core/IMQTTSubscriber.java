package com.gaoyf.mqtt.core;

/**
 * @author gaoyf
 * @since 2020/4/9 0009 16:04
 * <p>
 * 订阅者接口
 */
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
