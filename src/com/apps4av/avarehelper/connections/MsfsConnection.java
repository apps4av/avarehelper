/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper.connections;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.json.JSONException;
import org.json.JSONObject;

import com.apps4av.avarehelper.nmea.Ownship;
import com.apps4av.avarehelper.utils.Logger;
import com.ds.avare.IHelper;

/**
 * 
 * @author zkhan
 *
 */
public class MsfsConnection {

    
    private static MsfsConnection mConnection;
    
    private static IHelper mHelper;
    
    private Thread mThread;
    
    private static boolean mRunning;
    
    DatagramSocket mSocket;
    
    private int mPort = 0;
    
    private boolean mConnected = false;
    
    /**
     * 
     */
    private MsfsConnection() {
    }

    
    /**
     * 
     * @return
     */
    public static MsfsConnection getInstance() {

        if(null == mConnection) {
            mConnection = new MsfsConnection();
            mRunning = false;
        }
        return mConnection;
    }

    /**
     * 
     */
    public void stop() {
        Logger.Logit("Stopping MSFS Listener");
        mRunning = false;
        if(null != mThread) {
            mThread.interrupt();
        }
    }

    /**
     * 
     */
    public void start() {
        
        Logger.Logit("Starting MSFS Listener");
               
        mRunning = true;
        
        /*
         * Thread that reads Xplane
         */
        mThread = new Thread() {
            @Override
            public void run() {
        
                Logger.Logit("MSFS reading data");

                byte[] buffer = new byte[16384];

                com.apps4av.avarehelper.nmea.DataBuffer nbuffer = 
                        new com.apps4av.avarehelper.nmea.DataBuffer(16384);
                com.apps4av.avarehelper.nmea.Decode ndecode = 
                        new com.apps4av.avarehelper.nmea.Decode();
                Ownship nmeaOwnship = new Ownship();
                
                
                /*
                 * This state machine will keep trying to connect to 
                 * ADBS/GPS receiver
                 */
                while(mRunning) {
                    
                    int red = 0;
                    
                    /*
                     * Read.
                     */
                    red = read(buffer);
                    if(red <= 0) {
                        if(!mRunning) {
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            
                        }
                        
                        /*
                         * Try to reconnect
                         */
                        Logger.Logit("Listener error, re-starting listener");

                        disconnect();
                        connect(mPort);
                        continue;
                    }

                    nbuffer.put(buffer, red);                     

                    byte[] buf;
                    
                    while(null != (buf = nbuffer.get())) {
                        com.apps4av.avarehelper.nmea.Message m = ndecode.decode(buf);
                        if(nmeaOwnship.addMessage(m)) {
                                
                            /*
                             * Make a GPS locaiton message from ADSB ownship message.
                             */
                            JSONObject object = new JSONObject();
                            Ownship om = nmeaOwnship;
                            try {
                                object.put("type", "ownship");
                                object.put("longitude", (double)om.mLon);
                                object.put("latitude", (double)om.mLat);
                                object.put("speed", (double)(om.mHorizontalVelocity));
                                object.put("bearing", (double)om.mDirection);
                                object.put("altitude", (double)((double)om.mAltitude));
                                object.put("time", (long)om.getTime());
                            } catch (JSONException e1) {
                                return;
                            }
                            
                            if(mHelper != null) {
                                try {
                                    mHelper.sendDataText(object.toString());
                                } catch (Exception e) {
                                }
                            }

                        }
                        continue;
                    }
                }
            }
        };
        mThread.start();
    }
    
        
    /**
     * 
     * A device name devNameMatch, will connect to first device whose
     * name matched this string.
     * @return
     */
    public boolean connect(int port) {
        
        Logger.Logit("Listening on port " + port);

        mPort = port;
        
        /*
         * Make socket
         */
        Logger.Logit("Making socket to listen");

        try {
            mSocket = new DatagramSocket(mPort);
        }
        catch(Exception e) {
            Logger.Logit("Failed! Connecting socket " + e.getMessage());
            return false;
        }

        mConnected = true;
        Logger.Logit("Success!");

        return true;
    }
    
    /**
     * 
     */
    public void disconnect() {
        
        Logger.Logit("Disconnecting from device");

        /*
         * Exit
         */
        try {
            mSocket.close();
        } 
        catch(Exception e2) {
            Logger.Logit("Error stream close");
        }
        
        mConnected = false;
        Logger.Logit("Listener stopped");
    }
    
    /**
     * 
     * @return
     */
    private int read(byte[] buffer) {
        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length); 
        try {
            mSocket.receive(pkt);
        } 
        catch(Exception e) {
            return -1;
        }
        return pkt.getLength();
    }


    /**
     * 
     * @param helper
     */
    public void setHelper(IHelper helper) {
        mHelper = helper;
    }

    /**
     * 
     */
    public boolean isConnected() {
        return mConnected;
    }
    
    /**
     * 
     * @return
     */
    public int getPort() {
        return mPort;
    }


}
