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


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements
    ActionBar.OnNavigationListener {

    private boolean mBound;
    
    private TextView mTextLog;
    
    private IBinder mService;

    /**
     * Shows exit dialog
     */
    private AlertDialog mAlertDialogExit;


    private Fragment[] mFragments = new Fragment[9];

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        
        
        /*
         * And may exit
         */
        mAlertDialogExit = new AlertDialog.Builder(MainActivity.this).create();
        mAlertDialogExit.setTitle(getString(R.string.Exit));
        mAlertDialogExit.setCanceledOnTouchOutside(true);
        mAlertDialogExit.setCancelable(true);
        mAlertDialogExit.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Yes), new DialogInterface.OnClickListener() {
            /* (non-Javadoc)
             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
             */
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Go to background
                 */
                System.runFinalizersOnExit(true);
                System.exit(0);
            }
        });
        mAlertDialogExit.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.No), new DialogInterface.OnClickListener() {
            /* (non-Javadoc)
             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
             */
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Go to background
                 */
                dialog.dismiss();
            }            
        });

        mAlertDialogExit.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_main, null);
        mTextLog = (TextView)view.findViewById(R.id.main_text_log);
        Logger.setTextView(mTextLog);
        setContentView(view);
        mBound = false;
        
        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        Bundle args = new Bundle();
        mFragments[0] = new StatusFragment();
        mFragments[1] = new BlueToothInFragment();
        mFragments[2] = new WiFiInFragment();
        mFragments[3] = new XplaneFragment();
        mFragments[4] = new MsfsFragment();
        mFragments[5] = new BlueToothOutFragment();
        mFragments[6] = new FileFragment();
        mFragments[7] = new GPSSimulatorFragment();
        mFragments[8] = new USBInFragment();

        for(int i = 0; i < 9; i++) {
            mFragments[i].setArguments(args);
        }

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
        // Specify a SpinnerAdapter to populate the dropdown list.
        new ArrayAdapter<String>(actionBar.getThemedContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1, new String[] {
                getString(R.string.Status),
                getString(R.string.Bluetooth), 
                getString(R.string.WIFI), 
                getString(R.string.XPlane), 
                getString(R.string.MSFS), 
                getString(R.string.AP), 
                getString(R.string.Play), 
                getString(R.string.GPSSIM), 
                getString(R.string.USBIN)
                }), this);

    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Clean up stuff on exit
         */
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
        if(null != mAlertDialogExit) {
            try {
                mAlertDialogExit.dismiss();
            }
            catch (Exception e) {
            }
        }

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
            mBound = true;
            mService = service;
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Start the helper service in Avare.
         */
        Intent i = new Intent("com.ds.avare.START_SERVICE");
        i.setClassName("com.ds.avare", "com.ds.avare.IHelperService");
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragments[itemPosition].getArguments().putBoolean("bound", mBound);
        
        
        switch(itemPosition) {
        
            case 0:
                StatusFragment status = (StatusFragment) mFragments[0];
                fragmentTransaction.replace(R.id.detailFragment, status);
                break;
                
            case 1:
                BlueToothInFragment btin = (BlueToothInFragment) mFragments[1];
                BlueToothInFragment.init(mService);
                btin = new BlueToothInFragment();
                fragmentTransaction.replace(R.id.detailFragment, btin);
                break;
                
            case 2:
                WiFiInFragment wfin = (WiFiInFragment) mFragments[2];
                WiFiInFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, wfin);
                break;
           
            case 3:
                XplaneFragment xp = (XplaneFragment) mFragments[3];
                XplaneFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, xp);
                break;
            case 4:
                MsfsFragment msfs = (MsfsFragment) mFragments[4];
                MsfsFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, msfs);
                break;
            case 5:
                BlueToothOutFragment btout  = (BlueToothOutFragment) mFragments[5];
                BlueToothOutFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, btout);
                break;
            case 6:
                FileFragment file = (FileFragment) mFragments[6];
                FileFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, file);
                break;
            case 7:
                GPSSimulatorFragment gpsSim = (GPSSimulatorFragment) mFragments[7];
                GPSSimulatorFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, gpsSim);
                break;
            case 8:
                USBInFragment usbin = (USBInFragment) mFragments[8];
                USBInFragment.init(mService);
                fragmentTransaction.replace(R.id.detailFragment, usbin);
                break;
        }

        fragmentTransaction.commit();
        return true;
    }
    
}
