package com.christoff.apps.sumolambda.rikishisread.function;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.christoff.apps.sumolambda.rikishisread.pojos.RikishiPictureRequest;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

/**
 * This is the entry point of lambda processing
 */
public class RikishiPictureHandler extends LambdaBase implements RequestHandler<RikishiPictureRequest, ByteBuffer> {

    private static final Logger LOGGER = Logger.getLogger(RikishiPictureHandler.class);

    /**
     * The main function is for local usage only
     */
    public static void main(String[] args) {
        RikishiPictureHandler localHandler = new RikishiPictureHandler();
        RikishiPictureRequest rikishiPictureRequest = new RikishiPictureRequest();
        rikishiPictureRequest.setId(1123);
        ByteBuffer result = localHandler.handleRequest(rikishiPictureRequest, buildLocalContext());
        if (result != null) {
            LOGGER.info("SUCCESS");
        } else {
            LOGGER.warn("KO");
        }
    }

    /**
     * Return the content of the field in DynamoDB
     * According to this :
     * http://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-payload-encodings.html
     * We should return Base64 string and the API Gateway converts it to real binary
     * We'll see !
     *
     * @param input   contains only the id
     * @param context
     * @return Base64 String
     */
    public ByteBuffer handleRequest(RikishiPictureRequest input, Context context) {
        if (input == null) {
            LOGGER.error("Mandatory parameter with id is missing");
            return null;
        }
        DynamoDBMapper mapper = new DynamoDBMapper(getDynamoDbClient(context));
        //
        RikishiPicture result = mapper.load(RikishiPicture.class, input.getId());
        LOGGER.info("Got " + (result != null) + " picture");
        if (result != null) {
            return result.getPicture();
        } else {
            LOGGER.warn("No picture found returning null");
            return null;
        }
    }


}
