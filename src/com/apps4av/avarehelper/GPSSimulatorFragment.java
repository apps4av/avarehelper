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
import android.widget.CheckBox;
import android.widget.EditText;
import android.support.v4.app.*;

/**
 * 
 * @author rasii
 *
 */
public class GPSSimulatorFragment extends Fragment {

	private GPSSimulatorConnection mGPSSim;
	private CheckBox mLandAtCb;
	private CheckBox mFlyToCb;
	private EditText mTextLat;
	private EditText mTextLon;
	private EditText mTextHeading;
	private EditText mTextSpeed;
	private EditText mTextAltitude;
	private Button mButtonStart;
	private Button mButtonApply;

	private static IBinder mService;
	private Context mContext;

	private double getValidValue(String val) {
		double ret = 0;
		if(val.length() > 0) {
			ret = Double.parseDouble(val);
		}

		return ret;
	}

	private void apply() {
		// Make sure there are valid values
		mGPSSim.apply(getValidValue(mTextHeading.getText().toString()),
				getValidValue(mTextSpeed.getText().toString()),
				getValidValue(mTextAltitude.getText().toString()),
				mFlyToCb.isChecked(),
				mLandAtCb.isChecked());
	}

	@Override  
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
		mContext = container.getContext();

		View view = inflater.inflate(R.layout.layout_gpssim, container, false);

		mLandAtCb = (CheckBox)view.findViewById(R.id.main_button_gpssim_land_at);
		mFlyToCb = (CheckBox)view.findViewById(R.id.main_button_gpssim_fly_to);
		mTextLon = (EditText)view.findViewById(R.id.main_gpssim_lon);
		mTextLat = (EditText)view.findViewById(R.id.main_gpssim_lat);
		mTextAltitude = (EditText)view.findViewById(R.id.main_gpssim_altitude);
		mTextSpeed = (EditText)view.findViewById(R.id.main_gpssim_speed);
		mTextHeading = (EditText)view.findViewById(R.id.main_gpssim_heading);
		mButtonApply =(Button)view.findViewById(R.id.main_button_gpssim_apply);
		mButtonStart = (Button)view.findViewById(R.id.main_button_gpssim_start);

		mButtonStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGPSSim.isRunning()) {
					mGPSSim.stop();
				}
				else {
					apply();
					mGPSSim.start(getValidValue(mTextLat.getText().toString()),
							getValidValue(mTextLon.getText().toString()));
				}

				setStates();
			}
		});

		mButtonApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGPSSim.isRunning()) {
					apply();
				}
			}
		});
		/*
		 * Get Connection
		 */
		 mGPSSim = GPSSimulatorConnection.getInstance();
		if(null != mService) {
			mGPSSim.setHelper(IHelper.Stub.asInterface(mService));
		}

		setStates();

		return view;  
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

	private void setStates() {
		if(mGPSSim.isRunning()) {
			mButtonStart.setText(mContext.getString(R.string.Stop));
			mButtonApply.setEnabled(true);
		}
		else {
			mButtonStart.setText(mContext.getString(R.string.Start));
			mButtonApply.setEnabled(false);
		}

		mLandAtCb.setChecked(mGPSSim.getLandAtDest());
		mFlyToCb.setChecked(mGPSSim.getFlyToDest());
		mTextLat.setText(String.format("%.4f", mGPSSim.getLatitudeInit()));
		mTextLon.setText(String.format("%.4f", mGPSSim.getLongitudeInit()));
		mTextAltitude.setText(String.format("%.0f", mGPSSim.getAltitudeInFeet()));
		mTextHeading.setText(String.format("%.0f", mGPSSim.getBearing()));
		mTextSpeed.setText(String.format("%.0f", mGPSSim.getSpeedInKnots()));
	}    

} 