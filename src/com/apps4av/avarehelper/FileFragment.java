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
    private IBinder mService;
    private Button mConnectFileButton;
    private EditText mTextFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        View view = inflater.inflate(R.layout.layout_play, container, false);

        if (null == mService) {
            return view;
        }

        mTextFile = (EditText)view.findViewById(R.id.main_file_name);
        mConnectFileButton = (Button)view.findViewById(R.id.main_button_connect_file);
        mConnectFileButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                /*
                 * If connected, disconnect
                 */
                if(mFile.isConnected()) {
                    mFile.stop();
                    mFile.disconnect();
                    if(mFile.isConnected()) {
                        mConnectFileButton.setText(mContext.getString(R.string.Stop));
                    }
                    else {
                        mConnectFileButton.setText(mContext.getString(R.string.Start));                        
                    }
                    return;
                }
                
                /*
                 * Connect to the given file
                 */
                String val = mTextFile.getText().toString();
                if(null != val && (!mFile.isConnected())) {                    
                    mConnectFileButton.setText(mContext.getString(R.string.Start));
                    mFile.connect(val);
                    if(mFile.isConnected()) {
                        mFile.start();
                    }
                    if(mFile.isConnected()) {
                        mConnectFileButton.setText(mContext.getString(R.string.Stop));
                    }
                    else {
                        mConnectFileButton.setText(mContext.getString(R.string.Start));                        
                    }
                }
            }
        });

        /*
         * List of BT devices is same
         */
        mFile = FileConnectionIn.getInstance();
        mFile.setHelper(IHelper.Stub.asInterface(mService));


        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mFile.isConnected()) {
            mFile.stop();
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