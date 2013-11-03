package com.apps4av.avarehelper;


import java.util.List;

import com.ds.avare.IHelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

/**
 * author zkhan
 */
public class MainActivity extends Activity {

    private Spinner mSpinner;
    private List<String> mList;
    private Button mConnectButton;
    private BlueToothConnection mBt;
    private boolean mBound;

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
            
             mBt.setHelper(IHelper.Stub.asInterface(service));
             mBound = true;
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
        
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_main, null);
        setContentView(view);
        mBound = false;

        
        mConnectButton = (Button)view.findViewById(R.id.button_connect);
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
                    if(mBound) {
                        unbindService(mConnection);
                        mBound = false;
                    }
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
                        /*
                         * Start the helper service in Avare.
                         */
                        if(!mBound) {
                            Intent i = new Intent("com.ds.avare.START_SERVICE");
                            i.setClassName("com.ds.avare", "com.ds.avare.IHelperService");
                            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
                        }
                    }
                }
            }
        });
        
        
        mBt = BlueToothConnection.getInstance();
        /*
         * For selecting adsb device
         */
        mSpinner = (Spinner)view.findViewById(R.id.spinner);
        mList = mBt.getDevices();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, mList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);       
    }

    @Override
    protected void onDestroy() {
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if(mBt.isConnected()) {
            mBt.stop();
        }
        super.onDestroy();
    }
}
