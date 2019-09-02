package com.ad.carlib.source;

import com.ad.carlib.source.aidl.ICarClient;

public class ClientInfo {

    public static int CAR_CLIENT_IDLE = 0;
    public static int CAR_CLIENT_ALIVE = 1;
    public static int CAR_CLIENT_PAUSE = 2;
    public static int CAR_CLIENT_STOP = 3;

    private int pid;
    private int bindID;
    private int state;
    private ICarClient carClient;

    public ClientInfo(int pid, int bindID, int state, ICarClient carClient) {
        this.pid = pid;
        this.bindID = bindID;
        this.state = state;
        this.carClient = carClient;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getBindID() {
        return bindID;
    }

    public void setBindID(int bindID) {
        this.bindID = bindID;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ICarClient getCarClient() {
        return carClient;
    }

    public void setCarClient(ICarClient carClient) {
        this.carClient = carClient;
    }
}
