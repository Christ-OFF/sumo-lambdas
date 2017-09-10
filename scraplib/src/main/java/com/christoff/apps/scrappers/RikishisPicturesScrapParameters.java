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

    private RikishisPicturesScrapParameters(Builder builder) {
        this.baseurl = builder.baseurl;
        this.imageurl = builder.imageurl;
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
        private final String baseurl;
        private String imageurl = DEFAULT_PICS_PATH;

        /**
         * We only ask for base url
         *
         * @param baseUrl some url like http://sumodb.sumogames.de/
         */
        public Builder(@NotNull String baseUrl) {
            this.baseurl = baseUrl;
        }

        public Builder imageUrl(@NotNull String url) {
            this.imageurl = url;
            return this;
        }

        public RikishisPicturesScrapParameters build() {
            return new RikishisPicturesScrapParameters(this);
        }
    }
}
