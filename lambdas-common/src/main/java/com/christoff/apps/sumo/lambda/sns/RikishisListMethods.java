package com.christoff.apps.sumo.lambda.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is made to manage RikishisList in and out from SNS
 */
public class RikishisListMethods {

    private static final Logger LOGGER = Logger.getLogger(RikishisListMethods.class);
    private static final String UNABLE_TO_JSONIFY_IDS = "unable to Jsonify ids";

    private RikishisListMethods() {
        // utility class
    }

    /**
     * We use jasckson to jsonify
     * @param ids may be null or emtpy
     * @return null in cas of problems
     */
    private static @Nullable String getIdsAsJsonString(List<Integer> ids) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            LOGGER.error(UNABLE_TO_JSONIFY_IDS,e);
            return null;
        }
    }

    /**
     * We use jasckson to jsonify
     * @param ids may be null or emtpy
     * @return an empty array in case of problem
     */
    public static @NotNull List<Integer> getListIdsFromJsonString(String ids) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(ids,new TypeReference<List<Integer>>() {});
        } catch (IOException e) {
            LOGGER.error(UNABLE_TO_JSONIFY_IDS,e);
            return new ArrayList<>(0);
        }
    }

    /**
     * the method to use to send a liste of rikishis to process in a lambda
     * @param sns the amazon notification service
     * @param topic the topic to post to
     * @param ids list of ids (empty will do nothing)
     */
    public static void publishRikishisListEvent(AmazonSNS sns, String topic, List<Integer> ids) {
        Assert.notNull(sns, "Cannot publish with a null sns");
        //
        String idsJson = getIdsAsJsonString(ids);
        if (idsJson == null || idsJson.length() == 0) {
            LOGGER.info("No Ids to process. No message sent");
        } else {
            LOGGER.info("Going to publish " + ids.size() + " to " + topic);
            try {
                PublishResult publishResult = sns.publish(topic,idsJson);
                if (publishResult != null && publishResult.getMessageId() != null) {
                    LOGGER.info("MessageId - " + publishResult.getMessageId() + " sent with " + ids.size() + " rikishis.");
                } else {
                    LOGGER.warn("Message for " + ids.size() + " rikishis was NOT sent");
                }
            } catch (Exception e) {
                LOGGER.error("Publish error to " + topic, e);
            }
        }
    }
}
