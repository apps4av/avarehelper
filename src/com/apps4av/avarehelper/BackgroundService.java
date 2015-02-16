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

import java.util.Timer;
import java.util.TimerTask;

import com.apps4av.avarehelper.connections.BlueToothConnectionIn;
import com.apps4av.avarehelper.connections.BlueToothConnectionOut;
import com.apps4av.avarehelper.connections.FileConnectionIn;
import com.apps4av.avarehelper.connections.GPSSimulatorConnection;
import com.apps4av.avarehelper.connections.MsfsConnection;
import com.apps4av.avarehelper.connections.USBConnectionIn;
import com.apps4av.avarehelper.connections.WifiConnection;
import com.apps4av.avarehelper.connections.XplaneConnection;
import com.apps4av.avarehelper.utils.GenericCallback;
import com.ds.avare.IHelper;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

/**
 * @author zkhan
 * Main storage service. It stores all states so when activity dies,
 * we dont start from no state.
 * 
 */
public class BackgroundService extends Service {

    private IBinder mHelperService;
    
    private BlueToothConnectionIn mBtInCon;
    private BlueToothConnectionOut mBtOutCon;
    private FileConnectionIn mFileInCon;
    private GPSSimulatorConnection mGpsSimCon;
    private USBConnectionIn mUsbInCon;
    private WifiConnection mWifiCon;
    private XplaneConnection mXplaneCon;
    private MsfsConnection mMsfsCon;
    private Timer mTimer;
    private GenericCallback mCb;

    /**
     * Local binding as this runs in same thread
     */
    private final IBinder binder = new LocalBinder();

    /**
     * 
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
         */
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            mHelperService = service;
            mBtInCon.setHelper(IHelper.Stub.asInterface(service));
            mBtInCon.setHelper(IHelper.Stub.asInterface(service));
            mBtOutCon.setHelper(IHelper.Stub.asInterface(service));
            mFileInCon.setHelper(IHelper.Stub.asInterface(service));
            mGpsSimCon.setHelper(IHelper.Stub.asInterface(service));
            mUsbInCon.setHelper(IHelper.Stub.asInterface(service));
            mWifiCon.setHelper(IHelper.Stub.asInterface(service));
            mXplaneCon.setHelper(IHelper.Stub.asInterface(service));
            mMsfsCon.setHelper(IHelper.Stub.asInterface(service));
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    
    /**
     * @author zkhan
     *
     */
    public class LocalBinder extends Binder {
        /**
         * @return
         */
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
    
    /* (non-Javadoc)
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }
    
    /* (non-Javadoc)
     * @see android.app.Service#onUnbind(android.content.Intent)
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        mBtInCon = BlueToothConnectionIn.getInstance();
        mBtOutCon = BlueToothConnectionOut.getInstance();
        mFileInCon = FileConnectionIn.getInstance();
        mGpsSimCon = GPSSimulatorConnection.getInstance();
        mUsbInCon = USBConnectionIn.getInstance(this);
        mWifiCon = WifiConnection.getInstance();
        mXplaneCon = XplaneConnection.getInstance();
        mMsfsCon = MsfsConnection.getInstance();
        mHelperService = null;
        
        mTimer = new Timer();
        TimerTask time = new UpdateConnection();
        /*
         * Start binding to Avare
         */
        mTimer.scheduleAtFixedRate(time, 0, 5000);

    }
        
    /* (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        
        try {
	        unbindService(mConnection);
	        mTimer.cancel();
	        mBtInCon.stop();
	        mBtOutCon.stop();
	        mFileInCon.stop();
	        mGpsSimCon.stop();
	        mUsbInCon.stop();
	        mWifiCon.stop();
	        mXplaneCon.stop();
	        mMsfsCon.stop();
        }
        catch(Exception e) {
        	
        }
        
        super.onDestroy();        
    }

    public BlueToothConnectionIn getBlueToothConnectionIn() {
    	return mBtInCon;
    }
    public BlueToothConnectionOut getBlueToothConnectionOut() {
    	return mBtOutCon;
    }
    public FileConnectionIn getFileConnectionIn() { 
    	return mFileInCon;
    }
    public GPSSimulatorConnection getGPSSimulatorConnection() {
    	return mGpsSimCon;
    }
    public USBConnectionIn getUSBConnectionIn() {
    	return mUsbInCon;
    }
    public WifiConnection getWifiConnection() {
    	return mWifiCon;
    }
    public XplaneConnection getXplaneConnection() {
    	return mXplaneCon;
    }
    public MsfsConnection getMsfsConnection() {
    	return mMsfsCon;
    }

    public void setStatusCallback(GenericCallback cb) {
    	mCb = cb;
    }

    /**
     * @author zkhan
     *
     */ 
    private class UpdateConnection extends TimerTask {
        
        /* (non-Javadoc)
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run() {
        	
        	boolean notConnected = (null == mHelperService) || (!mHelperService.isBinderAlive());
        	
        	if(null != mCb) {
        		mCb.callback((Boolean)(!notConnected), null);
        	}
        	
        	if(notConnected) {
    	        /*
    	         * Start the helper service in Avare.
    	         */
    	        Intent i = new Intent("com.ds.avare.START_SERVICE");
    	        i.setClassName("com.ds.avare", "com.ds.avare.IHelperService");
    	        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        	}
        }
    }
}