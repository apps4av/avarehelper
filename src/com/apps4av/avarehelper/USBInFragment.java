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
import android.widget.EditText;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 * 
 */
public class USBInFragment extends Fragment {

    private USBConnectionIn mUSB;
    private Context mContext;
    private Button mConnectButton;
    private static IBinder mService;
    private Button mConnectFileSaveButton;
    private EditText mTextFileSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        mContext = container.getContext();
        
        View view = inflater.inflate(R.layout.layout_usbin, container, false);

        /*
         * BT connection
         */
        mTextFileSave = (EditText)view.findViewById(R.id.usbin_file_name_save);

        /*
         * List of BT devices is same
         */
        mUSB = USBConnectionIn.getInstance(mContext);
        
        if (null != mService) {
            mUSB.setHelper(IHelper.Stub.asInterface(mService));
        }

        mConnectButton = (Button) view.findViewById(R.id.usbin_button_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if (mUSB.isConnected()) {
                    mUSB.stop();
                    mUSB.disconnect();
                    setStates();
                    return;
                }
                /*
                 * Connect to the given device in list
                 */
                if (!mUSB.isConnected()) {
                    mConnectButton.setText(getString(R.string.Connect));
                    mUSB.connect();
                    if (mUSB.isConnected()) {
                        mUSB.start();
                    }
                    setStates();
                }
            }
        });

        mConnectFileSaveButton = (Button)view.findViewById(R.id.usbin_button_connect_file_save);
        mConnectFileSaveButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                String val = mTextFileSave.getText().toString();
                if(mUSB.getFileSave() != null) {
                    mConnectFileSaveButton.setText(mContext.getString(R.string.Save));
                    mUSB.setFileSave(null);
                }
                else {
                    mConnectFileSaveButton.setText(mContext.getString(R.string.Saving));
                    mUSB.setFileSave(val);
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
        if (mUSB.isConnected()) {
            mConnectButton.setText(getString(R.string.Disconnect));
        } else {
            mConnectButton.setText(getString(R.string.Connect));
        }

        if(mUSB.getFileSave() != null) {
            mConnectFileSaveButton.setText(mContext.getString(R.string.Saving));
            mTextFileSave.setText(mUSB.getFileSave());
        }
        else {
            mConnectFileSaveButton.setText(mContext.getString(R.string.Save));
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