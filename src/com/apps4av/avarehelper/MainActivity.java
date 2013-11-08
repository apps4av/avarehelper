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


import java.util.List;

import com.ds.avare.IHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * author zkhan
 */
public class MainActivity extends Activity {

    private Spinner mSpinner;
    private List<String> mList;
    private Button mConnectButton;
    private BlueToothConnection mBt;
    private XplaneConnection mXp;
    private boolean mBound;
    private TextView mText;
    private TextView mTextLog;
    private EditText mTextXplanePort;
    private TextView mTextXplaneIp;
    private CheckBox mXplaneCb;

    /**
     * Shows exit dialog
     */
    private AlertDialog mAlertDialogExit;

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
                MainActivity.super.onBackPressed();
                dialog.dismiss();
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
            
             /*
              * Get interface to Avare
              */
             mBt.setHelper(IHelper.Stub.asInterface(service));
             mXp.setHelper(IHelper.Stub.asInterface(service));
             mBound = true;
             mText.setText(getString(R.string.Connected));
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    /**
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Black);            

        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.main, null);
        setContentView(view);
        mBound = false;

        mText = (TextView)view.findViewById(R.id.main_text);
        
        mTextLog = (TextView)view.findViewById(R.id.main_text_log);
        Logger.setTextView(mTextLog);

        mTextXplaneIp = (TextView)view.findViewById(R.id.main_xplane_ip);
        mTextXplanePort = (EditText)view.findViewById(R.id.main_xplane_port);
        mXplaneCb = (CheckBox)view.findViewById(R.id.main_button_xplane_connect);
        mTextXplaneIp.setText(Util.getIpAddr(this));
        mXplaneCb.setOnClickListener(new OnClickListener() {
            
            
            @Override
            public void onClick(View v) {
              if (((CheckBox) v).isChecked()) {
                  try {
                      mXp.connect(Integer.parseInt(mTextXplanePort.getText().toString()));
                  }
                  catch (Exception e) {
                      /*
                       * Number parse
                       */
                      Logger.Logit("Invalid port");
                  }
                  mXp.start();
              }
              else {
                  mXp.stop();
                  mXp.disconnect();
              }
            }
          });

        mConnectButton = (Button)view.findViewById(R.id.main_button_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if(mBt.isConnected()) {
                    mBt.stop();
                    mBt.disconnect();
                    mConnectButton.setText(getApplicationContext().getString(R.string.Connect));
                    mText.setText(getString(R.string.NotConnected));
                    return;
                }
                /*
                 * Connect to the given device in list
                 */
                String val = (String)mSpinner.getSelectedItem();
                if(null != val && (!mBt.isConnected())) {                    
                    mBt.connect(val);
                    if(mBt.isConnected()) {
                        mConnectButton.setText(getApplicationContext().getString(R.string.Disconnect));
                        mBt.start();
                    }
                }
            }
        });
        
        /*
         * BT connection
         */
        mBt = BlueToothConnection.getInstance();
        
        /*
         * Xplane connection
         */
        mXp = XplaneConnection.getInstance();
        
        /*
         * For selecting adsb/nmea device
         */
        mSpinner = (Spinner)view.findViewById(R.id.main_spinner);
        mList = mBt.getDevices();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, mList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);       

        /*
         * Start the helper service in Avare.
         */
        Intent i = new Intent("com.ds.avare.START_SERVICE");
        i.setClassName("com.ds.avare", "com.ds.avare.IHelperService");
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        
        /*
         * Clean up stuff on exit
         */
        if(mBound) {
            mText.setText(getString(R.string.NotConnected));
            unbindService(mConnection);
            mBound = false;
        }
        if(mBt.isConnected()) {
            mBt.stop();
        }
        
        mXp.disconnect();
        mXp.stop();
        
        
        if(null != mAlertDialogExit) {
            try {
                mAlertDialogExit.dismiss();
            }
            catch (Exception e) {
            }
        }
        
        Logger.setTextView(null);

        super.onDestroy();
    }
}
