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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation to retrieve object from viaf.org "autosuggest" webservice
 * 
 * @see https://viaf.org/
 *
 * @author Riccardo Fazio (riccardo.fazio at 4science dot it)
 *
 */
public class FASTAuthority implements ChoiceAuthority, AuthorityVariantsSupport {
    Logger log = LogManager.getLogger(FASTAuthority.class);
	String fasturl = "https://fast.oclc.org/fastsuggest";
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
        List<BasicNameValuePair> args = new ArrayList<>();
        args.add(new BasicNameValuePair("query", text));
        String sUrl = fasturl + "?" + URLEncodedUtils.format(args, "UTF-8")
                + "&suggest=autoSubject&queryReturn=auth,idroot,suggestall,type&rows=20";

        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "DSpace/8.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                log.error("FASTAuthority HTTP error code: " + responseCode);
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            conn.disconnect();

            JSONObject ob = new JSONObject(sb.toString());
            JSONObject response = ob.getJSONObject("response");
            JSONArray results = response.getJSONArray("docs");

            Choice[] choices = new Choice[results.length()];
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                String term = result.optString("auth");
                String type = result.optString("type");
                JSONArray suggestall = result.optJSONArray("suggestall");
                JSONArray idroot = result.optJSONArray("idroot");

                String authority = idroot != null && idroot.length() > 0 ? idroot.getString(0) : term;
                String label = (type.equals("alt") && suggestall != null && suggestall.length() > 0)
                        ? suggestall.getString(0) + " USE: " + term
                        : term;

                choices[i] = new Choice(authority, term, label);
            }

            return new Choices(choices, 0, choices.length, Choices.CF_ACCEPTED, false);

        } catch (Exception e) {
            log.error("Error querying FAST API: " + e.getMessage(), e);
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
