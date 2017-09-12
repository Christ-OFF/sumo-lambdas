package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.christoff.apps.scrappers.RikishiPicturesScrapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;

/**
 * This is the entry point of lambda processing
 */
public class ScrapRikishiPictureLambdaMethodHandler extends LambdaBase implements RequestHandler<SNSEvent, Object> {

    private static final Logger LOGGER = Logger.getLogger(ScrapRikishiPictureLambdaMethodHandler.class);

    private static final String DEFAULT_JPG = "default.jpg";

    /**
     * To store annotated Objects
     */
    private DynamoDBMapper mapper = null;

    /**
     * The main function is for local usage only
     */
    public static void main(String[] args) {
        ScrapRikishiPictureLambdaMethodHandler lmh = new ScrapRikishiPictureLambdaMethodHandler();
        // Prepare a fake SNSEvent
        SNSEvent snsEvent = new SNSEvent();
        SNSEvent.SNSRecord snsRecord = new SNSEvent.SNSRecord();
        SNSEvent.SNS sns = new SNSEvent.SNS();
        sns.setMessage("1123");
        snsRecord.setSns(sns);
        snsEvent.setRecords(Collections.singletonList(snsRecord));
        // Handle it
        lmh.handleRequest(snsEvent, buildLocalContext());
    }

    /**
     * From this entry point we are going to process ALL actions
     * This is the entry point used by AWS as AWS sets System env
     *
     * @param context AWS context (null when local)
     */
    @Override
    public Object handleRequest(SNSEvent snsEvent, Context context) {
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder(System.getenv("baseurl"))
            .withImageUrl(System.getenv("imageurl"))
            .build();
        return handleRequest(snsEvent, context, params);
    }

    /**
     * From this entry point we are going to process ALL actions
     * This is the entry point used by AWS as AWS sets System env
     *
     * @param context AWS context (null when local)
     */
    public Object handleRequest(SNSEvent snsEvent, Context context, RikishisPicturesScrapParameters params) {
        // Get the SNSEvent
        if (snsEvent == null
            || snsEvent.getRecords() == null
            || snsEvent.getRecords().size() != 1
            || snsEvent.getRecords().get(0) == null
            || snsEvent.getRecords().get(0).getSNS() == null
            || snsEvent.getRecords().get(0).getSNS().getMessage() == null
            ) {
            LOGGER.error("SNSEvent with data is missing " + snsEvent.toString());
        } else {
            mapper = new DynamoDBMapper(getDynamoDbClient(context));
            String idFromEvent = snsEvent.getRecords().get(0).getSNS().getMessage();
            try {
                int rikishiId = Integer.valueOf(idFromEvent);
                if (params.isValid()) {
                    scrapPicture(params, rikishiId);
                } else {
                    LOGGER.error("Parameters must be valid " + params.toString());
                }
            } catch (NumberFormatException nfe) {
                LOGGER.error("RikishiId is not a number " + idFromEvent, nfe);
            }
        }
        return null;

    }

    /**
     * We are going to save all rikishi pictures one by one
     * Not in batch as memory will grow
     *
     * @return always true as single save does not return a failure like batchSave do
     */
    private boolean scrapPicture(RikishisPicturesScrapParameters params, int rikishiId) {
        LOGGER.info("Going to scrap picture for rikishi " + rikishiId);
        // Prepare
        RikishiPicturesScrapper picturesScrapper = new RikishiPicturesScrapper(params);
        byte[] defaulPicture = getDefaultRikishiPicture();
        if (defaulPicture == null) {
            LOGGER.error("No loading possible without a default picture");
            return false;
        }
        byte[] base64DefaultPicture = Base64.getEncoder().encode(defaulPicture);
        RikishiPicture result = picturesScrapper.getDetail(rikishiId);
        if (result != null) {
            LOGGER.info("Saving picture for rikishi " + rikishiId);
            result.setPicture(Base64.getEncoder().encode(result.getPicture()));
        } else {
            LOGGER.warn("Saving default picture for rikishi " + rikishiId);
            result = new RikishiPicture();
            result.setId(rikishiId);
            result.setPicture(ByteBuffer.wrap(base64DefaultPicture));
        }
        mapper.save(result);
        return true;
    }

    /**
     * Returns the default picture from the resources
     * Yes the the default picture is embedded here
     *
     * @return the byte array of the picture, otherwise NULL
     */
    private @Nullable
    byte[] getDefaultRikishiPicture() {
        URL urlDefaultPicture = Resources.getResource(DEFAULT_JPG);
        try {
            return Resources.toByteArray(urlDefaultPicture);
        } catch (IOException e) {
            LOGGER.error("Unable to load default picture " + DEFAULT_JPG, e);
            return null;
        }
    }

}
