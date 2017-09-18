package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
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
public class GetExtractInfoService {

    private static final Logger LOGGER = Logger.getLogger(GetExtractInfoService.class);

    private final
    DynamoDBMapper mapper;

    @Autowired
    public GetExtractInfoService(@NotNull DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * The main method called from the handler
     */
    public @Nullable
    ExtractInfo read() {
        LOGGER.info("Going to read extract_info");
        //
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<ExtractInfo> result = mapper.scan(ExtractInfo.class, scanExpression);
        if (result == null) {
            LOGGER.error("No result returned. Returning null");
        } else if (result.size() == 1) {
            return result.get(0);
        } else if (result.size() > 1) {
            LOGGER.error("Too many ExtractInfo.  Returning null");
        }
        return null;
    }

}
