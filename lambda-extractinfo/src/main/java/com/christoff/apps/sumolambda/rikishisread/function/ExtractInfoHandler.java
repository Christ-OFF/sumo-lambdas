package com.christoff.apps.sumolambda.rikishisread.function;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumolambda.rikishisread.pojos.ExtractInfoRequest;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class ExtractInfoHandler extends LambdaBase implements RequestHandler<ExtractInfoRequest, ExtractInfo> {

    private static final Logger LOGGER = Logger.getLogger(ExtractInfoHandler.class);

    /**
     * To retrieve annotated Objects
     */
    private DynamoDBMapper mapper = null;

    /**
     * The main function is for local usage only
     */
    public static void main(String[] args) {
        ExtractInfoRequest localRequest = new ExtractInfoRequest();
        ExtractInfoHandler localHandler = new ExtractInfoHandler();
        ExtractInfo result = localHandler.handleRequest(localRequest, null);
        if (result != null) {
            LOGGER.info("SUCCESS " + result.toString());
        } else {
            LOGGER.warn("KO");
        }
    }


    /**
     * From this entry point we are going to process ALL actions
     *
     * @param context
     * @return "DONE" or ... this result is not used. It's just useful to get it on AWS Console
     */
    public ExtractInfo handleRequest(ExtractInfoRequest input, Context context) {
        this.mapper = new DynamoDBMapper( getDynamoDbClient( context == null));
        //
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ExtractInfo> result = mapper.scan(ExtractInfo.class, scanExpression);
        LOGGER.info("Got " + result.size() + " extractinfo");
        if (result.size() == 1) {
            return result.get(0);
        } else if (result.size() > 1) {
            LOGGER.error("Too many ExtractInfo.  Returning null");
        }
        return null;
    }


}
