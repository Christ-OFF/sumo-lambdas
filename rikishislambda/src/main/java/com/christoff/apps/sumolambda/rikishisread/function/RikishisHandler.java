package com.christoff.apps.sumolambda.rikishisread.function;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumolambda.rikishisread.pojos.RikishisRequest;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class RikishisHandler implements RequestHandler<RikishisRequest, List<Rikishi>> {

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
    public List<Rikishi> handleRequest(RikishisRequest input, Context context){
        initDynamoDbClient();
        //
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Rikishi> result = mapper.scan(Rikishi.class, scanExpression);
        LOGGER.info("Got " + result.size() + " rikishis");
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
