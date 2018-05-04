package com.christoff.apps.sumolambda.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumolambda.services.RikishiDetailScrapperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class RikishiDetailScrapperServiceTest {

    private static final String FAKE_PUBLISH_DETAIL_TOPIC = "FAKE_PUBLISH_DETAIL_TOPIC";
    private static final String FAKE_PUBLISH_PICTURE_TOPIC = "FAKE_PUBLISH_PICTURE_TOPIC";
    private static final int FAKE_NUMBER = 42;

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private AmazonSNS sns;

    @Mock
    private Scrapper scrapper;

    private RikishisScrapParameters params;
    private RikishiDetailScrapperService tested;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        params = new RikishisScrapParameters.Builder(FAKE_PUBLISH_DETAIL_TOPIC,FAKE_PUBLISH_PICTURE_TOPIC).build();
        tested = new RikishiDetailScrapperService(dynamoDBMapper, sns, scrapper, params);
    }

    @Test
    public void should_abort_if_no_detail_found() {
        // Given
        // When
        tested.scrap(FAKE_NUMBER);
        // Then
        Mockito.verify(dynamoDBMapper, Mockito.never()).save(Mockito.anyObject());
        Mockito.verify(sns, Mockito.never()).publish(Mockito.any(PublishRequest.class));
    }

    @Test
    public void should_save_and_publish_when_detail() {
        // Given
        Rikishi rikishi = new Rikishi();
        rikishi.setId(FAKE_NUMBER);
        Mockito.when(scrapper.getDetail(FAKE_NUMBER)).thenReturn(rikishi);
        // When
        tested.scrap(FAKE_NUMBER);
        // Then
        Mockito.verify(dynamoDBMapper).save(rikishi);
        Mockito.verify(sns).publish(FAKE_PUBLISH_PICTURE_TOPIC,String.valueOf(FAKE_NUMBER));
    }

}
