/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String isoLanguageToDisplay(String iso) {
        if (StringUtils.isBlank(iso)) {
            return iso;
        }
        Locale locale;
        if (iso.contains("_")) {
            String language = iso.substring(0, iso.indexOf("_"));
            locale = new Locale(language);
        } else {
            locale = new Locale(iso);
        }
        String englishNameOfLanguage = locale.getDisplayLanguage(Locale.getDefault());
        if (!StringUtils.isBlank(englishNameOfLanguage))
        {
            return englishNameOfLanguage;
        }
        return iso;
    }

    public static String abbreviateMiddle(String input, String middle, int length) {
        if (input != null && input.length() > length) {
            int half = (length - middle.length()) / 2;

            Pattern pattern = Pattern.compile(
                    "^(.{" + half + ",}?)" + "\\b.*\\b" + "(.{" + half + ",}?)$");
            Matcher matcher = pattern.matcher(input);

            if (matcher.matches()) {
                return matcher.group(1) + middle + matcher.group(2);
            }
        }

        return input;
    }
}