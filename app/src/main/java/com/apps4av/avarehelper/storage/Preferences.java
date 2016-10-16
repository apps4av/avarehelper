/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper.storage;



import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Preferences for main activity
 */
public class Preferences {

    
    /**
     * Preferences
     */
    private SharedPreferences mPref;

    /**
     * 
     * @param ctx
     */
    public Preferences(Context ctx) {
        /*
         * Get prefs
         */
        mPref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }


    /**
     * 
     * @return
     */
    public String getEditTextValue(int id) {
        return mPref.getString("EditText" + id, null);
    }

    /**
     * 
     */
    public void setEditTextValue(int id, String val) {
        mPref.edit().putString("EditText" + id, val).commit();
    }

    /**
     * 
     * @return
     */
    public boolean getCheckboxValue(int id) {
        return mPref.getBoolean("Checkbox" + id, false);
    }

    /**
     * 
     */
    public void setCheckboxValue(int id, boolean val) {
        mPref.edit().putBoolean("Checkbox" + id, val).commit();
    }

    /**
     *
     */
    public int getFragmentIndex() {
        return mPref.getInt("fragmentindex", 0);
    }

    /**
     *
     * @param fragmentIndex
     */
    public void setFragmentIndex(int fragmentIndex) {
        mPref.edit().putInt("fragmentindex", fragmentIndex).commit();
    }
}
