package com.christoff.apps.scrappers;

/**
 * If the scrapper needs to publish it must have some parameters
 */
public interface ScrapPublishParameters {

    /**
     * this is a parameter as it must not be hard-coded
     *
     * @return
     */
    String getPublishTopic();
}
