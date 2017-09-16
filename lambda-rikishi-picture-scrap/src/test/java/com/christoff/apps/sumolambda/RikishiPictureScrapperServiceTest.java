package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.util.Base64;

class RikishiPictureScrapperServiceTest {

    private static final int FAKE_NUMBER = 42;

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private Scrapper scrapper;

    private RikishisPicturesScrapParameters params;
    private RikishiPictureScrapperService tested;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        params = new RikishisPicturesScrapParameters.Builder().build();
        tested = new RikishiPictureScrapperService(dynamoDBMapper, scrapper, params);
    }

    @Test
    public void should_save_base64_default_picture_if_no_picture_found() {
        // Given
        Mockito.when(scrapper.getDetail(FAKE_NUMBER)).thenReturn(null);
        @Nullable byte[] defaultPicture = tested.getDefaultRikishiPicture();
        @Nullable byte[] base64DefaultPicture = Base64.getEncoder().encode(defaultPicture);
        RikishiPicture expectedPicture = new RikishiPicture();
        expectedPicture.setId(FAKE_NUMBER);
        expectedPicture.setPicture(ByteBuffer.wrap(base64DefaultPicture));
        // When
        tested.scrap(FAKE_NUMBER);
        // Then
        Mockito.verify(dynamoDBMapper).save(Mockito.eq(expectedPicture));
    }

    @Test
    public void should_save_new_bas64_picture_if_picture_found() {
        // Given
        byte[] reallyFakePicture = new byte[0];
        byte[] base64ReallyFakePicture = Base64.getEncoder().encode(reallyFakePicture);
        RikishiPicture expectedPicture = new RikishiPicture();
        expectedPicture.setId(FAKE_NUMBER);
        expectedPicture.setPicture(ByteBuffer.wrap(base64ReallyFakePicture));
        Mockito.when(scrapper.getDetail(FAKE_NUMBER)).thenReturn(expectedPicture);
        // When
        tested.scrap(FAKE_NUMBER);
        // Then
        Mockito.verify(dynamoDBMapper).save(Mockito.eq(expectedPicture));
    }

}
