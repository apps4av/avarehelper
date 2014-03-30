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
public class FileFragment extends Fragment {

    private FileConnectionIn mFile;
    private Context mContext;
    private static IBinder mService;
    private Button mConnectButton;
    private EditText mTextFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        mContext = container.getContext();

        View view = inflater.inflate(R.layout.layout_play, container, false);

        mTextFile = (EditText)view.findViewById(R.id.main_file_name);
        mConnectButton = (Button)view.findViewById(R.id.main_button_connect_file);
        mConnectButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if(mFile.isConnected()) {
                    mFile.stop();
                    mFile.disconnect();
                    setStates();
                    return;
                }
                
                /*
                 * Connect to the given file
                 */
                String val = mTextFile.getText().toString();
                if(null != val && (!mFile.isConnected())) {                    
                    mConnectButton.setText(mContext.getString(R.string.Start));
                    mFile.connect(val);
                    if(mFile.isConnected()) {
                        mFile.start();
                    }
                    setStates();
                }
            }
        });

        /*
         * List of BT devices is same
         */
        mFile = FileConnectionIn.getInstance();
        if (null != mService) {
            mFile.setHelper(IHelper.Stub.asInterface(mService));
        }

        setStates();
        return view;

    }
    
    /**
     * 
     */
    private void setStates() {
        if(mFile.isConnected()) {
            mConnectButton.setText(mContext.getString(R.string.Stop));
        }
        else {
            mConnectButton.setText(mContext.getString(R.string.Start));                        
        }

        if(mFile.getFileName() != null) {
            mTextFile.setText(mFile.getFileName());
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