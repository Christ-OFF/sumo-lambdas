package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sns.AmazonSNS;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.ScrapperService;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The service called from the handler to process rikishis
 * Aim : test this stuff in a mocked context
 * Remember : this service must stay STATELESS
 */
@Component
public class RikishiDetailScrapperService extends ScrapperService {

    private static final Logger LOGGER = Logger.getLogger(RikishiDetailScrapperService.class);

    private final
    DynamoDBMapper mapper;

    private final
    RikishisScrapParameters parameters;

    private final
    Scrapper scrapper;

    @Autowired
    public RikishiDetailScrapperService(@NotNull DynamoDBMapper mapper, @NotNull AmazonSNS sns, @NotNull Scrapper scrapper,
                                        @NotNull RikishisScrapParameters parameters) {
        super(sns);
        this.mapper = mapper;
        this.parameters = parameters;
        this.scrapper = scrapper;
    }

    /**
     * The main method called from the handler
     */
    public void scrap(int rikishiId) {
        LOGGER.info("Going to get rikishi " + rikishiId + " details");
        Rikishi detail = (Rikishi) scrapper.getDetail(rikishiId);
        if (detail == null) {
            LOGGER.warn("Unable to get detail of Rikishi " + rikishiId + " not going further");
        } else {
            mapper.save(detail);
            LOGGER.info("Rikishi " + rikishiId + " detail saved.");
            publishEvent(parameters.getPublishPictureTopic(), String.valueOf(rikishiId));
        }

    }
}
