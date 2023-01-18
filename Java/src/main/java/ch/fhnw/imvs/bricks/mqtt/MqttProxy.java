// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package main.java.ch.fhnw.imvs.bricks.mqtt;

import main.java.ch.fhnw.imvs.bricks.core.Proxy;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import main.java.ch.fhnw.imvs.bricks.core.Brick;
import main.java.ch.fhnw.imvs.bricks.core.Proxy;

import java.io.IOException;

public final class MqttProxy extends Proxy {
    private MqttProxy(MqttConfig config) {
        mqttConfig = config;
        mqttService = new MqttService();
    }

    private final MqttConfig mqttConfig;
    private final MqttService mqttService;

    // calLed exactly once
    private void connect() {
        System.out.println("connect");
        String host = mqttConfig.getHost();
        String username = mqttConfig.getUsername();
        String password = mqttConfig.getPassword();
        System.out.println(host);
        mqttService.init(host, username, password);
        mqttService.connect();
    }

    @Override
    public void connectBrick(Brick brick) {
        String topic = mqttConfig.getSubscribeTopic(brick.getID());
        IMqttMessageListener listener = new IMqttMessageListener() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.printf("message on \"%s\"\n", topic);
                byte[] payload = message.getPayload();
                MqttProxy.this.setPendingPayload(brick, payload);
            }
        };
        mqttService.subscribe(topic, listener);
        super.addBrick(brick);
    }

    @Override
    protected void syncBrick(Brick brick) throws IOException {
        byte[] payload = super.getTargetPayload(brick, false); // not a mock
        String topic = mqttConfig.getPublishTopic(brick.getID());
        mqttService.publish(topic, payload);
    }

    public static MqttProxy fromConfig(String configHost) {
        MqttConfig config = MqttConfig.fromHost(configHost); // TODO: too early to get config?
        MqttProxy proxy = new MqttProxy(config); // TODO: singleton per configHost?
        proxy.connect();
        return proxy;
    }
}
