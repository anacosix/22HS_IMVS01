// Copyright (c) 2022 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package main.java.ch.fhnw.imvs.bricks.actuators;

import main.java.ch.fhnw.imvs.bricks.impl.DigitalOutputBrick;
import main.java.ch.fhnw.imvs.bricks.core.Proxy;

import java.io.IOException;

public final class BasicLedBrick extends DigitalOutputBrick {
    // Display images etc.
    private BasicLedBrick(Proxy proxy, String brickID) {
        super(proxy, brickID);
    }

//    public boolean isEnabled() {
//        return super.isActive();
//    }

    public void setEnabled(boolean enabled) throws IOException {
        super.setActive(enabled);
    }

    public static BasicLedBrick connect(Proxy proxy, String brickID) {
        BasicLedBrick brick = new BasicLedBrick(proxy, brickID);
        brick.connect();
        return brick;
    }
}
