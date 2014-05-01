/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.apps4av.avarehelper;

import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.ds.avare.IHelper;

/**
 * 
 * @author rasii
 *
 */
public class GPSSimulatorConnection {
	private static double KNOTS_TO_MS = 0.514444f;
	private static double FEET_TO_METERS = 0.3048f;

	private static GPSSimulatorConnection mConnection;
	private static IHelper mHelper;
	private static boolean mRunning;
	private static double mBearing = 45;
	private static double mSpeed = 150f * KNOTS_TO_MS;
	private static double mAltitude = 5000 * FEET_TO_METERS;
	private static double mLon = -94.7376f;
	private static double mLat = 38.8476f;
	private static double mLonInit = -94.7376f;
	private static double mLatInit = 38.8476f;	

	private static boolean mLandAtDest = false;
	private static boolean mFlyToDest = false;
	private static boolean mDestValid = false;
	private static double mDestDistance = -1;
	private static double mDestBearing = -1;
	private static double mDestElevation = 0;

	private Thread mThread;
	private Thread mThreadRcv;
	/**
	 * 
	 */
	 private GPSSimulatorConnection() {
	 }

	 public boolean isRunning() {
		 return mRunning;
	 }

	 public boolean getLandAtDest() {
		 return mLandAtDest;
	 }

	 public boolean getFlyToDest() {
		 return mFlyToDest;
	 }

	 public double getBearing() {
		 return mBearing;
	 }

	 public double getSpeedInKnots() {
		 return mSpeed / KNOTS_TO_MS;
	 }

	 public double getAltitudeInFeet() {
		 return mAltitude / FEET_TO_METERS;
	 }

	 public double getLatitudeInit() {
		 return mLatInit;
	 }

	 public double getLongitudeInit() {
		 return mLonInit;
	 }


	 /**
	  * 
	  * @return
	  */
	  public static GPSSimulatorConnection getInstance() {
		  if(null == mConnection) {
			  mConnection = new GPSSimulatorConnection();
			  mRunning = false;
		  }
		  return mConnection;
	  }

	  public void apply(double bearing, double speedInKnots, double altitudeInFeet, boolean flyToDest, boolean landAtDest) {
		  Logger.Logit("Applying changes");
		  mBearing = bearing;
		  mSpeed = speedInKnots * KNOTS_TO_MS;
		  mAltitude = altitudeInFeet * FEET_TO_METERS;
		  mFlyToDest = flyToDest;
		  mLandAtDest = landAtDest;
	  }

	  /**
	   * 
	   */
	  public void stop() {
		  Logger.Logit("Stopping GPS Simulator");
		  mRunning = false;
		  if(null != mThread) {
			  mThread.interrupt();
		  }
		  if(null != mThreadRcv) {
			  mThreadRcv.interrupt();
		  }
	  }

	  /**
	   * 
	   */
	  public void start(double lat, double lon) {
		  Logger.Logit("Starting GPS Simulator");

		  mRunning = true;
		  mLatInit = lat;
		  mLonInit = lon;
		  mLat = lat;
		  mLon = lon;

		  /*
		   * Thread that recvs destination information
		   */
		  mThreadRcv = new Thread() {
			  @Override
			  public void run() {
				  while(mRunning) {

					  /*
					   * Read data from Avare
					   */
					  String recvd = null;
					  try {
						  recvd = mHelper.recvDataText();
					  } catch (Exception e1) {
						  mDestValid = false;
						  try {
							  Thread.sleep(1000);
						  } 
						  catch (Exception e) {

						  }
						  continue;
					  }

					  if(null == recvd) {
						  mDestValid = false;
						  continue;
					  }

					  try {
						  JSONObject object = new JSONObject(recvd);
						  String type = object.getString("type");
						  if(type == null) {
							  mDestValid = false;
							  continue;
						  }
						  if(type.equals("ownship")) {
							  mDestBearing = object.getDouble("destBearing");
							  mDestDistance = object.getDouble("destDistance");
							  mDestElevation = object.getDouble("destElev") * FEET_TO_METERS;
							  mDestValid = true;
						  }
					  } 
					  catch (JSONException e) {
						  mDestValid = false;
						  continue;
					  }
				  }
			  }
		  };
		  mThreadRcv.start();

		  /*
		   * Thread that sends the GPS updates
		   */
		  mThread = new Thread() {
			  @Override
			  public void run() {
				  int i = 0;
				  double bearing = mBearing;
				  /*
				   * Start the GPS Simulator
				   */
				  while(mRunning) {
					  double time = 0.25; // in seconds
					  if(!mRunning) {
						  break;
					  }

					  try {
						  Thread.sleep((long)(time * 1000));
					  } 
					  catch (Exception e) {

					  }

					  if(mFlyToDest && mDestValid) {
						  // Just keep last bearing if we're getting really close, we don't want
						  // crazy swings at destination passage
						  if(mDestDistance > 0.1) {
							  bearing = mDestBearing;
						  }
					  }
					  else {
						  bearing = mBearing;
					  }

					  double speed = mSpeed;
					  double altitude = mAltitude;

					  // See if we're supposed to land, only do it at the final destination
					  // This is pretty simple logic, but it's something...
					  if(mLandAtDest && mDestValid) {
						  // Get close then stop
						  if(mDestDistance < 10) {
							  // This is somewhat random, but will simulate a descent
							  altitude = mDestElevation + ((mAltitude - mDestElevation) * (mDestDistance / 10.0));

							  // Slow down as we get close, stop when we're really close
							  if(mDestDistance < 0.1) {
								  speed = 0;
								  altitude = mDestElevation;
							  }
							  else if(mDestDistance < 1) {
								  speed = Math.max(mSpeed * mDestDistance/2.0, 20 * KNOTS_TO_MS);
							  }
							  else if(mDestDistance < 5) {
								  speed = mSpeed * ((mDestDistance + 5) / 10.0);
							  }
						  }
					  }

					  double earthRadius = 6371000f; // Earth Radius in meters
					  double distance = speed * time;
					  double degToRad = Math.PI / 180.0;
					  double radToDeg = 180.0 / Math.PI;
					  double lat2 = Math.asin(Math.sin(degToRad * mLat) * 
							  Math.cos(distance / earthRadius) + 
							  Math.cos(degToRad * mLat) * 
							  Math.sin(distance / earthRadius) * 
							  Math.cos(degToRad * bearing));
					  double lon2 = degToRad * mLon + Math.atan2(Math.sin(degToRad * bearing) * 
							  Math.sin(distance / earthRadius) * 
							  Math.cos(degToRad * mLat ), 
							  Math.cos(distance / earthRadius) - 
							  Math.sin(degToRad * mLat) * Math.sin(lat2));                  

					  // Now convert radians to degrees
					  mLat = lat2 * radToDeg;
					  mLon = lon2 * radToDeg;

					  /*
					   * Make a GPS location message
					   */
					  JSONObject object = new JSONObject();
					  try {
						  object.put("type", "ownship");
						  object.put("longitude", mLon);
						  object.put("latitude", mLat);
						  object.put("speed", speed);
						  object.put("bearing", bearing);
						  object.put("altitude", altitude);
						  object.put("time", System.currentTimeMillis());
					  } catch (JSONException e1) {
						  return;
					  }

					  if(mHelper != null) {
						  try {
							  mHelper.sendDataText(object.toString());

							  // Just log every second
							  i++;
							  if(i == 4) {
								  i = 0;
								  Logger.Logit(String.format(Locale.getDefault(), 
										  "lat=%3.4f, lon=%3.4f, spd=%.0f, brg=%.0f, alt=%.0f", 
										  mLat, mLon, speed, bearing, altitude));
							  }
						  } catch (Exception e) {
							  Logger.Logit("Exception sending data: " + e.getMessage());
						  }
					  }

				  }
			  }
		  };
		  mThread.start();
	  }

	  /**
	   * 
	   * @param helper
	   */
	  public void setHelper(IHelper helper) {
		  mHelper = helper;
	  }
}
