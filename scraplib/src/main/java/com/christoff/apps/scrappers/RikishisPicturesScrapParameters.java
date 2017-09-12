package com.christoff.apps.scrappers;

import org.jetbrains.annotations.NotNull;

/**
 * The necessary properties to scrap rikishis pictures
 */
public class RikishisPicturesScrapParameters {

    public static final String DEFAULT_PICS_PATH = "pics/";
    /**
     * Should be http://sumodb.sumogames.de/ in production
     */
    private String baseurl;
    private String imageurl;

    /**
     * Only builder can build
     */
    private RikishisPicturesScrapParameters() {
    }

    public String getFullImageUrl() {
        return baseurl + imageurl;
    }

    public boolean isValid() {
        return baseurl != null
            && !baseurl.isEmpty()
            && imageurl != null
            && !imageurl.isEmpty();
    }

    @Override
    public String toString() {
        return "RikishisPicturesScrapParameters{" +
            "baseurl='" + baseurl + '\'' +
            ", imageurl='" + imageurl + '\'' +
            '}';
    }

    /**
     * Builder providing default values
     */
    public static class Builder {
        private RikishisPicturesScrapParameters builded;

        /**
         * We only ask for base url
         *
         * @param baseUrl some url like http://sumodb.sumogames.de/
         */
        public Builder(@NotNull String baseUrl) {
            builded = new RikishisPicturesScrapParameters();
            builded.baseurl = baseUrl;
            builded.imageurl = DEFAULT_PICS_PATH;
        }

        public Builder imageUrl(@NotNull String url) {
            builded.imageurl = url;
            return this;
        }

        public RikishisPicturesScrapParameters build() {
            return builded;
        }
    }
}
