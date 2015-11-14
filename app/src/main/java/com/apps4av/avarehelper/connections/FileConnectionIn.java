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

import android.widget.CheckBox;

import com.apps4av.avarehelper.storage.Preferences;
import com.apps4av.avarehelper.utils.Logger;
import com.ds.avare.IHelper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * 
 * @author zkhan
 *
 */
public class FileConnectionIn {

    private static InputStream mStream = null;
    private static boolean mRunning = false;
    
    private static FileConnectionIn mConnection;
    
    private static ConnectionStatus mConnectionStatus;
    private static IHelper mHelper;

    private Thread mThread;
    private String mFileName = null;
    public CheckBox mFileDelayCb;
    public CheckBox mFileThrottleCb;


    /**
     * 
     */
    private FileConnectionIn() {
    }

    
    /**
     * 
     * @return
     */
    public static FileConnectionIn getInstance() {

        if(null == mConnection) {
            mConnection = new FileConnectionIn();
            mConnectionStatus = new ConnectionStatus();
            mConnectionStatus.setState(ConnectionStatus.DISCONNECTED);
        }
        return mConnection;
    }

    /**
     * 
     */
    public void stop() {
        Logger.Logit("Stopping File Reader");
        if(mConnectionStatus.getState() != ConnectionStatus.CONNECTED) {
            Logger.Logit("Stop failed because already stopped");
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
    public void start(final Preferences pref) {
        Logger.Logit("Starting File Reader");
        if(mConnectionStatus.getState() != ConnectionStatus.CONNECTED) {
            Logger.Logit("Starting failed because already started");
            return;
        }
        
        mRunning = true;
        // When reading from file, wait for 5 seconds to allow user to switch to Avare.
        if(mFileDelayCb.isChecked()) {
            Logger.Logit("Delaying 5 seconds");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }


        /*
         * Thread that reads File
         */
        mThread = new Thread() {
            @Override
            public void run() {
        
                Logger.Logit("File reading data");

                BufferProcessor bp = new BufferProcessor();

                bp.paceOutput= mFileThrottleCb;

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
     * @param state
     */
    private void setState(int state) {
        mConnectionStatus.setState(state);
    }
    
    
    /**
     * 
     * A device name devNameMatch, will connect to first device whose
     * name matched this string.
     * @return
     */
    public boolean connect(String fileName) {
        
        Logger.Logit("Opening file " + fileName);

        if(fileName == null) {
            return false;
        }
        
        mFileName = fileName;
        
        /*
         * Only when not connected, connect
         */
        if(mConnectionStatus.getState() != ConnectionStatus.DISCONNECTED) {
            Logger.Logit("Failed! Already reading?");

            return false;
        }
        setState(ConnectionStatus.CONNECTING);

        Logger.Logit("Getting input stream");

        try {
            mStream = new BufferedInputStream(new FileInputStream(mFileName));
        } 
        catch (Exception e) {
            Logger.Logit("Failed! Input stream error");

            setState(ConnectionStatus.DISCONNECTED);
        } 

        setState(ConnectionStatus.CONNECTED);

        Logger.Logit("Success!");

        return true;
    }
    
    /**
     * 
     */
    public void disconnect() {
        
        Logger.Logit("Closing file");

        /*
         * Exit
         */
        try {
            mStream.close();
        } 
        catch(Exception e2) {
            Logger.Logit("Error stream close");
        }
        
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
            red = mStream.read(buffer, 0, buffer.length);
        } 
        catch(Exception e) {
            red = -1;
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
    public String getFileName() {
        return mFileName;
    }

}
