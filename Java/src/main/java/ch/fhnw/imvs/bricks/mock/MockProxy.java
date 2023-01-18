// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package main.java.ch.fhnw.imvs.bricks.mock;

import java.io.IOException;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.List;

import main.java.ch.fhnw.imvs.bricks.core.Brick;
import main.java.ch.fhnw.imvs.bricks.core.Proxy;

public final class MockProxy extends Proxy {
    private MockProxy() {
        bricks = new ArrayList<Brick>();
    }

    private final List<Brick> bricks;

    @Override
    public void connectBrick(Brick brick) {
        bricks.add(brick);
        super.addBrick(brick);
    }

    @Override
    protected void syncBrick(Brick brick) throws IOException {
        byte[] payload = super.getTargetPayload(brick, true); // mock
        super.setPendingPayload(brick, payload);
    }

    private void run() throws IOException {
        while (true) {
            for (Brick brick : bricks) {
                syncBrick(brick);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100); // ms
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        }
    }

    public static MockProxy fromConfig(String configHost) {
        MockProxy proxy = new MockProxy();
        Thread thread = new Thread() {
            public void run() {
                try {
                    proxy.run();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.start();
        return proxy;
    }
}
