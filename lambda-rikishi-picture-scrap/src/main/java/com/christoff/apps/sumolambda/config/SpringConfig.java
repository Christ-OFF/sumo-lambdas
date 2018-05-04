package com.christoff.apps.sumolambda.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.christoff.apps.scrappers.RikishiPicturesScrapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.christoff.apps.sumolambda")
public class SpringConfig {

    /**
     * The Lambda function will get it's properties from the env
     * Those properties are set via the admin console
     */
    @Bean
    public RikishisPicturesScrapParameters params() {
        return new RikishisPicturesScrapParameters.Builder()
            .withBaseUrl(System.getenv("baseurl"))
            .withImageUrl(System.getenv("imageurl"))
            .withBucket(System.getenv("bucket"))
            .build();
    }

    @Bean
    Scrapper scrapper(RikishisPicturesScrapParameters params) {
        return new RikishiPicturesScrapper(params);
    }

    @Bean
    AmazonS3 s3Client() {
        return AmazonS3Client.builder().build();
    }

}
