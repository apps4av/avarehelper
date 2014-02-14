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
import android.widget.EditText;
import android.widget.Spinner;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 * 
 */
public class BlueToothInFragment extends Fragment {

    private BlueToothConnectionIn mBt;
    private List<String> mList;
    private Spinner mSpinner;
    private Context mContext;
    private Button mConnectButton;
    private IBinder mService;
    private Button mConnectFileSaveButton;
    private boolean mFileSave;
    private EditText mTextFileSave;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.layout_btin, container, false);

        /*
         * BT connection
         */
        mSpinner = (Spinner) view.findViewById(R.id.main_spinner);
        mTextFileSave = (EditText)view.findViewById(R.id.main_file_name_save);

        /*
         * List of BT devices is same
         */
        mBt = BlueToothConnectionIn.getInstance();
        
        if (null != mService) {
            mBt.setHelper(IHelper.Stub.asInterface(mService));
        }
        mList = mBt.getDevices();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_spinner_item, mList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);

        mConnectButton = (Button) view.findViewById(R.id.main_button_connect);
        mConnectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if (mBt.isConnected()) {
                    mBt.stop();
                    mBt.disconnect();
                    if (mBt.isConnected()) {
                        mConnectButton.setText(getString(R.string.Disconnect));
                    } else {
                        mConnectButton.setText(getString(R.string.Connect));
                    }
                    return;
                }
                /*
                 * Connect to the given device in list
                 */
                String val = (String) mSpinner.getSelectedItem();
                if (null != val && (!mBt.isConnected())) {
                    mConnectButton.setText(getString(R.string.Connect));
                    mBt.connect(val);
                    if (mBt.isConnected()) {
                        mBt.start();
                    }
                    if (mBt.isConnected()) {
                        mConnectButton.setText(getString(R.string.Disconnect));
                    } else {
                        mConnectButton.setText(getString(R.string.Connect));
                    }
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
                    mBt.setFileSave(null);
                    mFileSave = false;
                }
                else {
                    mConnectFileSaveButton.setText(mContext.getString(R.string.Saving));
                    mBt.setFileSave(val);
                    mFileSave = true;
                }
            }
        });

        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBt.isConnected()) {
            mBt.stop();
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