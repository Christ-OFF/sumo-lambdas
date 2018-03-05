package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.sns.AmazonSNS;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.ScrapperService;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumo.lambda.sns.RikishisListMethods;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The service called from the handler to process rikishis
 * Aim : test this stuff in a mocked context
 * Remember : this service must stay STATELESS
 */
@Component
public class RikishisScrapperService extends ScrapperService {

    private static final Logger LOGGER = Logger.getLogger(RikishisScrapperService.class);
    /**
     * There is only on extract info (is it an anti-pattern ?)
     */
    private static final int EXTRACT_INFO_ID = 1;

    private final DynamoDBMapper mapper;
    private final RikishisScrapParameters parameters;
    private final Scrapper scrapper;

    @Autowired
    public RikishisScrapperService(@NotNull DynamoDBMapper mapper,
                                   @NotNull AmazonSNS sns,
                                   @NotNull Scrapper scrapper,
                                   @NotNull RikishisScrapParameters parameters) {
        super(sns);
        this.mapper = mapper;
        this.parameters = parameters;
        this.scrapper = scrapper;
    }

    /**
     * The main method called from the handler
     */
    public void scrap() {
        if (!parameters.getExtractInfoOnly()) {
            LOGGER.info("First erase all rikishis");
            cleanUpRikishis();
            LOGGER.info("Going to scrap Rikishi's list using " + parameters.toString());
            // Scrap the list of rikishis
            List<Integer> rikishisIds = scrapper.select();
            // Emmit message with list of all rikishis ids
            if (rikishisIds != null && !rikishisIds.isEmpty()) {
                LOGGER.info("Scraped list of " + rikishisIds.size() + " Rikishis ");
                RikishisListMethods.publishRikishisListEvent(sns, parameters.getPublishDetailTopic(), rikishisIds);
                updateExtractInfo();
            } else {
                LOGGER.warn("No rikishis in scrapped list from " + parameters.toString());
            }
        } else {
            updateExtractInfo();
        }
    }

    /**
     * Erase all rikishis before adding again
     * Be careful a DeleteItemRequest cannot be made on all
     * We could recreate table BUT we have to wait until table is ACTIVE again !
     */
    private void cleanUpRikishis() {
        LOGGER.info("Erasing Rikishis one by one...");
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Rikishi> result = mapper.scan(Rikishi.class, scanExpression);
        if (result != null) {
            for (Rikishi data : result) {
                mapper.delete(data);
            }
            LOGGER.info("Erasing Rikishis one by one...done");
        } else {
            LOGGER.warn("No rikishis to erase");
        }
    }

    /**
     * Save and extract info with today as Date
     */
    private void updateExtractInfo() {
        Assert.notNull(mapper, "Cannot save with a null mapper");
        ExtractInfo extractInfo = new ExtractInfo();
        extractInfo.setId(EXTRACT_INFO_ID);
        LocalDate now = LocalDate.now();
        extractInfo.setExtractdate(now.format(DateTimeFormatter.ISO_DATE));
        LOGGER.info("Saving : " + extractInfo.toString());
        mapper.save(extractInfo);
    }
}
