package com.christoff.apps.sumolambda;

import com.amazonaws.services.s3.AmazonS3;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RikishiPictureScrapperServiceTest {

    private static final int FAKE_NUMBER = 42;

    @Mock
    private AmazonS3 s3;

    @Mock
    private Scrapper scrapper;

    private RikishisPicturesScrapParameters params;
    private RikishiPictureScrapperService tested;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        params = new RikishisPicturesScrapParameters.Builder().build();
        tested = new RikishiPictureScrapperService(s3, scrapper, params);
    }

    @Test
    public void should_save_picture() {
        // Given
        Mockito.when(scrapper.getDetail(FAKE_NUMBER)).thenReturn(null);
        @Nullable byte[] defaultPicture = tested.getDefaultRikishiPicture();
        assertNotNull(defaultPicture, "Can't test without default picture");
        RikishiPicture expectedPicture = new RikishiPicture();
        expectedPicture.setId(FAKE_NUMBER);
        expectedPicture.setPicture(ByteBuffer.wrap(defaultPicture));
        // When
        tested.scrap(FAKE_NUMBER);
        // Then
        Mockito.verify(s3).putObject(
            Mockito.eq(params.getBucket()),
            Mockito.eq(FAKE_NUMBER + ".jpg"),
            Mockito.any(),
            Mockito.any());
    }

}
