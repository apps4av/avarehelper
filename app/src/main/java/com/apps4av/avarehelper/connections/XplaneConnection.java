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

import org.json.JSONObject;

import com.apps4av.avarehelper.utils.Logger;
import com.ds.avare.IHelper;

/**
 * 
 * @author zkhan
 *
 */
public class XplaneConnection {

    
    private static XplaneConnection mConnection;
    
    private static IHelper mHelper;
    
    private Thread mThread;
    
    private static boolean mRunning;
    
    DatagramSocket mSocket;
    
    private int mPort = 0;
    
    private boolean mConnected = false;
    
    /**
     * 
     */
    private XplaneConnection() {
    }

    
    /**
     * 
     * @return
     */
    public static XplaneConnection getInstance() {

        if(null == mConnection) {
            mConnection = new XplaneConnection();
            mRunning = false;
        }
        return mConnection;
    }

    /**
     * 
     */
    public void stop() {
        Logger.Logit("Stopping XPlane Listener");
        mRunning = false;
        if(null != mThread) {
            mThread.interrupt();
        }
    }

    /**
     * 
     */
    public void start() {
        
        Logger.Logit("Starting XPlane Listener");
               
        mRunning = true;
        
        /*
         * Thread that reads Xplane
         */
        mThread = new Thread() {
            @Override
            public void run() {
        
                Logger.Logit("Xplane reading data");

                byte[] buffer = new byte[1024];
                
                
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

                    String input = new String(buffer);
                    if(input.startsWith("XGPS")) {
                        String tokens[] = input.split(",");
                        if(tokens.length >= 6) {
                            /*
                             * Make a GPS location message from ownship message.
                             */
                            JSONObject object = new JSONObject();
                            try {
                                object.put("type", "ownship");
                                object.put("longitude", Double.parseDouble(tokens[1]));
                                object.put("latitude", Double.parseDouble(tokens[2]));
                                object.put("speed", Double.parseDouble(tokens[5]));
                                object.put("bearing", Double.parseDouble(tokens[4]));
                                object.put("altitude", Double.parseDouble(tokens[3]));
                                object.put("time", System.currentTimeMillis());
                            } catch (Exception e1) {
                                continue;
                            }
                            
                            if(mHelper != null) {
                                try {
                                    mHelper.sendDataText(object.toString());
                                    Logger.Logit(object.toString());
                                } catch (Exception e) {
                                }
                            }
                     
                        }
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

        Logger.Logit("Success!");

        mConnected = true;
        
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
