package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class ScrapRikishisPicturesLambdaMethodHandler extends LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(ScrapRikishisPicturesLambdaMethodHandler.class);

    /**
     * There is only on extract info (is it an anti-pattern ?)
     */
    private static final int EXTRACT_INFO_ID = 1;
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
            // Init
            LOGGER.info("Entering Sumo Scrapping process...for " + params.toString());
            this.mapper = new DynamoDBMapper( getDynamoDbClient( context ));
            // Rikishis
            if (!params.getExtractInfoOnly()) {
                boolean result = handleRikishis(params);
                if (result) {
                    LOGGER.info("SUCCESS");
                } else {
                    LOGGER.warn("There was a failure on handling rikishis");
                }
            } else {
                LOGGER.info("Skipped Rikishis");
            }
            // Last step extract info about scrapping
            handleExtractInfo();
            // It's over
            LOGGER.info("Finished Sumo Scrapping process");
        }
    }

    /**
     * Get from web scrapper and Write Rikishis to DynamoDB
     * @param parameters the mandatory addresses we must know to scrap
     * @return true if there is no failure at all
     */
    private boolean handleRikishis(RikishisScrapParameters parameters) {
        // Rikishis
        LOGGER.info("Entering Sumo Scrapping process...for " + parameters.toString());
        // Get the default picture for pictureless rikishis
        byte[] defaultPicture = getDefaultRikishiPicture();
        if (defaultPicture == null){
            LOGGER.error("Cannot process rikishis without a default picture");
            return false;
        }
        // Prepare the scrapper
        RikishisScrapper rikishisScrapper = new RikishisScrapper(parameters);
        List<Integer> rikishisIds = rikishisScrapper.select();
        LOGGER.info("Going to query " + rikishisIds.size() + " rikishis");
        List<Rikishi> rikishis = extractRikishis(rikishisScrapper, rikishisIds);
        // Then write them in BATCH (to avoid cost)
        List<DynamoDBMapper.FailedBatch> failures = mapper.batchSave(rikishis);
        LOGGER.info("Save failures " + failures.size());
        if (!failures.isEmpty()){
            LOGGER.error("Error saving riskishis", failures.get(0).getException());
            return false;
        }
        LOGGER.info("Going to extract and save pictures ");
        return scrapPictures(rikishis, rikishisScrapper, defaultPicture);
    }

    /**
     * We are going to save all rikishi pictures one by one
     * Not in batch as memory will grow
     *
     * @return always true as single save does not return a failure like batchSave do
     */
    private boolean scrapPictures(List<Rikishi> rikishis, RikishisScrapper scrapper, byte[] defaultPicture) {
        rikishis
            .parallelStream()
            .map(Rikishi::getId)
            .forEach(id -> {
                byte[] picture = scrapper.getIllustration(id, defaultPicture);
                byte[] base64picture = Base64.getEncoder().encode(picture);
                RikishiPicture rikishiPicture = new RikishiPicture();
                rikishiPicture.setId(id);
                rikishiPicture.setPicture(ByteBuffer.wrap(base64picture));
                mapper.save(rikishiPicture);
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

    /**
     *  Save and extract info with today as Date
     */
    private void handleExtractInfo(){
        ExtractInfo extractInfo = new ExtractInfo();
        extractInfo.setId(EXTRACT_INFO_ID);
        LocalDate now = LocalDate.now();
        extractInfo.setDate(now.format(DateTimeFormatter.ISO_DATE));
        mapper.save(extractInfo);
    }

    /**
     * Extract Rikishis using multiple threadss
     * @param rikishisIds We need a list of ids as the deatil url is known
     * @return the filter list of rikishis
     */
    private List<Rikishi> extractRikishis(RikishisScrapper rikishisScrapper, List<Integer> rikishisIds) {
        List<Rikishi> result = new ArrayList<>();
        rikishisIds
            .parallelStream()
            .forEach(rikishiUrl -> {
                Rikishi detail = (Rikishi) rikishisScrapper.getDetail(rikishiUrl);
                if (detail != null) {
                    result.add(detail);
                } // else rikishi is skipped
            });
        return result;
    }
}
