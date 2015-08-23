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

import com.apps4av.avarehelper.storage.Preferences;
import com.apps4av.avarehelper.utils.Logger;
import com.ds.avare.IHelper;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

/**
 * 
 * @author zkhan
 *
 */
public class WifiConnection {

    
    private static WifiConnection mConnection;
    
    private static IHelper mHelper;
    
    private Thread mThread;
    private static String mFileSave = null;
    
    private static boolean mRunning;
    
    DatagramSocket mSocket;
    
    private int mPort = 0;
    
    private boolean mConnected = false;
    
    /**
     * 
     */
    private WifiConnection() {
    }

    /**
     * 
     * @param file
     */
    public void setFileSave(String file) {
        synchronized(this) {
            mFileSave = file;
        }
    }

    
    /**
     * 
     * @return
     */
    public static WifiConnection getInstance() {

        if(null == mConnection) {
            mConnection = new WifiConnection();
            mRunning = false;
        }
        return mConnection;
    }

    /**
     * 
     */
    public void stop() {
        Logger.Logit("Stopping WIFI");
        mRunning = false;
        if(null != mThread) {
            mThread.interrupt();
        }
    }

    /**
     * 
     */
    public void start(final Preferences pref) {
        
        Logger.Logit("Starting WiFi Listener");

        mRunning = true;
        
        /*
         * Thread that reads WIFI
         */
        mThread = new Thread() {
            @Override
            public void run() {
        
                Logger.Logit("WiFi reading data");

                BufferProcessor bp = new BufferProcessor();

                byte[] buffer = new byte[8192];
                
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

                    /*
                     * Put both in Decode and ADBS buffers
                     */
                    bp.put(buffer, red);
                    LinkedList<String> objs = bp.decode(pref);
                    for(String s : objs) {
                        if(mHelper != null) {
                            try {
                                mHelper.sendDataText(s);
                            } catch (Exception e) {
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
            mSocket.setBroadcast(true);
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
        
        if(pkt.getLength() > 0) {
            String file = null;
            synchronized(this) {
                file = mFileSave;
            }
            if(file != null) {
                try {
                    FileOutputStream output = new FileOutputStream(file, true);
                    output.write(buffer, 0, pkt.getLength());
                    output.close();
                } catch(Exception e) {
                }
            }
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
     * @return
     */
    public String getFileSave() {
        return mFileSave;
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
