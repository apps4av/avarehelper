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

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.apps4av.avarehelper.storage.Preferences;
import com.apps4av.avarehelper.utils.GenericCallback;
import com.apps4av.avarehelper.utils.Logger;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.LinkedList;


/**
 * 
 * @author zkhan
 *
 */
public class USBConnectionIn extends Connection {

    private static USBConnectionIn mConnection;
    private static UsbManager mUsbManager;
    private String mParams = "230400,8,n,1";
    private UsbSerialDriver mDriver = null;


    /**
     * 
     */
    private USBConnectionIn() {
        super("USB Input");
    }

    /**
     * 
     * @return
     */
    public static USBConnectionIn getInstance(Context ctx) {
        if(null == mConnection) {
            mConnection = new USBConnectionIn();
            mUsbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
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
                            Thread.sleep(10);
                        } catch (Exception e) {

                        }

                        // serial driver sends 0 when no data
                        if(red == 0) {
                            continue;
                        }

                        /*
                         * Try to reconnect
                         */
                        Logger.Logit("Disconnected from USB device, retrying to connect");

                        disconnect();
                        connect(mParams);
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
    public boolean connect(String params) {
        
        mParams = params;
        mDriver = UsbSerialProber.findFirstDevice(mUsbManager);

        if(mDriver == null) {
            Logger.Logit("No USB serial device available");
            return false;
        }
        
        
        /*
         * Only when not connected, connect
         */
        if(getState() != Connection.DISCONNECTED) {
            Logger.Logit("Failed! Already connected?");

            return false;
        }
        setState(Connection.CONNECTING);

        try {
            mDriver.open();
            
            String tokens[] = mParams.split(",");
            // 115200, 8, n, 1
            // rate, data, parity, stop
            int rate = Integer.parseInt(tokens[0]);
            int data = Integer.parseInt(tokens[1]);
            int parity;
            if(tokens[2].equals("n")) {
                parity = UsbSerialDriver.PARITY_NONE;
            }
            else if (tokens[2].equals("o")) {
                parity = UsbSerialDriver.PARITY_ODD;
            }
            else {
                parity = UsbSerialDriver.PARITY_EVEN;
            }
            int stop = Integer.parseInt(tokens[3]);
            mDriver.setParameters(rate, data, stop, parity);
        } 
        catch (Exception e) {
            setState(Connection.DISCONNECTED);
            Logger.Logit("Failed!");
            return false;
        } 

        super.connect();
        return true;
    }
    
    /**
     * 
     */
    public void disconnect() {
        
        try {
            mDriver.close();
        }
        catch (Exception e) {
            
        }
        mDriver = null;
        /*
         * Exit
         */
        super.disconnect();
    }
    
    /**
     * 
     * @return
     */
    private int read(byte[] buffer) {
        int red = -1;
        try {
            red = mDriver.read(buffer, 1000);
        } 
        catch(Exception e) {
            red = -1;
        }

        saveToFile(red, buffer);
        return red;
    }

    /**
     * 
     * @return
     */
    public String getParams() {
        return mParams;
    }
    
}
