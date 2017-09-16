package com.christoff.apps.scrappers;

import org.jetbrains.annotations.Nullable;

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
         * Only default values
         */
        public Builder() {
            builded = new RikishisPicturesScrapParameters();
            builded.baseurl = RikishisScrapParameters.DEFAULT_BASE_URL;
            builded.imageurl = DEFAULT_PICS_PATH;
        }

        public Builder withBaseUrl(@Nullable String url) {
            if (url != null && !url.isEmpty()) {
                builded.baseurl = url;
            }
            return this;
        }

        public Builder withImageUrl(@Nullable String url) {
            if (url != null && !url.isEmpty()) {
                builded.imageurl = url;
            }
            return this;
        }

        public RikishisPicturesScrapParameters build() {
            return builded;
        }
    }
}
