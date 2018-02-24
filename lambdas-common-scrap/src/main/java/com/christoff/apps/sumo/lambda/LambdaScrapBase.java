package com.christoff.apps.sumo.lambda;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.christoff.apps.sumo.lambda.sns.RikishisListMethods;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * We have some common methods to all lambdas : at least connect to dynamoDB
 */
public abstract class LambdaScrapBase extends LambdaBase {

    private static final Logger LOGGER = Logger.getLogger(LambdaScrapBase.class);

    protected static final String PUBLISH_DETAIL_TOPIC = "publishdetailtopic";
    protected static final String PUBLISH_PICTURE_TOPIC = "publishpicturetopic";

    /**
     * Common method with heavy null checks to extract id from message
     *
     * @param event message containing raw id
     * @return id or NULL
     */
    protected @NotNull
    List<Integer> getRikishiIdFromEvent(SNSEvent event) {
        if (event == null
            || event.getRecords() == null
            || event.getRecords().isEmpty()
            || event.getRecords().get(0) == null
            || event.getRecords().get(0).getSNS() == null
            || event.getRecords().get(0).getSNS().getMessage() == null
            || event.getRecords().get(0).getSNS().getMessage().isEmpty()) {
            LOGGER.error("Event is null or empty");
            return new ArrayList<>(0);
        } else {
            return RikishisListMethods.getListIdsFromJsonString(event.getRecords().get(0).getSNS().getMessage());
        }
    }
}
