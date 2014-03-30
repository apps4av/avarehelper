package com.apps4av.avarehelper;

import java.util.List;

import com.ds.avare.IHelper;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 * 
 */
public class BlueToothOutFragment extends Fragment {

    private BlueToothConnectionOut mBt;
    private List<String> mList; 
    private Spinner mSpinner;
    private Context mContext;
    private Button mConnectButton;
    private static IBinder mService;
    private CheckBox mSecureCb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mContext = container.getContext();


        View view = inflater.inflate(R.layout.layout_ap, container, false);

        /*
         * List of BT devices is same
         */
        mBt = BlueToothConnectionOut.getInstance();
        if (null != mService) {
            mBt.setHelper(IHelper.Stub.asInterface(mService));
        }

        mList = mBt.getDevices();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, mList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner = (Spinner)view.findViewById(R.id.main_spinner_out);

        mSpinner.setAdapter(adapter);

        mSecureCb = (CheckBox) view.findViewById(R.id.main_cb_btout);
        
        mConnectButton = (Button) view.findViewById(R.id.main_button_connect_out);
        mConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if (mBt.isConnected()) {
                    mBt.stop();
                    mBt.disconnect();
                    setStates();
                    return;
                }
                /*
                 * Connect to the given device in list
                 */
                String val = (String) mSpinner.getSelectedItem();
                if (null != val && (!mBt.isConnected())) {
                    mConnectButton.setText(getString(R.string.Connect));
                    mBt.connect(val, mSecureCb.isChecked());
                    if (mBt.isConnected()) {
                        mBt.start();
                    }
                    setStates();
                }
            }
        });

        setStates();
        return view;

    }
    
    /**
     * 
     */
    private void setStates() {
        if (mBt.isConnected()) {
            mConnectButton.setText(getString(R.string.Disconnect));
        } else {
            mConnectButton.setText(getString(R.string.Connect));
        }
        mSecureCb.setChecked(mBt.isSecure());
        int loc = mList.indexOf(mBt.getConnDevice());
        if(loc >= 0) {
            mSpinner.setSelection(loc);            
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