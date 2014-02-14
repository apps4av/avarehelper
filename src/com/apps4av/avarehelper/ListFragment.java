package com.apps4av.avarehelper;

import android.app.Activity;  
import android.support.v4.app.*;
import android.os.Bundle;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.Button;  

/**
 * 
 * @author zkhan
 *
 */
public class ListFragment extends Fragment {  
     
    private OnItemSelectedListener listener;  
  
    @Override  
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.listf, container, false);  
      
        Button mButtonBtin = (Button) view.findViewById(R.id.listf_btn_btin);  
        Button mButtonWifiIn = (Button) view.findViewById(R.id.listf_btn_wifiin);  
        Button mButtonXplane = (Button) view.findViewById(R.id.listf_btn_xplane);
        Button mButtonMsfs = (Button) view.findViewById(R.id.listf_btn_msfs);  
        Button mButtonAp = (Button) view.findViewById(R.id.listf_btn_ap);  
        Button mButtonPlay = (Button) view.findViewById(R.id.listf_btn_play);  
        Button mButtonStatus = (Button) view.findViewById(R.id.listf_btn_status);  

        mButtonStatus.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutStatus");  
            }  
        });  

        mButtonBtin.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutBtin");  
            }  
        });  
          
        mButtonWifiIn.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutWifiin");  
            }  
        });  
                   
        mButtonXplane.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutXplane");  
            }  
        });  
                   
        mButtonMsfs.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutMsfs");  
            }  
        });  
                   
        mButtonAp.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutAp");  
            }  
        });  
                   
        mButtonPlay.setOnClickListener(new View.OnClickListener() {  
            public void onClick(View v) {                 
                updateDetail("layoutPlay");  
            }  
        });  
                   
        return view;  
    }  
      

    /**
     * 
     * @author zkhan
     *
     */
    public interface OnItemSelectedListener {  
        public void onRssItemSelected(String link);  
    }
     
    /**
     * 
     */
    @Override  
    public void onAttach(Activity activity) {  
        super.onAttach(activity);  
        if (activity instanceof OnItemSelectedListener) {  
            listener = (OnItemSelectedListener) activity;  
        } 
        else {  
            throw new ClassCastException(activity.toString() + " must implemenet MyListFragment.OnItemSelectedListener");  
        }  
    }  
  
    /**
     * 
     * @param s
     */
    public void updateDetail(String s) {          
        listener.onRssItemSelected(s);  
    }  
}