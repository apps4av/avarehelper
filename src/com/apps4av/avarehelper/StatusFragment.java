package com.apps4av.avarehelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v4.app.*;

/**
 * 
 * @author zkhan
 *
 */
public class StatusFragment extends Fragment {
    
    private TextView mTv;
    private boolean mBound;
    
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
        View view = inflater.inflate(R.layout.layout_status, container, false);
        mTv = (TextView)view.findViewById(R.id.main_text);
        if(mBound) {
            mTv.setText(getString(R.string.Connected));            
        }
        else {
            mTv.setText(getString(R.string.NotConnected));            
        }
        return view;
    }      
    
    /**
     * 
     * @param bound
     */
    public void setBound(boolean bound) {
        mBound = bound;
    }
} 