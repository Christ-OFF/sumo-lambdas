package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Created by christophe on 30.04.17.
 */
public class RikishisPicturesScrapperTest {

    /**
     * for the test I have ONE picture, could have more...
     */
    private static final String RIKISHI_PICTURE = "42.jpg";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule();
    private RikishiPicturesScrapper tested;

    @Before
    public void setup() {
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder("http://localhost:8080/").build();
        tested = new RikishiPicturesScrapper(params);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void should_not_provide_select_method() {
        tested.select();
    }

    @Test
    public void should_scrap_picture() throws IOException {
        // Given
        int expectedId = 42;
        URL urlPicture = Resources.getResource("42.jpg");
        byte[] bodyPicture = Resources.toByteArray(urlPicture);
        stubFor(get(urlEqualTo("/" + RikishisPicturesScrapParameters.DEFAULT_PICS_PATH + "42.jpg"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(bodyPicture)));
        // When
        RikishiPicture result = tested.getDetail(expectedId);
        // Then
        assertNotNull(result);
        assertEquals(expectedId, result.getId());
        assertArrayEquals(bodyPicture, result.getPicture().array());
    }

    @Test
    public void should_return_null_on_missing_image() throws IOException, ParseException {
        // Given
        stubFor(get(urlEqualTo("/" + RikishisPicturesScrapParameters.DEFAULT_PICS_PATH + "666.jpg")).willReturn(aResponse().withStatus(404)));
        // When
        RikishiPicture result = tested.getDetail(666);
        // Then
        assertNull(result);
    }

}
