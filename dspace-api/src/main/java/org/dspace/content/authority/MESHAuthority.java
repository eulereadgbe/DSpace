/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a *very* stupid test fixture for authority control with AuthorityVariantsSupport.
 *
 * @author Andrea Bollini (CILEA)
 */
public class MESHAuthority implements ChoiceAuthority, AuthorityVariantsSupport {
    Logger log = LogManager.getLogger(MESHAuthority.class);
    String meshurl = "https://id.nlm.nih.gov/mesh/lookup/descriptor";
    private String pluginInstanceName;

    @Override
    public List<String> getVariants(String key, String locale) {
        if (StringUtils.isNotBlank(key)) {
            List<String> variants = new ArrayList<String>();
            for (int i = 0; i < 3; i++) {
                variants.add(key + "_variant#" + i);
            }
            return variants;
        }
        return null;
    }

    @Override
    public Choices getMatches(String text, int start, int limit, String locale) {
        List<BasicNameValuePair> args = new ArrayList<BasicNameValuePair>();
        args.add(new BasicNameValuePair("label", text));
        String sUrl = meshurl + "?" + URLEncodedUtils.format(args, "UTF8") + "&match=contains&limit=50";
        try {
            URL url = new URL(sUrl);
            InputStream is = url.openStream();
            StringBuffer sb = new StringBuffer();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null){
                sb.append(inputLine);
            }
            in.close();

            //VIAF responds a json with duplicate keys? must remove them as they are unused
            String str= sb.toString().replaceAll("\"bav\":\"adv\\d+\",", "").replaceAll("\"dnb\":\"\\d+\",", "");
            //JSONObject ob = new JSONObject(str);
            JSONArray results = new JSONArray(str);

            Choice[] choices = new Choice[results.length()];
            for(int i=0;i< results.length();i++){
                JSONObject result = results.getJSONObject(i);
                String term = result.getString("label");
                String label = result.getString("label");
                String authority = result.getString("resource");
                choices[i] = new Choice(authority, term, label);
            }

            return new Choices(choices, 0, choices.length, Choices.CF_ACCEPTED, false);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(),e);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }

        return null;
    }

    @Override
    public Choices getBestMatch(String text, String locale) {
        Choices choices = new Choices(false);
        if (StringUtils.isNotBlank(text)) {

            List<Choice> choiceValues = new ArrayList<Choice>();

            choiceValues.add(new Choice(text + "_authoritybest", text
                + "_valuebest", text + "_labelbest"));

            choices = new Choices(
                (Choice[]) choiceValues.toArray(new Choice[choiceValues
                    .size()]), 0, 3, Choices.CF_UNCERTAIN, false);
        }
        return choices;
    }

    @Override
    public String getLabel(String key, String locale) {
        if (StringUtils.isNotBlank(key)) {
            return key.replaceAll("authority", "label");
        }
        return "Unknown";
    }

    @Override
    public String getPluginInstanceName() {
        return pluginInstanceName;
    }

    @Override
    public void setPluginInstanceName(String name) {
        this.pluginInstanceName = name;
    }
}
