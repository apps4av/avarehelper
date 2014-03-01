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

    private BlueToothConnectionOut mBtOut;
    private List<String> mList; 
    private Spinner mSpinnerOut;
    private Context mContext;
    private Button mConnectButtonOut;
    private IBinder mService;
    private CheckBox mSecureCb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.layout_ap, container, false);

        /*
         * List of BT devices is same
         */
        mBtOut = BlueToothConnectionOut.getInstance();
        if (null != mService) {
            mBtOut.setHelper(IHelper.Stub.asInterface(mService));
        }

        mList = mBtOut.getDevices();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, mList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerOut = (Spinner)view.findViewById(R.id.main_spinner_out);

        mSpinnerOut.setAdapter(adapter);

        mSecureCb = (CheckBox) view.findViewById(R.id.main_cb_btout);
        
        mConnectButtonOut = (Button) view.findViewById(R.id.main_button_connect_out);
        mConnectButtonOut.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if (mBtOut.isConnected()) {
                    mBtOut.stop();
                    mBtOut.disconnect();
                    if (mBtOut.isConnected()) {
                        mConnectButtonOut.setText(getString(R.string.Disconnect));
                    } else {
                        mConnectButtonOut.setText(getString(R.string.Connect));
                    }
                    return;
                }
                /*
                 * Connect to the given device in list
                 */
                String val = (String) mSpinnerOut.getSelectedItem();
                if (null != val && (!mBtOut.isConnected())) {
                    mConnectButtonOut.setText(getString(R.string.Connect));
                    mBtOut.connect(val, mSecureCb.isChecked());
                    if (mBtOut.isConnected()) {
                        mBtOut.start();
                    }
                    if (mBtOut.isConnected()) {
                        mConnectButtonOut.setText(getString(R.string.Disconnect));
                    } else {
                        mConnectButtonOut.setText(getString(R.string.Connect));
                    }
                }
            }
        });

        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBtOut.isConnected()) {
            mBtOut.stop();
        }
    }

    /**
     * 
     */
    public void init(Context ctx, IBinder service) {
        mContext = ctx;
        mService = service;
    }

}