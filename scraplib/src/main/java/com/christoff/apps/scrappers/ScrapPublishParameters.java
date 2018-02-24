package com.christoff.apps.scrappers;

/**
 * If the scrapper needs to publish it must have some parameters
 */
public interface ScrapPublishParameters {

    /**
     * this is a parameter as it must not be hard-coded
     *
     * @return the topic arn to ask for a detail to be retrieved
     */
    String getPublishDetailTopic();
}
