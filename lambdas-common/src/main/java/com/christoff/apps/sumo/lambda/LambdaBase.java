package com.christoff.apps.sumo.lambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.log4j.Logger;


/**
 * We have some common methods to all lambdas : at least connect to dynamoDB
 */
public abstract class LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(LambdaBase.class);

    /**
     * When the function is this one then we are executing locallys
     */
    private static final String LOCAL_FUNCTION_NAME = "LOCAL_FUNCTION_NAME";
    private static final String SERVICE_ENDPOINT = "http://0.0.0.0:8000";

    /**
     * Build a fake context that will be detected as "local"
     * This way conneczion to dynamoDB will be done
     * eiher locally or via AWS
     * @return a minimal context with one expected property
     */
    public static Context buildLocalContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return LOCAL_FUNCTION_NAME;
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        };
    }

    /**
     * This is the way we know for sure that we are local
     * @param context the AWS or handmade context
     * @return true is the expected function name is local
     */
    private boolean isLocal(Context context){
        return context != null && LOCAL_FUNCTION_NAME.equals(context.getFunctionName());
    }

    /**
     * The moste important method :
     * We need to connect to dynamo DB
     */
    protected AmazonDynamoDB getDynamoDbClient(Context context) {
        AmazonDynamoDB client;
        if (!isLocal(context)) {
            LOGGER.info("Building DynamoDB client for AWS Cloud");
            client = AmazonDynamoDBClientBuilder.standard().build();
        } else {
            LOGGER.info("Building DynamoDB client for LOCAL");
            client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(SERVICE_ENDPOINT, "us-west-2"))
                .build();
        }
        return client;
    }
}
