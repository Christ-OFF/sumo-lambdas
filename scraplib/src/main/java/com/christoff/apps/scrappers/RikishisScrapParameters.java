package com.christoff.apps.scrappers;

/**
 * The necessary properties to scrap rikishis
 * 4 parameters for source
 * 1 parameter for destination
 */
public class RikishisScrapParameters implements ScrapPublishParameters {

    /**
     * We may store in code some default values
     */
    public static final String DEFAULT_BASE_URL = "http://sumodb.sumogames.de/";
    public static final String DEFAULT_LIST_QUERY = "Rikishi.aspx?shikona=&heya=-1&shusshin=-1&b=-1&high=-1&hd=-1&entry=-1&intai=999999&sort=1";
    public static final String DEFAULT_RIKISHI_QUERY = "Rikishi.aspx?r=";
    private static final String DEFAULT_EXTRACTONLY = "false";

    /**
     * Should be http://sumodb.sumogames.de/ in production
     */
    private String baseurl;
    private String listurl;
    private String rikishiurl;
    private String extractInfoOnly;
    private String publishDetailTopic;
    private String publishPictureTopic;

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

    public String getPublishDetailTopic() {
        return publishDetailTopic;
    }
    public String getPublishPictureTopic() { return publishPictureTopic; }

    @Override
    public String toString() {
        return "RikishisScrapParameters{" +
            "baseurl='" + baseurl + '\'' +
            ", listurl='" + listurl + '\'' +
            ", rikishiurl='" + rikishiurl + '\'' +
            ", extractInfoOnly='" + extractInfoOnly + '\'' +
            ", publishDetailTopic='" + publishDetailTopic + '\'' +
            ", publishPictureTopic='" + publishPictureTopic + '\'' +
            '}';
    }

    /**
     * Builder providing default values
     */
    public static class Builder {

        private RikishisScrapParameters builded;
        /**
         * Parameters cannot be made null as this is useless !
         */
        public Builder(String publishDetailTopic, String publishPictureTopic) {
            builded = new RikishisScrapParameters();
            builded.baseurl = DEFAULT_BASE_URL;
            builded.listurl = DEFAULT_LIST_QUERY;
            builded.rikishiurl = DEFAULT_RIKISHI_QUERY;
            builded.extractInfoOnly = DEFAULT_EXTRACTONLY;
            // Value without default (topic is not public)
            builded.publishDetailTopic = publishDetailTopic;
            builded.publishPictureTopic = publishPictureTopic;
        }

        public Builder withBaseUrl(String url) {
            if (url != null && !url.isEmpty()) {
                builded.baseurl = url;
            }
            return this;
        }

        public Builder withListUrl(String url) {
            if (url != null && !url.isEmpty()) {
                builded.listurl = url;
            }
            return this;
        }

        public Builder withRikishiUrl(String url) {
            if (url != null && !url.isEmpty()) {
                builded.rikishiurl = url;
            }
            return this;
        }

        public Builder withextractInfoOnly(String extractInfoOnly) {
            if (extractInfoOnly != null && !extractInfoOnly.isEmpty()) {
                builded.extractInfoOnly = extractInfoOnly;
            }
            return this;
        }

        public RikishisScrapParameters build() {
            return builded;
        }
    }
}
