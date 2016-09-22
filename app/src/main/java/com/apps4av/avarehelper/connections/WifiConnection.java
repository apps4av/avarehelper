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
import com.apps4av.avarehelper.utils.GenericCallback;
import com.apps4av.avarehelper.utils.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;

/**
 * 
 * @author zkhan
 *
 */
public class WifiConnection extends Connection {

    
    private static WifiConnection mConnection;

    DatagramSocket mSocket;
    
    private int mPort = 0;
    

    /**
     * 
     */
    private WifiConnection() {
        super("WIFI Input");
    }


    /**
     * 
     * @return
     */
    public static WifiConnection getInstance() {

        if(null == mConnection) {
            mConnection = new WifiConnection();
        }
        return mConnection;
    }

    /**
     * 
     */
    public void start(final Preferences pref) {

        super.start(new GenericCallback() {
            @Override
            public Object callback(Object o, Object o1) {
                BufferProcessor bp = new BufferProcessor();

                byte[] buffer = new byte[8192];

                /*
                 * This state machine will keep trying to connect to
                 * ADBS/GPS receiver
                 */
                while(isRunning()) {

                    int red = 0;

                    /*
                     * Read.
                     */
                    red = read(buffer);
                    if(red <= 0) {
                        if(isStopped()) {
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
                        sendDataToHelper(s);
                    }
                }
                return null;
            }
        });
    }
        
    /**
     * 
     * A device name devNameMatch, will connect to first device whose
     * name matched this string.
     * @return
     */
    public boolean connect(int port) {
        
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


        super.connect();

        return true;
    }
    
    /**
     * 
     */
    public void disconnect() {
        
        /*
         * Exit
         */
        try {
            mSocket.close();
        } 
        catch(Exception e2) {
            Logger.Logit("Error stream close");
        }

        super.disconnect();
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

        saveToFile(pkt.getLength(), buffer);
        
        return pkt.getLength();
    }

    /**
     * 
     * @return
     */
    public int getPort() {
        return mPort;
    }

}
