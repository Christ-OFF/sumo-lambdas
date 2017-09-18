package com.christoff.apps.sumo.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;


/**
 * We have some common methods to all lambdas : at least connect to dynamoDB
 */
public abstract class LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(LambdaBase.class);

    /**
     * This is the only bean necessary for all
     *
     * @return
     */
    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        return new DynamoDBMapper(dynamoDB);
    }

}
