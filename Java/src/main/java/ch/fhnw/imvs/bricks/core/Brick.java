// Copyright (c) 2020 FHNW, Switzerland. All rights reserved.
// Licensed under MIT License, see LICENSE for details.

package main.java.ch.fhnw.imvs.bricks.core;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class Brick {
    protected Brick(Proxy proxy, String brickID) {
        this.proxy = proxy;
        this.brickID = brickID;
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(timeZone);
    }

    private final String brickID;
    private final Proxy proxy;
    private final DateFormat formatter;
    private double currentBatteryVoltage = 0.0;
    private Date currentTimestamp = new Date(0L);
    private Date pendingTimestamp = new Date(0L);
    private byte[] pendingPayload = null;

    public String getID() {
        return brickID;
    }

    public double getBatteryVoltage() {
        return currentBatteryVoltage;
    }

    public Date getTimestamp() {
        return currentTimestamp;
    }

    public String getTimestampIsoUtc() {
        return formatter.format(currentTimestamp);
    }

    protected void setBatteryVoltage(double voltage) { // called by Brick subclasses
        currentBatteryVoltage = voltage;
    }

    protected void connect() { // called by Brick factories
        proxy.connectBrick(this);
    }

    protected void sync() throws IOException { // called by Brick subclasses
        proxy.syncBrick(this);
    }

    protected abstract byte[] getTargetPayload(boolean mock) throws IOException; // called by Proxy base

    /* package */ void setPendingPayload(byte[] payload) { // called by Proxy base
        pendingTimestamp = new Date();
        pendingPayload = payload;
    }

    protected abstract void setCurrentPayload(byte[] payload) throws IOException; // called below

    /* package */ boolean tryUpdate() throws IOException { // called by Proxy base
        boolean updated;
        if (currentTimestamp.before(pendingTimestamp)) {
            currentTimestamp = pendingTimestamp;
            setCurrentPayload(pendingPayload);
            updated = true;
        } else {
            updated = false;
        }
        return updated;
    }
}
