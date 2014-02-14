package com.apps4av.avarehelper;


import com.ds.avare.IHelper;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 *
 */
public class MsfsFragment extends Fragment {
    
    private MsfsConnection mMsfs;
    private EditText mTextMsfsPort;
    private TextView mTextMsfsIp;
    private CheckBox mMsfsCb;

    private IBinder mService;
    private Context mContext;

    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        
        View view = inflater.inflate(R.layout.layout_msfs, container, false);
        
        if(null == mService) {
            return view;
        }
        
        mTextMsfsIp = (TextView)view.findViewById(R.id.main_msfs_ip);
        mTextMsfsPort = (EditText)view.findViewById(R.id.main_msfs_port);
        mMsfsCb = (CheckBox)view.findViewById(R.id.main_button_msfs_connect);
        mTextMsfsIp.setText(mTextMsfsIp.getText() + "(" + Util.getIpAddr(mContext) + ")");
        mMsfsCb.setOnClickListener(new OnClickListener() {
            
            
            @Override
            public void onClick(View v) {
              if (((CheckBox) v).isChecked()) {
                  try {
                      mMsfs.connect(Integer.parseInt(mTextMsfsPort.getText().toString()));
                  }
                  catch (Exception e) {
                      /*
                       * Number parse
                       */
                      Logger.Logit("Invalid port");
                  }
                  mMsfs.start();
              }
              else {
                  mMsfs.stop();
                  mMsfs.disconnect();
              }
            }
        });



        /*
         * Get Connection
         */
        mMsfs = MsfsConnection.getInstance();
        mMsfs.setHelper(IHelper.Stub.asInterface(mService));
        
        return view;  
        
    }

    @Override  
    public void onDestroyView() {  
        super.onDestroyView();
        mMsfs.disconnect();
        mMsfs.stop();
    }

    
    /**
     * 
     */
    public void init(Context ctx, IBinder service) {
        mService = service;
        mContext = ctx;
    }
    
} 