package com.apps4av.avarehelper.connections;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;



public class Dump1090Connection extends Connection {

    private static Dump1090Connection mConnection;

    @Override
    public boolean connect(String to, boolean secure) {}

    @Override
    public void disconnect() {}

    public static Dump1090Connection getInstance(Context ctx) {

        if(null == mConnection) {
            mConnection = new Dump1090Connection();
        }
        return mConnection;
    }

    @Override
    public List<String> getDevices() {
        return new ArrayList<String>();
    }

    @Override
    public String getConnDevice() {
        return "";
    }
}
