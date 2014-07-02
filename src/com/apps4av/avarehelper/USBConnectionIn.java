/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.ds.avare.IHelper;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;


/**
 * 
 * @author zkhan
 *
 */
public class USBConnectionIn {

    private static UsbSerialDriver mDriver = null;
    private static boolean mRunning = false;
    private static String mFileSave = null;
    private static String mParams = "115200,8,n,1";
    
    private static USBConnectionIn mConnection;
    
    private static ConnectionStatus mConnectionStatus;
    private static IHelper mHelper;
    
    private Thread mThread;
    private static UsbManager mUsbManager;


    /**
     * 
     */
    private USBConnectionIn() {
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
    public static USBConnectionIn getInstance(Context ctx) {

        if(null == mConnection) {
            mConnection = new USBConnectionIn();
            mConnectionStatus = new ConnectionStatus();
            mConnectionStatus.setState(ConnectionStatus.DISCONNECTED);
            mUsbManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        }
        return mConnection;
    }

    /**
     * 
     */
    public void stop() {
        Logger.Logit("Stopping USB");
        if(mConnectionStatus.getState() != ConnectionStatus.CONNECTED) {
            Logger.Logit("Stopping USB failed because already stopped");
            return;
        }
        mRunning = false;
        if(null != mThread) {
            mThread.interrupt();
        }
    }

    /**
     * 
     */
    public void start() {
        Logger.Logit("Starting USB");
        if(mConnectionStatus.getState() != ConnectionStatus.CONNECTED) {
            Logger.Logit("Starting USB failed because already started");
            return;
        }
        
        mRunning = true;
        
        /*
         * Thread that reads BT
         */
        mThread = new Thread() {
            @Override
            public void run() {
        
                Logger.Logit("USB reading data");

                BufferProcessor bp = new BufferProcessor();
                Logger.Logit("BT reading data");

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
                    LinkedList<String> objs = bp.decode();
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
     * @param state
     */
    private void setState(int state) {
        mConnectionStatus.setState(state);
    }
    
    /**
     * 
     * @return
     */
    public List<String> getDevices() {
        List<String> list = new ArrayList<String>();
        


        /*
         * Find devices
         */
        
        return list;
    }
    
    /**
     * 
     * A device name devNameMatch, will connect to first device whose
     * name matched this string.
     * @return
     */
    public boolean connect(String params) {
        
        mParams = params;
        Logger.Logit("Connecting to serial device");
        mDriver = UsbSerialProber.findFirstDevice(mUsbManager);

        if(mDriver == null) {
            Logger.Logit("No USB serial device available");
            return false;
        }
        
        
        /*
         * Only when not connected, connect
         */
        if(mConnectionStatus.getState() != ConnectionStatus.DISCONNECTED) {
            Logger.Logit("Failed! Already connected?");

            return false;
        }
        setState(ConnectionStatus.CONNECTING);

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
            setState(ConnectionStatus.DISCONNECTED);
            Logger.Logit("Failed!");
            return false;
        } 
        setState(ConnectionStatus.CONNECTED);

        Logger.Logit("Success!");

        return true;
    }
    
    /**
     * 
     */
    public void disconnect() {
        
        Logger.Logit("Disconnecting from device");

        try {
            mDriver.close();
        }
        catch (Exception e) {
            
        }
        mDriver = null;
        /*
         * Exit
         */
        setState(ConnectionStatus.DISCONNECTED);
        Logger.Logit("Disconnected");
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
        
        if(red > 0) {
            String file = null;
            synchronized(this) {
                file = mFileSave;
            }
            if(file != null) {
                try {
                    FileOutputStream output = new FileOutputStream(file, true);
                    output.write(buffer, 0, red);
                    output.close();
                } catch(Exception e) {
                }
            }
        }
        return red;
    }

    /**
     * 
     * @return
     */
    public boolean isConnected() {
        return mConnectionStatus.getState() == ConnectionStatus.CONNECTED;
    }

    /**
     * 
     * @return
     */
    public boolean isConnectedOrConnecting() {
        return mConnectionStatus.getState() == ConnectionStatus.CONNECTED ||
                mConnectionStatus.getState() == ConnectionStatus.CONNECTING;
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
     * @return
     */
    public String getParams() {
        return mParams;
    }
    
}
