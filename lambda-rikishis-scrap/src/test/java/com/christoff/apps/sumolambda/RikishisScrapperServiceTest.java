package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

public class RikishisScrapperServiceTest {

    private static final String FAKE_PUBLISH_DETAIL_TOPIC = "FAKE_PUBLISH_DETAIL_TOPIC";
    private static final String FAKE_PUBLISH_PICTURE_TOPIC = "FAKE_PUBLISH_PICTURE_TOPIC";
    private static final String EXTRACT_INFO_ONLY = "true";
    private static final int FAKE_NUMBER = 42;

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private AmazonSNS sns;

    @Mock
    private Scrapper scrapper;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_update_extract_info_to_today() {
        // Given
        RikishisScrapParameters params = new RikishisScrapParameters.Builder(
            FAKE_PUBLISH_DETAIL_TOPIC,FAKE_PUBLISH_PICTURE_TOPIC).withextractInfoOnly(EXTRACT_INFO_ONLY).build();
        RikishisScrapperService tested = new RikishisScrapperService(dynamoDBMapper, sns, scrapper, params);
        // When
        tested.scrap();
        // Then
        Mockito.verify(dynamoDBMapper).save(Mockito.anyObject());
    }

    @Test
    public void should_publish_found_rikishis() {
        // Given
        RikishisScrapParameters params = new RikishisScrapParameters.Builder(FAKE_PUBLISH_DETAIL_TOPIC,FAKE_PUBLISH_PICTURE_TOPIC).build();
        RikishisScrapperService tested = new RikishisScrapperService(dynamoDBMapper, sns, scrapper, params);
        Mockito.when(scrapper.select()).thenReturn(Collections.singletonList(FAKE_NUMBER));
        // When
        tested.scrap();
        // Then
        PublishRequest publishRequest = new PublishRequest(FAKE_PUBLISH_DETAIL_TOPIC, "[" + String.valueOf(FAKE_NUMBER) + "]");
        Mockito.verify(sns).publish(publishRequest);
    }

}
