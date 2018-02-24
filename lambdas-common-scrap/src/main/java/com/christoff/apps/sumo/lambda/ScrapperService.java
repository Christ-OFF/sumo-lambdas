package com.christoff.apps.sumo.lambda;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishResult;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

public abstract class ScrapperService {

    private static final Logger LOGGER = Logger.getLogger(ScrapperService.class);

    protected final AmazonSNS sns;

    public ScrapperService(@NotNull AmazonSNS sns) {
        this.sns = sns;
    }

    /**
     * Publish that a Rikishi detail must be treated
     * @param id to be publish as is
     */
    protected void publishEvent(String topic, String id) {
        Assert.notNull(sns, "Cannot publish with a null sns");
        LOGGER.info("Going to publish " + id + " to " + topic);
        try {
            PublishResult publishResult = sns.publish(topic,id);
            if (publishResult != null && publishResult.getMessageId() != null) {
                LOGGER.info("MessageId - " + publishResult.getMessageId() + " sent : " + id);
            } else {
                LOGGER.warn("Message for " + id + " was NOT sent");
            }
        } catch (Exception e) {
            LOGGER.error("Publish error with " + topic, e);
        }
    }
}
