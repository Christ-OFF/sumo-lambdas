package com.christoff.apps.sumo.lambda;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.christoff.apps.scrappers.ScrapPublishParameters;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

public abstract class ScrapperService {

    private static final Logger LOGGER = Logger.getLogger(ScrapperService.class);

    protected final AmazonSNS sns;

    public ScrapperService(@NotNull AmazonSNS sns) {
        this.sns = sns;
    }

    /**
     * Publish that a Rikishi detail must be treated
     *
     * @param content to be publish as is
     */
    protected @Nullable
    PublishResult publishEvent(ScrapPublishParameters params, String content) {
        Assert.notNull(sns, "Cannot publish with a null sns");
        PublishRequest publishRequest = new PublishRequest(params.getPublishTopic(), content);
        LOGGER.info("Going to publish " + publishRequest.toString());
        try {
            PublishResult publishResult = sns.publish(publishRequest);
            if (publishResult != null && publishResult.getMessageId() != null) {
                LOGGER.info("MessageId - " + publishResult.getMessageId() + " sent : " + content);
            } else {
                LOGGER.warn("Message for " + content + " was NOT sent");
            }
            return publishResult;
        } catch (Exception e) {
            LOGGER.error("Publish error with " + publishRequest.toString(), e);
            return null;
        }
    }
}
