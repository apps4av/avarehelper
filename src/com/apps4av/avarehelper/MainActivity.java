package com.apps4av.avarehelper;


import com.ds.avare.IHelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {

    /**
     * 
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
         */
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            
             IHelper ihelp = IHelper.Stub.asInterface(service);
             
            try {
                ihelp.sendDataText("hello");
            } catch (Exception e) {
            } 
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        
        Intent i = new Intent("com.ds.avare.START_SERVICE");
        i.setClassName("com.ds.avare", "com.ds.avare.IHelperService");
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
