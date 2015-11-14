/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper.connections;

import android.widget.CheckBox;

import com.apps4av.avarehelper.gdl90.BasicReportMessage;
import com.apps4av.avarehelper.gdl90.Constants;
import com.apps4av.avarehelper.gdl90.FisBuffer;
import com.apps4av.avarehelper.gdl90.Id413Product;
import com.apps4av.avarehelper.gdl90.Id6364Product;
import com.apps4av.avarehelper.gdl90.LongReportMessage;
import com.apps4av.avarehelper.gdl90.OwnshipGeometricAltitudeMessage;
import com.apps4av.avarehelper.gdl90.OwnshipMessage;
import com.apps4av.avarehelper.gdl90.Product;
import com.apps4av.avarehelper.gdl90.TrafficReportMessage;
import com.apps4av.avarehelper.gdl90.UplinkMessage;
import com.apps4av.avarehelper.nmea.Ownship;
import com.apps4av.avarehelper.nmea.RTMMessage;
import com.apps4av.avarehelper.storage.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

/**
 * 
 * @author zkhan
 *
 */
public class BufferProcessor {

    private int mGeoAltitude = Integer.MIN_VALUE;
    private long mLastTime = Long.MIN_VALUE; //   Cache for last msg time to allow pacing.  Should be instance variable, but no constructor.

    com.apps4av.avarehelper.gdl90.DataBuffer dbuffer = 
            new com.apps4av.avarehelper.gdl90.DataBuffer(16384);
    com.apps4av.avarehelper.nmea.DataBuffer nbuffer = 
            new com.apps4av.avarehelper.nmea.DataBuffer(16384);
    com.apps4av.avarehelper.gdl90.Decode decode = 
            new com.apps4av.avarehelper.gdl90.Decode();
    com.apps4av.avarehelper.nmea.Decode ndecode = 
            new com.apps4av.avarehelper.nmea.Decode();
    Ownship nmeaOwnship = new Ownship();
    CheckBox paceOutput;

    private void pace(long mTime) {  // method to pace messages when used with fileplayback

        int deltaT;

            // Compute time since last message.  If negative, assume 0.
            //  If set a maximum time, since this will delay updates.
            deltaT = (int) java.lang.Math.min((int) java.lang.Math.max(0, (mTime - mLastTime)/2 ), 2000);
            mLastTime = java.lang.Math.max(mTime, mLastTime); // Update if later.
            //  Wait deltaT milliseconds.
            try {
                Thread.sleep(deltaT);
            } catch (InterruptedException e) {
                return;  // Not sure this is correct. REVIEW
            }
    }
    /**
     * 
     * @param buffer
     * @param red
     */
    public void put(byte [] buffer, int red) {
        nbuffer.put(buffer, red);                     
        dbuffer.put(buffer, red);
    }

    /**
     * 
     * @return
     */
    public LinkedList<String> decode(Preferences pref) {

        LinkedList<String> objs = new LinkedList<String>();

        long mTime = 0;

        byte[] buf;

        while(null != (buf = nbuffer.get())) {
            com.apps4av.avarehelper.nmea.Message m = ndecode.decode(buf);
            
            if(m instanceof RTMMessage) {

                /*
                 * Make a GPS locaiton message from ADSB ownship message.
                 */
                JSONObject object = new JSONObject();
                RTMMessage tm = (RTMMessage)m;
                try {
                    object.put("type", "traffic");
                    object.put("longitude", (double)tm.mLon);
                    object.put("latitude", (double)tm.mLat);
                    object.put("speed", (double)(tm.mSpeed));
                    object.put("bearing", (double)tm.mDirection);
                    object.put("altitude", (double)((double)tm.mAltitude));
                    object.put("callsign", (String)"");
                    object.put("address", (int)tm.mIcaoAddress);
                    mTime=(long) tm.getTime();
                    object.put("time", mTime);

                } catch (JSONException e1) {
                    continue;
                }

                objs.add(object.toString());

            }

            else if(nmeaOwnship.addMessage(m)) {
                    
                /*
                 * Make a GPS locaiton message from ADSB ownship message.
                 */
                JSONObject object = new JSONObject();
                Ownship om = nmeaOwnship;
                try {
                    object.put("type", "ownship");
                    object.put("longitude", (double)om.mLon);
                    object.put("latitude", (double)om.mLat);
                    object.put("speed", (double)(om.mHorizontalVelocity));
                    object.put("bearing", (double)om.mDirection);
                    object.put("altitude", (double)((double)om.mAltitude));
                    mTime=(long) om.getTime();
                    object.put("time", mTime);
                } catch (JSONException e1) {
                    continue;
                }

                objs.add(object.toString());
            }
        }

        while(null != (buf = dbuffer.get())) {

            /*
             * Get packets, decode
             */
            com.apps4av.avarehelper.gdl90.Message m = decode.decode(buf);
            /*
             * Post on UI thread.
             */
            
            if(m instanceof TrafficReportMessage) {
                
                /*
                 * Make a GPS locaiton message from ADSB ownship message.
                 */
                JSONObject object = new JSONObject();
                TrafficReportMessage tm = (TrafficReportMessage)m;
                try {
                    object.put("type", "traffic");
                    object.put("longitude", (double)tm.mLon);
                    object.put("latitude", (double)tm.mLat);
                    object.put("speed", (double)(tm.mHorizVelocity));
                    object.put("bearing", (double)tm.mHeading);
                    object.put("altitude", (double)((double)tm.mAltitude));
                    object.put("callsign", (String)tm.mCallSign);
                    object.put("address", (int)tm.mIcaoAddress);
                    mTime=(long) tm.getTime();
                    object.put("time", mTime);
                } catch (JSONException e1) {
                    continue;
                }                
                
                objs.add(object.toString());

            }

            else if(m instanceof BasicReportMessage) {

                /*
                 * Make a GPS locaiton message from ADSB ownship message.
                 */
                JSONObject object = new JSONObject();
                BasicReportMessage tm = (BasicReportMessage)m;
                try {
                    object.put("type", "traffic");
                    object.put("longitude", (double)tm.mLon);
                    object.put("latitude", (double)tm.mLat);
                    object.put("speed", (double)(tm.mSpeed));
                    object.put("bearing", (double)tm.mHeading);
                    object.put("altitude", (double)((double)tm.mAltitude));
                    object.put("callsign", (String)tm.mCallSign);
                    object.put("address", (int)tm.mIcaoAddress);
                    mTime=(long) tm.getTime();
                    object.put("time", mTime);
                } catch (JSONException e1) {
                    continue;
                }

                objs.add(object.toString());
            }

            else if(m instanceof LongReportMessage) {

                /*
                 * Make a GPS locaiton message from ADSB ownship message.
                 */
                JSONObject object = new JSONObject();
                LongReportMessage tm = (LongReportMessage)m;
                try {
                    object.put("type", "traffic");
                    object.put("longitude", (double)tm.mLon);
                    object.put("latitude", (double)tm.mLat);
                    object.put("speed", (double)(tm.mSpeed));
                    object.put("bearing", (double)tm.mHeading);
                    object.put("altitude", (double)((double)tm.mAltitude));
                    object.put("callsign", (String)tm.mCallSign);
                    object.put("address", (int)tm.mIcaoAddress);
                    mTime=(long) tm.getTime();
                    object.put("time", mTime);
                } catch (JSONException e1) {
                    continue;
                }

                objs.add(object.toString());
            }

            else if(m instanceof OwnshipGeometricAltitudeMessage) {
                mGeoAltitude = ((OwnshipGeometricAltitudeMessage)m).mAltitudeWGS84;
            }

            else if(m instanceof UplinkMessage) {
                /*
                 * Send an uplink nexrad message
                 */
                FisBuffer fis = ((UplinkMessage) m).getFis();
                if(null == fis) {
                    continue;
                }
                LinkedList<Product> pds = fis.getProducts();
                for(Product p : pds) {
                    if(p instanceof Id6364Product) {
                        Id6364Product pn = (Id6364Product)p;
                        JSONObject object = new JSONObject();

                        JSONArray arrayEmpty = new JSONArray();
                        JSONArray arrayData = new JSONArray();

                        int[] data = pn.getData();
                        if(null != data) {
                            for(int i = 0; i < data.length; i++) {
                                arrayData.put(data[i]);
                            }
                        }
                        LinkedList<Integer> empty = pn.getEmpty();
                        if(null != empty) {
                            for(int e : empty) {
                                arrayEmpty.put(e);
                            }
                        }

                        try {
                            object.put("type", "nexrad");
                            mTime= (long) pn.getTime().getTimeInMillis();
                            object.put("time", mTime);
                            object.put("conus", pn.isConus());
                            object.put("blocknumber", (long)pn.getBlockNumber());
                            object.put("x", Constants.COLS_PER_BIN);
                            object.put("y", Constants.ROWS_PER_BIN);
                            object.put("empty", arrayEmpty);
                            object.put("data", arrayData);
                        } catch (JSONException e1) {
                            continue;
                        }

                        objs.add(object.toString());
                    }
                    /*
                     * Text product
                     */
                    else if(p instanceof Id413Product) {
                        Id413Product pn = (Id413Product)p;
                        JSONObject object = new JSONObject();

                        String data = pn.getData();
                        String type = pn.getHeader();
                        long time = (long)pn.getTime().getTimeInMillis();

                        /*
                         * Clear garbage spaces etc. Convert to Avare format
                         */
                        try {
                            if(type.equals("WINDS")) {

                                String tokens[] = data.split("\n");
                                if(tokens.length < 2) {
                                    /*
                                     * Must have line like
                                     * MSY 230000Z  FT 3000 6000    F9000   C12000  G18000  C24000  C30000  D34000  39000   Y
                                     * and second line like
                                     * 1410 2508+10 2521+07 2620+01 3037-12 3041-26 304843 295251 29765
                                     */
                                    continue;
                                }

                                tokens[0] = tokens[0].replaceAll("\\s+", " ");
                                tokens[1] = tokens[1].replaceAll("\\s+", " ");
                                String winds[] = tokens[1].split(" ");
                                String alts[] = tokens[0].split(" ");

                                /*
                                 * Start from 3rd entry - alts
                                 */
                                data = "";
                                boolean found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("3000") && !alts[i].contains("30000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("6000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("9000") && !alts[i].contains("39000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("12000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("18000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("24000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("30000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("34000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                                found = false;
                                for(int i = 2; i < alts.length; i++) {
                                    if(alts[i].contains("39000")) {
                                        data += winds[i - 2] + ",";
                                        found = true;
                                    }
                                }
                                if(!found) {
                                    data += ",";
                                }
                            }
                        }
                        catch (Exception e) {
                            continue;
                        }

                        try {
                            object.put("type", pn.getHeader());
                            mTime=time;
                            object.put("time", time);
                            object.put("location", pn.getLocation());
                            object.put("data", data);
                        } catch (JSONException e1) {
                            continue;
                        }

                        objs.add(object.toString());
                    }
                }
            }
            else if(m instanceof OwnshipMessage) {

                /*
                 * Make a GPS locaiton message from ADSB ownship message.
                 */
                JSONObject object = new JSONObject();
                OwnshipMessage om = (OwnshipMessage)m;
                try {
                    object.put("type", "ownship");
                    object.put("longitude", (double)om.mLon);
                    object.put("latitude", (double)om.mLat);
                    object.put("speed", (double)(om.mHorizontalVelocity));
                    object.put("bearing", (double)om.mDirection);
                    mTime=(long) om.getTime();
                    object.put("time", mTime);
                    int altitude = -1000;
                    if(pref.getGeoAltitude()) {
                        altitude = mGeoAltitude;
                    }
                    else {
                        altitude = om.mAltitude;
                    }
                    if(altitude < -1000) {
                        altitude = -1000;
                    }
                    object.put("altitude", (double) altitude);
                } catch (JSONException e1) {
                    continue;
                }

                objs.add(object.toString());
            }
        }
        if (paceOutput != null && paceOutput.isChecked()) pace(mTime);
        return objs;
    }
}
