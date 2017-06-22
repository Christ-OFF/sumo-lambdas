package com.christoff.apps.sumolambda.rikishisread.function;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumolambda.rikishisread.pojos.ExtractInfoRequest;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class ExtractInfoHandler implements RequestHandler<ExtractInfoRequest, ExtractInfo> {

    static final Logger LOGGER = Logger.getLogger(ExtractInfoHandler.class);

    /**
     * To retrieve annotated Objects
     */
    DynamoDBMapper mapper = null;

    /**
     * From this entry point we are going to process ALL actions
     *
     * @param context
     * @return "DONE" or ... this result is not used. It's just useful to get it on AWS Console
     */
    public ExtractInfo handleRequest(ExtractInfoRequest input, Context context){
        initDynamoDbClient();
        //
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ExtractInfo> result = mapper.scan(ExtractInfo.class, scanExpression);
        LOGGER.info("Got " + result.size() + " extractinfo");
        if (result.size() == 1){
            return result.get(0);
        } else if (result.size() > 1){
            LOGGER.error("Too many ExtractInfo.  Returning null");
        }
        return null;
    }

    /**
     * The moste important method :
     * We need to connect to dynamo DB
     */
    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        this.mapper = new DynamoDBMapper(client);
    }

}
