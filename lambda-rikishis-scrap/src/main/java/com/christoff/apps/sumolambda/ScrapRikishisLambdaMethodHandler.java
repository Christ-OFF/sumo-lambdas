package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.Constants;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the entry point of lambda processing
 */
public class ScrapRikishisLambdaMethodHandler extends LambdaBase implements Constants {

    private static final Logger LOGGER = Logger.getLogger(ScrapRikishisLambdaMethodHandler.class);

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
        ScrapRikishisLambdaMethodHandler lmh = new ScrapRikishisLambdaMethodHandler();
        lmh.handleRequest(buildLocalContext());
    }

    /**
     * From this entry point we are going to process ALL actions
     * This is the entry point used by AWS as AWS sets System env
     *
     * @param context AWS context (null when local)
     */
    @SuppressWarnings("WeakerAccess")
    public void handleRequest(Context context) {
        // Get Env parameters : Those parameters are set in AWS Lambda console
        RikishisScrapParameters params = new RikishisScrapParameters.Builder(System.getenv("baseurl"))
            .withListUrl(System.getenv("listurl"))
            .withRikishiUrl(System.getenv("rikishiurl"))
            .withextractInfoOnly(System.getenv("extractInfoOnly"))
            .build();
        handleRequest(context, params);
    }


    /**
     * This method doesn't rely on env values as it is evil to change them in test for example
     *
     * @param context AWS context (null when local)
     */
    public void handleRequest(Context context, RikishisScrapParameters params) {
        if (!params.isValid()) {
            LOGGER.error("Mandatory env variables are missing. " + params.toString());
        } else {
            // Init
            LOGGER.info("Entering Sumo Scrapping process...for " + params.toString());
            mapper = new DynamoDBMapper(getDynamoDbClient(context));
            HttpClient client = HttpClientBuilder.create().build();
            // Rikishis
            if (!params.getExtractInfoOnly()) {
                boolean result = handleRikishis(context, params);
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
     *
     * @param parameters the mandatory addresses we must know to scrap
     * @return true if there is no failure at all
     */
    private boolean handleRikishis(Context context, RikishisScrapParameters parameters) {
        // Rikishis
        LOGGER.info("Entering Sumo Scrapping process...for " + parameters.toString());
        // Prepare the scrapper
        RikishisScrapper rikishisScrapper = new RikishisScrapper(parameters);
        List<Integer> rikishisIds = rikishisScrapper.select();
        LOGGER.info("Going to query " + rikishisIds.size() + " rikishis");
        List<Rikishi> rikishis = extractRikishis(rikishisScrapper, rikishisIds);
        // Then write them in BATCH (to avoid cost)
        List<DynamoDBMapper.FailedBatch> failures = mapper.batchSave(rikishis);
        LOGGER.info("Save failures " + failures.size());
        if (!failures.isEmpty()) {
            LOGGER.error("Error saving riskishis", failures.get(0).getException());
            return false;
        }
        LOGGER.info("Successfully extracted Rikishis details (going to notify for pictures)");
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().build();
        rikishis.stream()
            .map(Rikishi::getId)
            .forEach(id -> publishDetailUpdated(context, snsClient, id));
        return true;
    }

    /**
     * Publish that a Rikishi have been created or updated
     *
     * @param id rikishi unique id
     */
    private void publishDetailUpdated(Context context, AmazonSNS sns, int id) {
        if (isLocal(context)) {
            LOGGER.warn("Not sending notification when running local for id " + id);
        } else {
            PublishRequest publishRequest = new PublishRequest(RIKISHI_UPDATED, String.valueOf(id));
            PublishResult publishResult = sns.publish(publishRequest);
            if (publishResult != null && publishResult.getMessageId() != null) {
                LOGGER.info("MessageId - " + publishResult.getMessageId() + " sent for rikishi id " + id);
            } else {
                LOGGER.warn("Message for Rikishi " + id + " was NOT sent");
            }
        }
    }
    /**
     * Save and extract info with today as Date
     */
    private void handleExtractInfo() {
        ExtractInfo extractInfo = new ExtractInfo();
        extractInfo.setId(EXTRACT_INFO_ID);
        LocalDate now = LocalDate.now();
        extractInfo.setDate(now.format(DateTimeFormatter.ISO_DATE));
        mapper.save(extractInfo);
    }

    /**
     * Extract Rikishis using multiple threadss
     *
     * @param rikishisIds We need a list of ids as the deatil url is known
     * @return the filter list of rikishis
     */
    private List<Rikishi> extractRikishis(RikishisScrapper rikishisScrapper, List<Integer> rikishisIds) {
        List<Rikishi> result = new ArrayList<>();
        rikishisIds
            .parallelStream()
            .forEach(id -> {
                Rikishi detail = (Rikishi) rikishisScrapper.getDetail(id);
                if (detail != null) {
                    result.add(detail);
                } // else rikishi is skipped
            });
        return result;
    }
}
