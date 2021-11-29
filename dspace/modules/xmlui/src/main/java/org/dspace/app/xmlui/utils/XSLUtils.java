/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.utils;

import org.apache.commons.lang.StringUtils;
import org.dspace.content.authority.Choice;
import org.dspace.content.authority.Choices;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Utilities that are needed in XSL transformations.
 *
 * @author Art Lowel (art dot lowel at atmire dot com)
 */
public class XSLUtils {

    /*
     * Cuts off the string at the space nearest to the targetLength if there is one within
     * maxDeviation chars from the targetLength, or at the targetLength if no such space is
     * found
     */
    public static String shortenString(String string, int targetLength, int maxDeviation) {
        targetLength = Math.abs(targetLength);
        maxDeviation = Math.abs(maxDeviation);
        if (string == null || string.length() <= targetLength + maxDeviation)
        {
            return string;
        }


        int currentDeviation = 0;
        while (currentDeviation <= maxDeviation) {
            try {
                if (string.charAt(targetLength) == ' ')
                {
                    return string.substring(0, targetLength) + " ...";
                }
                if (string.charAt(targetLength + currentDeviation) == ' ')
                {
                    return string.substring(0, targetLength + currentDeviation) + " ...";
                }
                if (string.charAt(targetLength - currentDeviation) == ' ')
                {
                    return string.substring(0, targetLength - currentDeviation) + " ...";
                }
            } catch (Exception e) {
                //just in case
            }

            currentDeviation++;
        }

        return string.substring(0, targetLength) + " ...";

    }

    public static String lookupAgrovoc(String term, String lang) {
        try {
            URLConnection AgrovocConn = (new URL("https://agrovoc.uniroma2.it/agrovoc/rest/v1/search?query=" + URLEncoder.encode(term, "UTF-8") + "&labellang=" + lang)).openConnection();
            AgrovocConn.setConnectTimeout(5000);
            AgrovocConn.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(AgrovocConn.getInputStream(), "UTF-8"));
            String value = in.readLine();
            in.close();

            if (!StringUtils.isEmpty(value)) {
                //return value;
                //String str= value.toString().replaceAll("\"bav\":\"adv\\d+\",", "").replaceAll("\"dnb\":\"\\d+\",", "");
                JSONObject ob = new JSONObject(value);
                JSONArray results = ob.getJSONArray("results");
                JSONObject result = results.getJSONObject(0);
                return result.getString("prefLabel");
            }
        }      catch (Exception je) {

        }
        return term;
    }
    public static void main (String [] args) {
        //  Test3 test3=new Test3();
        //XSLUtils s = new XSLUtils();
        System.out.println(lookupAgrovoc("Freshwater aquaculture", "th")); }
}
