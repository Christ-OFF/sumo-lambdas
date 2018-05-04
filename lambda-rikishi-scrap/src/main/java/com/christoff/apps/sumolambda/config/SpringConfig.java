package com.christoff.apps.sumolambda.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.christoff.apps.sumolambda")
public class SpringConfig {

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        return new DynamoDBMapper(dynamoDB);
    }

    /**
     * The Lambda function will get it's properties from the env
     * Those properties are set via the admin console
     */
    @Bean
    public RikishisScrapParameters params() {
        return new RikishisScrapParameters.Builder(
            System.getenv(LambdaScrapBase.PUBLISH_DETAIL_TOPIC),
            System.getenv(LambdaScrapBase.PUBLISH_PICTURE_TOPIC))
            .withBaseUrl(System.getenv("baseurl"))
            .withListUrl(System.getenv("listurl"))
            .withRikishiUrl(System.getenv("rikishiurl"))
            .withextractInfoOnly(System.getenv("extractInfoOnly"))
            .build();
    }

    @Bean
    public AmazonSNS sns() {
        return AmazonSNSClientBuilder.standard().build();
    }

    @Bean
    Scrapper scrapper(RikishisScrapParameters params) {
        return new RikishisScrapper(params);
    }

}
