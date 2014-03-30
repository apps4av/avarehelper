package com.apps4av.avarehelper;


import com.ds.avare.IHelper;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 *
 */
public class WiFiInFragment extends Fragment {
    
    private WifiConnection mWifi;
    private EditText mTextWifiPort;
    private CheckBox mWifiCb;
    private static IBinder mService;
    private Context mContext;
    private Button mConnectFileSaveButton;
    private EditText mTextFileSave;

    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        
        mContext = container.getContext();

        View view = inflater.inflate(R.layout.layout_wifiin, container, false);
        
        /*
         * WIFI connection
         */

        mWifi = WifiConnection.getInstance();
        if(null != mService) {
            mWifi.setHelper(IHelper.Stub.asInterface(mService));
        }
        
        
        mTextFileSave = (EditText)view.findViewById(R.id.main_file_name_save);
        mWifiCb = (CheckBox)view.findViewById(R.id.main_button_connectwifi);
        mTextWifiPort = (EditText)view.findViewById(R.id.main_wifi_port);
        mWifiCb.setOnClickListener(new OnClickListener() {
            
            
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    try {
                        mWifi.connect(Integer.parseInt(mTextWifiPort.getText().toString()));
                    }
                    catch (Exception e) {
                        /*
                         * Number parse
                         */
                        Logger.Logit("Invalid port");
                    }
                    mWifi.start();
                }
                else {
                    mWifi.stop();
                    mWifi.disconnect();
                }
            }
        });

        mConnectFileSaveButton = (Button)view.findViewById(R.id.main_button_connect_file_save);
        mConnectFileSaveButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                String val = mTextFileSave.getText().toString();
                if(mWifi.getFileSave() != null) {
                    mWifi.setFileSave(null);
                }
                else {
                    mWifi.setFileSave(val);
                }
                setStates();
            }
        });

        setStates();
        return view;  
        
    }
    
    /**
     * 
     */
    private void setStates() {
        mWifiCb.setChecked(mWifi.isConnected());

        if(mWifi.getFileSave() != null) {
            mConnectFileSaveButton.setText(mContext.getString(R.string.Saving));
            mTextFileSave.setText(mWifi.getFileSave());
        }
        else {
            mConnectFileSaveButton.setText(mContext.getString(R.string.Save));
        }

        if(mWifi.getPort() != 0) {
            mTextWifiPort.setText("" + mWifi.getPort());
        }

    }


    @Override  
    public void onDestroyView() {  
        super.onDestroyView();
    }

    
    /**
     * 
     */
    public static void init(IBinder service) {
        mService = service;
    }
    
} 