package com.christoff.apps.scrappers;

import org.jetbrains.annotations.NotNull;

/**
 * The necessary properties to scrap rikishis
 */
public class RikishisScrapParameters {

    public static final String DEFAULT_LIST_QUERY = "Rikishi.aspx?shikona=&heya=-1&shusshin=-1&b=-1&high=-1&hd=-1&entry=-1&intai=999999&sort=1";
    public static final String DEFAULT_RIKISHI_QUERY = "Rikishi.aspx?r=";
    public static final String DEFAULT_EXTRACTONLY = "false";
    /**
     * Should be http://sumodb.sumogames.de/ in production
     */
    private String baseurl;
    private String listurl;
    private String rikishiurl;
    private String extractInfoOnly;

    /**
     * Only builder can build
     */
    private RikishisScrapParameters() {
    }

    public String getFullListUrl() {
        return baseurl + listurl;
    }

    public String getFullRikishiUrl() {
        return baseurl + rikishiurl;
    }

    public boolean getExtractInfoOnly() {
        return Boolean.valueOf(extractInfoOnly);
    }

    public boolean isValid() {
        return baseurl != null
            && !baseurl.isEmpty()
            && listurl != null
            && !listurl.isEmpty()
            && rikishiurl != null
            && !rikishiurl.isEmpty()
            && extractInfoOnly != null
            && !extractInfoOnly.isEmpty();
    }

    @Override
    public String toString() {
        return "RikishisScrapParameters{" +
            "baseurl='" + baseurl + '\'' +
            ", listurl='" + listurl + '\'' +
            ", rikishiurl='" + rikishiurl + '\'' +
            ", extractInfoOnly='" + extractInfoOnly + '\'' +
            '}';
    }

    /**
     * Builder providing default values
     */
    public static class Builder {

        private RikishisScrapParameters builded;
        /**
         * We only ask for base url
         *
         * @param baseUrl some url like http://sumodb.sumogames.de/
         */
        public Builder(@NotNull String baseUrl) {
            builded = new RikishisScrapParameters();
            // only mandatory
            builded.baseurl = baseUrl;
            // the default value
            builded.listurl = DEFAULT_LIST_QUERY;
            builded.rikishiurl = DEFAULT_RIKISHI_QUERY;
            builded.extractInfoOnly = DEFAULT_EXTRACTONLY;
        }

        public Builder withListUrl(String url) {
            builded.listurl = url;
            return this;
        }

        public Builder withRikishiUrl(String url) {
            builded.rikishiurl = url;
            return this;
        }

        public Builder withextractInfoOnly(String extractInfoOnly) {
            builded.extractInfoOnly = extractInfoOnly;
            return this;
        }

        public RikishisScrapParameters build() {
            return builded;
        }
    }
}
