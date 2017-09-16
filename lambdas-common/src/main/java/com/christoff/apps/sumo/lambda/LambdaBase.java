package com.christoff.apps.sumo.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;


/**
 * We have some common methods to all lambdas : at least connect to dynamoDB
 */
public abstract class LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(LambdaBase.class);

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        return new DynamoDBMapper(dynamoDB);
    }


    @Bean
    public AmazonSNS sns() {
        return AmazonSNSClientBuilder.standard().build();
    }

    /**
     * The Lambda function will get it's properties from the env
     * Those properties are set via the admin console
     */
    @Bean
    public RikishisScrapParameters params() {
        return new RikishisScrapParameters.Builder(System.getenv("publishtopic"))
            .withBaseUrl(System.getenv("baseurl"))
            .withListUrl(System.getenv("listurl"))
            .withRikishiUrl(System.getenv("rikishiurl"))
            .withextractInfoOnly(System.getenv("extractInfoOnly"))
            .build();
    }

    /**
     * Common method with heavy null checks to extract id from message
     * @param event message containing raw id
     * @return id or NULL
     */
    protected @Nullable
    Integer rikishiIdFromEvent(SNSEvent event) {
        if (event == null
            || event.getRecords() == null
            || event.getRecords().isEmpty()
            || event.getRecords().get(0) == null
            || event.getRecords().get(0).getSNS() == null
            || event.getRecords().get(0).getSNS().getMessage() == null
            || event.getRecords().get(0).getSNS().getMessage().isEmpty()) {
            LOGGER.error("Event is null or empty");
            return null;
        } else {
            return Integer.parseInt(event.getRecords().get(0).getSNS().getMessage());
        }
    }
}
