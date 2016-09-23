package com.apps4av.avarehelper;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.apps4av.avarehelper.connections.Connection;
import com.apps4av.avarehelper.connections.GPSSimulatorConnection;
import com.apps4av.avarehelper.storage.Preferences;
import com.apps4av.avarehelper.storage.SavedEditText;

/**
 * 
 * @author rasii
 *
 */
public class GPSSimulatorFragment extends Fragment {

	private Connection mGPSSim;
	private CheckBox mLandAtCb;
	private CheckBox mFlyToCb;
	private SavedEditText mTextLat;
	private SavedEditText mTextLon;
	private SavedEditText mTextHeading;
	private SavedEditText mTextSpeed;
	private SavedEditText mTextAltitude;
	private Button mButtonStart;

	private Context mContext;

	private double getValidValue(String val) {
		double ret = 0;
		if(val.length() > 0) {
		    try {
		        ret = Double.parseDouble(val);
		    }
		    catch (Exception e) {
		        ret = 0;
		    }
		}

		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
		mContext = container.getContext();

		View view = inflater.inflate(R.layout.layout_gpssim, container, false);

		mLandAtCb = (CheckBox)view.findViewById(R.id.main_button_gpssim_land_at);
		mFlyToCb = (CheckBox)view.findViewById(R.id.main_button_gpssim_fly_to);
		mTextLon = (SavedEditText)view.findViewById(R.id.main_gpssim_lon);
		mTextLat = (SavedEditText)view.findViewById(R.id.main_gpssim_lat);
		mTextAltitude = (SavedEditText)view.findViewById(R.id.main_gpssim_altitude);
		mTextSpeed = (SavedEditText)view.findViewById(R.id.main_gpssim_speed);
		mTextHeading = (SavedEditText)view.findViewById(R.id.main_gpssim_heading);
		mButtonStart = (Button)view.findViewById(R.id.main_button_gpssim_start);

		mButtonStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mGPSSim.isConnected()) {
                    mGPSSim.stop();
                    mGPSSim.disconnect();
                }
				else {
                    mGPSSim.connect(
                            getValidValue(mTextLat.getText().toString()) + "," +
                            getValidValue(mTextLon.getText().toString()) + "," +
                            getValidValue(mTextHeading.getText().toString()) + "," +
                            getValidValue(mTextSpeed.getText().toString()) + "," +
                            getValidValue(mTextAltitude.getText().toString()) + "," +
                            mFlyToCb.isChecked() + "," +
                            mLandAtCb.isChecked(),
                            false);
                    mGPSSim.start(new Preferences(mContext));
				}

				setStates();
			}
		});

		/*
		 * Get Connection
		 */
		mGPSSim = GPSSimulatorConnection.getInstance(mContext);

		setStates();

		return view;  
	}

	@Override  
	public void onDestroyView() {  
		super.onDestroyView();
	}

    private void setStates() {
		if(mGPSSim.isConnected()) {
			mButtonStart.setText(mContext.getString(R.string.Stop));
		}
		else {
			mButtonStart.setText(mContext.getString(R.string.Start));
		}

        // lat, lon, alt, bearing, speed, landatdest, flytodest
        String params[] = mGPSSim.getParam().split(",");


		mTextLat.setText(String.format("%.4f", getValidValue(params[0])));
		mTextLon.setText(String.format("%.4f", getValidValue(params[1])));
        mTextHeading.setText(String.format("%.0f", getValidValue(params[2])));
        mTextSpeed.setText(String.format("%.0f", getValidValue(params[3])));
		mTextAltitude.setText(String.format("%.0f", getValidValue(params[4])));
        mFlyToCb.setChecked(Boolean.parseBoolean(params[5]));
        mLandAtCb.setChecked(Boolean.parseBoolean(params[6]));
	}

} 