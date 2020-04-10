package com.gaoyf.mqtt.controller;

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
