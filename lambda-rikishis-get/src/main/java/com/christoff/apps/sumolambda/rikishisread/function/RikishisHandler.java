package com.christoff.apps.sumolambda.rikishisread.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumolambda.rikishisread.pojos.RikishisRequest;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class RikishisHandler extends LambdaBase implements RequestHandler<RikishisRequest, List<Rikishi>> {

    private static final Logger LOGGER = Logger.getLogger(RikishisHandler.class);

    /**
     * From this entry point we are going to process ALL actions
     *
     * @param context
     * @return "DONE" or ... this result is not used. It's just useful to get it on AWS Console
     */
    public List<Rikishi> handleRequest(RikishisRequest input, Context context){
      /*  DynamoDBMapper mapper = new DynamoDBMapper(getDynamoDbClient(context));
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Rikishi> result = mapper.scan(Rikishi.class, scanExpression);
        LOGGER.info("Got " + result.size() + " rikishis");
        return result;*/
        return null;
    }


}
