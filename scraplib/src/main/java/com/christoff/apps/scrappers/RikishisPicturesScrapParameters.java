package com.christoff.apps.scrappers;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

/**
 * The necessary properties to scrap rikishis pictures
 */
public class RikishisPicturesScrapParameters {

    public static final Float DEFAULT_QUALITY = 0.3F;

    public static final String DEFAULT_PICS_PATH = "pics/";
    public static final String DEFAULT_BUCKET = "rikishis";
    private static final Logger LOGGER = Logger.getLogger(RikishisPicturesScrapParameters.class);
    /**
     * Should be http://sumodb.sumogames.de/ in production
     */
    private String baseurl;
    private String imageurl;

    /**
     * The S3 bucket "Folder"
     */
    private String bucket;
    private Float quality;

    /**
     * Only builder can build
     */
    private RikishisPicturesScrapParameters() {
    }

    public String getFullImageUrl() {
        return baseurl + imageurl;
    }

    public String getBucket() {
        return bucket;
    }

    public Float getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return "RikishisPicturesScrapParameters{" +
            "baseurl='" + baseurl + '\'' +
            ", imageurl='" + imageurl + '\'' +
            ", bucket='" + bucket + '\'' +
            ", quality=" + quality +
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
            builded.bucket = DEFAULT_BUCKET;
            builded.quality = DEFAULT_QUALITY;
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

        public Builder withBucket(@Nullable String bucket) {
            if (bucket != null && !bucket.isEmpty()) {
                builded.bucket = bucket;
            }
            return this;
        }

        public Builder withQuality(@Nullable String paramQuality) {
            if (paramQuality != null && !paramQuality.isEmpty()) {
                Float quality = DEFAULT_QUALITY;
                try {
                    quality = Float.parseFloat(paramQuality);
                } catch (NumberFormatException nfe) {
                    LOGGER.warn("Bad quality parameter " + paramQuality + " applying default", nfe);
                }
                builded.quality = quality;
            }
            return this;
        }

        public RikishisPicturesScrapParameters build() {
            return builded;
        }
    }
}
