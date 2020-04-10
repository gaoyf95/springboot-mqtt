package com.gaoyf.mqtt.core;

/**
 * mqtt配置文件
 */
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

    protected Boolean hasSSL = false; /* By default SSL is disabled */

    protected Integer port = 1883; /* Default port */

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
