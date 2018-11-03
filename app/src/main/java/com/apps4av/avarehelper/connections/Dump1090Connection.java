package com.apps4av.avarehelper.connections;

import android.content.Context;

import com.apps4av.avarehelper.utils.GenericCallback;
import com.apps4av.avarehelper.utils.Logger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class Dump1090Connection extends Connection {

    private static Dump1090Connection mConnection;
    private URL mURL;

    private Dump1090Connection() {
        super("Dump1090 Input");
        setCallback(new GenericCallback() {
            @Override
            public Object callback(Object o, Object o1) {
                while(isRunning()) {
                    fetchDump1090JSON();
                    try {
                        Thread.sleep(3000);
                    } catch(Exception e) {
                    }
                }
                return null;
            }
        });
    }

    @Override
    public boolean connect(String to, boolean secure) {
        try {
            Logger.Logit("Fetch Dump1090 feed from http://" + to + "/data.json");
            mURL = new URL("http://" + to + "/data.json");
            connectConnection();
        } catch (Exception e) {
            Logger.Logit("Illegal URL format.");
            mURL = null;
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
        mURL = null;
        disconnectConnection();
    }

    public static Dump1090Connection getInstance(Context ctx) {
        if(null == mConnection) {
            mConnection = new Dump1090Connection();
        }
        return mConnection;
    }

    @Override
    public List<String> getDevices() {
        return new ArrayList<>();
    }

    @Override
    public String getConnDevice() {
        return "";
    }

    public JSONObject fetchDump1090JSON() {
        if (null == mURL) return null;

        StringBuffer response = new StringBuffer();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) mURL.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");

            int status = conn.getResponseCode();
            if (status != 200) {
                // Return empty if fetch failed.
                Logger.Logit("HTTP connection failed, status code: " + status);
                return null;
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
        } catch (Exception e) {
            Logger.Logit("HTTP connection failed: " + e.toString());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                Logger.Logit("GET: " + response.toString());
                return new JSONObject(response.toString());
            } catch (Exception e) {
                Logger.Logit("Unable to convert JSON.");
                return null;
            }
        }
    }
}
