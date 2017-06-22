package com.christoff.apps.sumolambda.rikishisread.function;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumolambda.rikishisread.domain.Rikishi;
import com.christoff.apps.sumolambda.rikishisread.pojos.RikishisRequest;
import com.christoff.apps.sumolambda.rikishisread.pojos.RikishisResponse;
import org.apache.log4j.Logger;

/**
 * This is the entry point of lambda processing
 */
public class RikishisHandler implements RequestHandler<RikishisRequest, RikishisResponse> {

    static final Logger LOGGER = Logger.getLogger(RikishisHandler.class);

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
    public RikishisResponse handleRequest(RikishisRequest input, Context context){
        initDynamoDbClient();
        //
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        RikishisResponse result = new RikishisResponse();
        result.setRikishis(mapper.scan(Rikishi.class, scanExpression));
        LOGGER.info("Got " + result.getRikishis().size() + " rikishis");
        return result;
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
