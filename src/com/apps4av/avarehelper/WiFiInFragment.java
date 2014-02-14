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
    private IBinder mService;
    private Context mContext;
    private Button mConnectFileSaveButton;
    private boolean mFileSave;
    private EditText mTextFileSave;

    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        
        View view = inflater.inflate(R.layout.layout_wifiin, container, false);
        
        if(null == mService) {
            return view;
        }
        
        /*
         * WIFI connection
         */

        mWifi = WifiConnection.getInstance();
        mWifi.setHelper(IHelper.Stub.asInterface(mService));
        
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

        mFileSave = false;
        mConnectFileSaveButton = (Button)view.findViewById(R.id.main_button_connect_file_save);
        mConnectFileSaveButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                String val = mTextFileSave.getText().toString();
                if(mFileSave) {
                    mConnectFileSaveButton.setText(mContext.getString(R.string.Save));
                    mWifi.setFileSave(null);
                    mFileSave = false;
                }
                else {
                    mConnectFileSaveButton.setText(mContext.getString(R.string.Saving));
                    mWifi.setFileSave(val);
                    mFileSave = true;
                }
            }
        });

        
        return view;  
        
    }

    @Override  
    public void onDestroyView() {  
        super.onDestroyView();
        mWifi.stop();
        mWifi.disconnect();
    }

    
    /**
     * 
     */
    public void init(Context ctx, IBinder service) {
        mService = service;
        mContext = ctx;
    }
    
} 