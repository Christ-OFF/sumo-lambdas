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

    private RikishisScrapParameters(Builder builder) {
        this.baseurl = builder.baseurl;
        this.listurl = builder.listurl;
        this.rikishiurl = builder.rikishiurl;
        this.extractInfoOnly = builder.extractInfoOnly;
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
        private final String baseurl;
        private String listurl = DEFAULT_LIST_QUERY;
        private String rikishiurl = DEFAULT_RIKISHI_QUERY;
        private String extractInfoOnly = DEFAULT_EXTRACTONLY;

        /**
         * We only ask for base url
         *
         * @param baseUrl some url like http://sumodb.sumogames.de/
         */
        public Builder(@NotNull String baseUrl) {
            this.baseurl = baseUrl;
        }

        public Builder listUrl(@NotNull String url) {
            this.listurl = url;
            return this;
        }

        public Builder rikishiUrl(@NotNull String url) {
            this.rikishiurl = url;
            return this;
        }

        public Builder extractInfoOnly(@NotNull String extractInfoOnly) {
            this.extractInfoOnly = extractInfoOnly;
            return this;
        }

        public RikishisScrapParameters build() {
            return new RikishisScrapParameters(this);
        }
    }
}
