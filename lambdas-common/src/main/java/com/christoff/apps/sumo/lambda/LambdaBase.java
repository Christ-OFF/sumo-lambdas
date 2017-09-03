package com.christoff.apps.sumo.lambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.apache.log4j.Logger;


/**
 * We have some common methods to all lambdas : at least connect to dynamoDB
 */
public abstract class LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(LambdaBase.class);

    /**
     * The moste important method :
     * We need to connect to dynamo DB
     */
    protected AmazonDynamoDB getDynamoDbClient(boolean local) {
        AmazonDynamoDB client;
        if (local) {
            LOGGER.info("Building DynamoDB client for AWS Cloud");
            client = AmazonDynamoDBClientBuilder.standard().build();
        } else {
            LOGGER.info("Building DynamoDB client for LOCAL");
            client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
                .build();
        }
        return client;
    }
}
