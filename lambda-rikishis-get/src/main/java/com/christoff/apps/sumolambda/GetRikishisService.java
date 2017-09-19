package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The service called from the handler to process rikishis
 * Aim : test this stuff in a mocked context
 * Remember : this service must stay STATELESS
 */
@Component
public class GetRikishisService {

    private static final Logger LOGGER = Logger.getLogger(GetRikishisService.class);

    private final
    DynamoDBMapper mapper;

    @Autowired
    public GetRikishisService(@NotNull DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * The main method called from the handler
     */
    public @Nullable
    List<Rikishi> read() {
        LOGGER.info("Going to read extract_info");
        //
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Rikishi> result = mapper.scan(Rikishi.class, scanExpression);
        LOGGER.info("Got " + result.size() + " rikishis");
        return result;
    }

}
