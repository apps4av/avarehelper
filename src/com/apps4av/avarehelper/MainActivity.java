package com.apps4av.avarehelper;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements ListFragment.OnItemSelectedListener {

    private boolean mBound;
    
    private TextView mTextLog;
    
    private IBinder mService;

    /**
     * Shows exit dialog
     */
    private AlertDialog mAlertDialogExit;


    /*
     * (non-Javadoc)
     * @see android.app.Activity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        
        
        /*
         * And may exit
         */
        mAlertDialogExit = new AlertDialog.Builder(MainActivity.this).create();
        mAlertDialogExit.setTitle(getString(R.string.Exit));
        mAlertDialogExit.setCanceledOnTouchOutside(true);
        mAlertDialogExit.setCancelable(true);
        mAlertDialogExit.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.Yes), new DialogInterface.OnClickListener() {
            /* (non-Javadoc)
             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
             */
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Go to background
                 */
                MainActivity.super.onBackPressed();
                dialog.dismiss();
            }
        });
        mAlertDialogExit.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.No), new DialogInterface.OnClickListener() {
            /* (non-Javadoc)
             * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
             */
            public void onClick(DialogInterface dialog, int which) {
                /*
                 * Go to background
                 */
                dialog.dismiss();
            }            
        });

        mAlertDialogExit.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.activity_main, null);
        mTextLog = (TextView)view.findViewById(R.id.main_text_log);
        Logger.setTextView(mTextLog);
        setContentView(view);
        mBound = false;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * Clean up stuff on exit
         */
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        
        if(null != mAlertDialogExit) {
            try {
                mAlertDialogExit.dismiss();
            }
            catch (Exception e) {
            }
        }

    }
   
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
            mBound = true;
            mService = service;
        }

        /* (non-Javadoc)
         * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onRssItemSelected(String link) {
        // TODO Auto-generated method stub
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        BlueToothInFragment btin;
        StatusFragment status;
        WiFiInFragment wfin;
        XplaneFragment xp;
        MsfsFragment msfs;
        BlueToothOutFragment btout;
        FileFragment file;
        GPSSimulatorFragment gpsSim;

        Bundle args = new Bundle();
        args.putBoolean("bound", mBound);
        if(link.equals("layoutStatus")) {
            status = new StatusFragment();
            status.setArguments(args);
            fragmentTransaction.replace(R.id.detailFragment, status);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutBtin")) {
            BlueToothInFragment.init(mService);
            btin = new BlueToothInFragment();
            fragmentTransaction.replace(R.id.detailFragment, btin);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutWifiin")) {
            WiFiInFragment.init(mService);
            wfin = new WiFiInFragment();
            fragmentTransaction.replace(R.id.detailFragment, wfin);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutXplane")) {
            XplaneFragment.init(mService);
            xp = new XplaneFragment();
            fragmentTransaction.replace(R.id.detailFragment, xp);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutMsfs")) {
            MsfsFragment.init(mService);
            msfs = new MsfsFragment();
            fragmentTransaction.replace(R.id.detailFragment, msfs);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutAp")) {
            BlueToothOutFragment.init(mService);
            btout = new BlueToothOutFragment();
            fragmentTransaction.replace(R.id.detailFragment, btout);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutPlay")) {
            FileFragment.init(mService);
            file = new FileFragment();
            fragmentTransaction.replace(R.id.detailFragment, file);
            fragmentTransaction.commit();
        }
        else if(link.equals("layoutGPSSim")) {
            GPSSimulatorFragment.init(mService);
            gpsSim = new GPSSimulatorFragment();
            fragmentTransaction.replace(R.id.detailFragment, gpsSim);
            fragmentTransaction.commit();
        }        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Start the helper service in Avare.
         */
        Intent i = new Intent("com.ds.avare.START_SERVICE");
        i.setClassName("com.ds.avare", "com.ds.avare.IHelperService");
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);

    }

}
