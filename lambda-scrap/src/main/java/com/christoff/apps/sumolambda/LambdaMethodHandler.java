package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.christoff.apps.scrappers.IdAndUrl;
import com.christoff.apps.scrappers.RikishiScrapper;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class LambdaMethodHandler extends LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(LambdaMethodHandler.class);

    /**
     * There is only on extract info (is it an anti-pattern ?)
     */
    private static final int EXTRACT_INFO_ID = 1;

    /**
     * To store annotated Objects
     */
    private DynamoDBMapper mapper = null;

    /**
     * The main function is for local usage only
     */
    public static void main(String[] args) {
        LambdaMethodHandler lmh = new LambdaMethodHandler();
        lmh.handleRequest(buildLocalContext());
    }

    /**
     * From this entry point we are going to process ALL actions
     * @param context AWS context (null when local)
     */
    @SuppressWarnings("WeakerAccess")
    public void handleRequest(Context context) {
        // Get Env parameters : Those parameters are set in AWS Lambda console
        String baseurl = System.getenv("baseurl");
        String imageurl = System.getenv("imageurl");
        String listurl = System.getenv("listurl");
        String extractInfoOnly = System.getenv("extractInfoOnly");
        if ( baseurl == null || baseurl.isEmpty() || imageurl == null || imageurl.isEmpty()){
            LOGGER.error("Mandatory env variables are missing.  Aborting");
        } else {
            // Init
            LOGGER.info("Entering Sumo Scrapping process...for " + baseurl);
            this.mapper = new DynamoDBMapper( getDynamoDbClient( context ));
            // Rikishis
            if (extractInfoOnly != null && !extractInfoOnly.isEmpty() && !Boolean.valueOf(extractInfoOnly)) {
                boolean result = handleRikishis(baseurl, listurl);
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
     * @param baseurl of sum website
     * @param listurl url of rikishis list
     * @return true if there is no failure at all
     */
    private boolean handleRikishis(String baseurl, String listurl){
        // Rikishis
        LOGGER.info("Entering Sumo Scrapping process...for " + listurl);
        RikishiScrapper rikishiScrapper = new RikishiScrapper();
        rikishiScrapper.setBaseUrl(baseurl);
        rikishiScrapper.setListUrl(listurl);
        List<IdAndUrl> rikishisUrls = rikishiScrapper.select();
        LOGGER.info("Going to query " + rikishisUrls.size() + " rikishis");
        List<Rikishi> rikishis = extractRikishis(rikishiScrapper, rikishisUrls);
        // Then write them in BATCH (to avoid cost)
        List<DynamoDBMapper.FailedBatch> failures = mapper.batchSave(rikishis);
        LOGGER.info("Save failures " + failures.size());
        if (!failures.isEmpty()){
            LOGGER.error("Error saving riskishis", failures.get(0).getException());
        }
        // fine !
        return !failures.isEmpty();
    }

    /**
     *
     * @return state of thei operation
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
     *
     * @param rikishisUrls
     * @return
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    private List<Rikishi> extractRikishis(RikishiScrapper rikishiScrapper, List<IdAndUrl> rikishisUrls) {
        List<Rikishi> result = new ArrayList<>();
        rikishisUrls
            .parallelStream()
            .forEach(rikishiUrl -> {
                Rikishi detail = (Rikishi) rikishiScrapper.getDetail(rikishiUrl);
                if (detail != null) {
                    result.add(detail);
                }
            });
        return result;
    }
}
