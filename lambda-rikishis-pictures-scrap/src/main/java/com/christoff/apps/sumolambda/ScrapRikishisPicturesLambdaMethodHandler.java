package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.christoff.apps.scrappers.RikishiPicturesScrapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class ScrapRikishisPicturesLambdaMethodHandler extends LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(ScrapRikishisPicturesLambdaMethodHandler.class);

    private static final String DEFAULT_JPG = "default.jpg";

    /**
     * To store annotated Objects
     */
    private DynamoDBMapper mapper = null;

    /**
     * The main function is for local usage only
     */
    public static void main(String[] args) {
        ScrapRikishisPicturesLambdaMethodHandler lmh = new ScrapRikishisPicturesLambdaMethodHandler();
        lmh.handleRequest(buildLocalContext());
    }

    /**
     * From this entry point we are going to process ALL actions
     * This is the entry point used by AWS as AWS sets System env
     * @param context AWS context (null when local)
     */
    @SuppressWarnings("WeakerAccess")
    public void handleRequest(Context context) {
        // Get Env parameters : Those parameters are set in AWS Lambda console
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder(System.getenv("baseurl"))
            .imageUrl(System.getenv("imageurl"))
            .build();
        handleRequest(context, params);
    }


    /**
     * This method doesn't rely on env values as it is evil to change them in test for example
     *
     * @param context AWS context (null when local)
     */
    public void handleRequest(Context context, RikishisPicturesScrapParameters params) {
        if (!params.isValid()) {
            LOGGER.error("Mandatory env variables are missing. " + params.toString());
        } else {
            mapper = new DynamoDBMapper(getDynamoDbClient(context));
            //
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            List<Rikishi> result = mapper.scan(Rikishi.class, scanExpression);
            LOGGER.info("Got " + result.size() + " rikishis pictures to process");
            if (scrapPictures(result, params)) {
                LOGGER.info("Scrapped pictures");
            } else {
                LOGGER.warn("Unable to scrap pictures");
            }
        }
    }

    /**
     * We are going to save all rikishi pictures one by one
     * Not in batch as memory will grow
     *
     * @return always true as single save does not return a failure like batchSave do
     */
    private boolean scrapPictures(List<Rikishi> rikishis, RikishisPicturesScrapParameters params) {
        // Prepare
        RikishiPicturesScrapper picturesScrapper = new RikishiPicturesScrapper(params);
        byte[] defaulPicture = getDefaultRikishiPicture();
        if (defaulPicture == null) {
            LOGGER.error("No loadin possible without a default picture");
            return false;
        }
        byte[] base64DefaultPicture = Base64.getEncoder().encode(defaulPicture);
        // Stream
        rikishis
            .parallelStream()
            .map(Rikishi::getId)
            .forEach(id -> {
                RikishiPicture result = picturesScrapper.getDetail(id);
                if (result != null) {
                    result.setPicture(Base64.getEncoder().encode(result.getPicture()));
                } else {
                    LOGGER.warn("Saving default picture for rikishi " + id);
                    result = new RikishiPicture();
                    result.setId(id);
                    result.setPicture(ByteBuffer.wrap(base64DefaultPicture));
                }
                mapper.save(result);
            });
        return true;
    }

    /**
     * Returns the default picture from the resources
     * Yes the the default picture is embedded here
     * @return the byte array of the picture, otherwise NULL
     */
    private @Nullable byte[] getDefaultRikishiPicture() {
        URL urlDefaultPicture = Resources.getResource(DEFAULT_JPG);
        try {
            return Resources.toByteArray(urlDefaultPicture);
        } catch (IOException e) {
            LOGGER.error("Unable to load default picture " + DEFAULT_JPG, e);
            return null;
        }
    }
}
