package org.dspace.content.authority;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class MESHAuthority implements ChoiceAuthority, AuthorityVariantsSupport {
    Logger log = LogManager.getLogger(MESHAuthority.class);
    String meshurl = "https://id.nlm.nih.gov/mesh/lookup/descriptor";
    private String pluginInstanceName;

    public MESHAuthority() {
    }

    public List<String> getVariants(String key, String locale) {
        if (!StringUtils.isNotBlank(key)) {
            return null;
        } else {
            List<String> variants = new ArrayList();

            for(int i = 0; i < 3; ++i) {
                variants.add(key + "_variant#" + i);
            }

            return variants;
        }
    }

    public Choices getMatches(String text, int start, int limit, String locale) {
        List<BasicNameValuePair> args = new ArrayList();
        args.add(new BasicNameValuePair("label", text));
        String var10000 = this.meshurl;
        String sUrl = var10000 + "?" + URLEncodedUtils.format(args, "UTF8") + "&match=contains&limit=50";

        try {
            URL url = new URL(sUrl);
            InputStream is = url.openStream();
            StringBuffer sb = new StringBuffer();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }

            in.close();
            String str = sb.toString().replaceAll("\"bav\":\"adv\\d+\",", "").replaceAll("\"dnb\":\"\\d+\",", "");
            JSONArray results = new JSONArray(str);
            Choice[] choices = new Choice[results.length()];

            for(int i = 0; i < results.length(); ++i) {
                JSONObject result = results.getJSONObject(i);
                String term = result.getString("label");
                String label = result.getString("label");
                String authority = result.getString("resource");
                choices[i] = new Choice(authority, term, label);
            }

            return new Choices(choices, 0, choices.length, 600, false);
        } catch (MalformedURLException var20) {
            this.log.error(var20.getMessage(), var20);
        } catch (IOException var21) {
            this.log.error(var21.getMessage(), var21);
        }

        return null;
    }

    public Choices getBestMatch(String text, String locale) {
        Choices choices = new Choices(false);
        if (StringUtils.isNotBlank(text)) {
            List<Choice> choiceValues = new ArrayList();
            choiceValues.add(new Choice(text + "_authoritybest", text + "_valuebest", text + "_labelbest"));
            choices = new Choices((Choice[])choiceValues.toArray(new Choice[choiceValues.size()]), 0, 3, 500, false);
        }

        return choices;
    }

    public String getLabel(String key, String locale) {
        return StringUtils.isNotBlank(key) ? key.replaceAll("authority", "label") : "Unknown";
    }

    public String getPluginInstanceName() {
        return this.pluginInstanceName;
    }

    public void setPluginInstanceName(String name) {
        this.pluginInstanceName = name;
    }
}
