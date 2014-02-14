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
public class XplaneFragment extends Fragment {
    
    private XplaneConnection mXp;
    private EditText mTextXplanePort;
    private TextView mTextXplaneIp;
    private CheckBox mXplaneCb;

    private IBinder mService;
    private Context mContext;

    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        
        View view = inflater.inflate(R.layout.layout_xplane, container, false);
        
        mTextXplaneIp = (TextView)view.findViewById(R.id.main_xplane_ip);
        mTextXplanePort = (EditText)view.findViewById(R.id.main_xplane_port);
        mXplaneCb = (CheckBox)view.findViewById(R.id.main_button_xplane_connect);
        mTextXplaneIp.setText(mTextXplaneIp.getText() + "(" + Util.getIpAddr(mContext) + ")");
        mXplaneCb.setOnClickListener(new OnClickListener() {
            
            
            @Override
            public void onClick(View v) {
              if (((CheckBox) v).isChecked()) {
                  try {
                      mXp.connect(Integer.parseInt(mTextXplanePort.getText().toString()));
                  }
                  catch (Exception e) {
                      /*
                       * Number parse
                       */
                      Logger.Logit("Invalid port");
                  }
                  mXp.start();
              }
              else {
                  mXp.stop();
                  mXp.disconnect();
              }
            }
        });


        /*
         * List of BT devices is same
         */
        mXp = XplaneConnection.getInstance();
        if(null != mService) {
            mXp.setHelper(IHelper.Stub.asInterface(mService));
        }
        
        
        return view;  
        
    }

    @Override  
    public void onDestroyView() {  
        super.onDestroyView();
        mXp.disconnect();
        mXp.stop();
    }

    
    /**
     * 
     */
    public void init(Context ctx, IBinder service) {
        mService = service;
        mContext = ctx;
    }
    
} 