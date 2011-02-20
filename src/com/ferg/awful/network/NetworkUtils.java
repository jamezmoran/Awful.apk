/********************************************************************************
 * Copyright (c) 2011, Scott Ferguson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the software nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awful.network;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.ferg.awful.constants.Constants;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    private static DefaultHttpClient sHttpClient;
    private static HtmlCleaner sCleaner;

    public static TagNode get(String aUrl) throws Exception {
        return get(aUrl, null);
    }

	public static TagNode get(String aUrl, HashMap<String, String> aParams) throws Exception {
        TagNode response = null;
        String parameters = getQueryStringParameters(aParams);

        HttpGet httpGet = new HttpGet(aUrl + parameters);

        HttpResponse httpResponse = sHttpClient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();

        if (entity != null) {
            response = sCleaner.clean(new InputStreamReader(entity.getContent()));
        }

		return response;
	}

	public static String post(String aUrl, HashMap<String, String> aParams) throws Exception {
        String response = null;

        HttpPost httpPost = new HttpPost(aUrl);
        httpPost.setEntity(
            new UrlEncodedFormEntity(getPostParameters(aParams)));  

        HttpResponse httpResponse = sHttpClient.execute(httpPost);

        HttpEntity entity = httpResponse.getEntity();

        if (entity != null) {
            response = EntityUtils.toString(entity);
        }

		return response;
	}

    private static ArrayList<NameValuePair> getPostParameters(HashMap<String, String> aParams) {
        // Append parameters
        ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();  

        if (aParams != null) {
            Iterator<?> iter = aParams.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<String, String> param = (Map.Entry<String, String>) iter.next();

                result.add(new BasicNameValuePair(param.getKey(), param.getValue()));  
            }
        }

        return result;
    }

    private static String getQueryStringParameters(HashMap<String, String> aParams) {
        StringBuffer result = new StringBuffer("?");

        if (aParams != null) {
            try {
                // Loop over each parameter and add it to the query string
                Iterator<?> iter = aParams.entrySet().iterator();

                while (iter.hasNext()) {
                    @SuppressWarnings("unchecked")
                        Map.Entry<String, String> param = (Map.Entry<String, String>) iter.next();

                    result.append(param.getKey() + "=" + URLEncoder.encode((String) param.getValue(), "UTF-8"));

                    if (iter.hasNext()) {
                        result.append("&");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                Log.i(TAG, e.toString());
            }
        } else {
            return "";
        }
		
        return result.toString();
    }
    
    static {
        if (sHttpClient == null) {
            sHttpClient = new DefaultHttpClient(); 
        }

        sCleaner = new HtmlCleaner();
        CleanerProperties properties = sCleaner.getProperties();
        properties.setOmitComments(true);
    }
}