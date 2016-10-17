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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.apps4av.avarehelper.connections.ConnectionFactory;
import com.apps4av.avarehelper.storage.Preferences;
import com.apps4av.avarehelper.utils.GenericCallback;
import com.apps4av.avarehelper.utils.Logger;

import java.io.Serializable;
import java.util.HashMap;

public class MainActivity extends ActionBarActivity implements
    ActionBar.OnNavigationListener {
    
    private TextView mTextLog;
    private TextView mTextStatus;
    
    private BackgroundService mService;

    private HashMap<String, String> mState;
    private Preferences mPref;

    private Fragment[] mFragments = new Fragment[10];

    private WifiManager.MulticastLock mMulticastLock;


    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("savedState", (Serializable) mState);
    }

    // On back press, act like home button and go to background.
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_main, null);
        mTextLog = (TextView)view.findViewById(R.id.main_text_log);
        mTextStatus = (TextView)view.findViewById(R.id.main_text_status);
        Logger.setTextView(mTextLog);
        setContentView(view);

        mPref = new Preferences(getApplicationContext());
        /*
         * Start service now, bind later. This will be no-op if service is already running
         */
        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        Bundle args = new Bundle();
        int pos = 0;
        mFragments[pos++] = new WiFiInFragment();
        mFragments[pos++] = new BlueToothInFragment();
        mFragments[pos++] = new XplaneFragment();
        mFragments[pos++] = new MsfsFragment();
        mFragments[pos++] = new BlueToothOutFragment();
        mFragments[pos++] = new FileFragment();
        mFragments[pos++] = new GPSSimulatorFragment();
        mFragments[pos++] = new USBInFragment();
        mFragments[pos++] = new HelpFragment();
        mFragments[pos++] = new PreferencesFragment();

        for(int i = 0; i < pos; i++) {
            mFragments[i].setArguments(args);
        }

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
        // Specify a SpinnerAdapter to populate the dropdown list.
        new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1, new String[] {
                getString(R.string.WIFI),
                getString(R.string.Bluetooth),
                getString(R.string.XPlane),
                getString(R.string.MSFS), 
                getString(R.string.AP), 
                getString(R.string.Play), 
                getString(R.string.GPSSIM), 
                getString(R.string.USBIN),
                getString(R.string.Help),
                getString(R.string.Preferences)
                }), this);


        // Acquire Multicast Lock to receive multicast packets over Wifi.
        WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mMulticastLock = wm.createMulticastLock("avarehelper");
        mMulticastLock.acquire();


        if (savedInstanceState != null) {
            //probably orientation change
            mState = (HashMap<String, String>) savedInstanceState.getSerializable("savedState");
            try {
                int id = Integer.valueOf(mState.get("fragmentIndex"));
                if(id >= 0) {
                    actionBar.setSelectedNavigationItem(id);
                }
            }
            catch (Exception e) {
            }

        }
        else if (mState != null) {
                //returning from backstack, data is fine, do nothing
        }
        else {
            //newly created, compute data
            mState = new HashMap<String, String>();
            try {
                int id = mPref.getFragmentIndex();
                if(id >= 0) {
                    actionBar.setSelectedNavigationItem(id);
                }
            }
            catch (Exception e) {
            }
        }
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Clean up stuff on exit
         */
        getApplicationContext().unbindService(mConnection);        

        // Release multicast lock.
        mMulticastLock.release();
    }
   
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
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder)service;
            mService = binder.getService();
            mService.setStatusCallback(new GenericCallback() {
            	@Override
            	public Object callback(Object o1, Object o2) {
            		/*
            		 * Update status from timer in service
            		 */
            		Message m = mHandler.obtainMessage();
            		m.obj = o1;
            		mHandler.sendMessage(m);
            		return null;
            	}
            });
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Start the helper service in Avare.
         */
        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

    	FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Store fragment we are showing now
        mState.put("fragmentIndex", Integer.toString(itemPosition));
        mPref.setFragmentIndex(itemPosition);

        switch(itemPosition) {
        
            case 0:
                WiFiInFragment wfin = (WiFiInFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, wfin);
                break;
            case 1:
                BlueToothInFragment btin = (BlueToothInFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, btin);
                break;


            case 2:
                XplaneFragment xp = (XplaneFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, xp);
                break;
            case 3:
                MsfsFragment msfs = (MsfsFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, msfs);
                break;
            case 4:
                BlueToothOutFragment btout  = (BlueToothOutFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, btout);
                break;
            case 5:
                FileFragment file = (FileFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, file);
                break;
            case 6:
                GPSSimulatorFragment gpsSim = (GPSSimulatorFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, gpsSim);
                break;
            case 7:
                USBInFragment usbin = (USBInFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, usbin);
                break;
            case 8:
                HelpFragment help = (HelpFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, help);
                break;
            case 9:
                PreferencesFragment pref = (PreferencesFragment) mFragments[itemPosition];
                fragmentTransaction.replace(R.id.detailFragment, pref);
                break;
        }

        fragmentTransaction.commit();
        return true;
    }


    /**
     * This leak warning is not an issue if we do not post delayed messages, which is true here.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if((Boolean)msg.obj) {
            	mTextStatus.setText(getString(R.string.Connected) + " " + ConnectionFactory.getActiveConnections(getApplicationContext()));
            }
            else {
            	mTextStatus.setText(getString(R.string.NotConnected));
            }
        }
    };

}
