/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.apps4av.avarehelper.nmea;

import com.apps4av.avarehelper.nmea.Message;
import com.apps4av.avarehelper.nmea.MessageFactory;

/**
 * 
 * @author zkhan
 *
 * http://www.gpsinformation.org/dale/nmea.htm#RMC
 */
public class NMEA {

    /**
     * 
     * @param buffer
     * @param len
     * @return
     */
    public static boolean isNMEA(byte buffer[], int len) {
        byte data[];
        data = new byte[len];
        byte cs[];
        cs = new byte[2];
        System.arraycopy(buffer, 0, data, 0, len);
        /*
         * Starts with $GP, ends with checksum *DD\r\n
         */
        if(data[0] == 36 && data[1] == 71 && data[2] == 80 &&
                data[len - 5] == 42 &&
                data[len - 2] == 13 && data[len - 1] == 10) {
            int xor = 0;
            int i = 1;
            /*
             * Find checksum from after $ to before *
             */
            while(i < len) {
                if(data[i] == 42) {
                    break;
                }
                xor = xor ^ ((int)data[i] & 0xFF);
                i++;
            }
            
            /*
             * Checksum is in xor data[len - 3] and data[len - 4] has checksum in Hex
             */
            System.arraycopy(data, len - 4, cs, 0, 2);
            String css = new String(cs);
            String ma = Integer.toHexString(xor);
            if(ma.equals(css)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 
     * @param bufin
     * @return
     */
    public Message decode(byte[] bufin) {
        
        return MessageFactory.buildMessage(bufin);
    }
}
