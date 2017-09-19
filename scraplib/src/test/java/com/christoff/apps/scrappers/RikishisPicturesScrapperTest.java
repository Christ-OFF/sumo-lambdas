package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

/**
 * Created by christophe on 30.04.17.
 */
@ExtendWith({
    WiremockResolver.class
})
public class RikishisPicturesScrapperTest {

    /**
     * for the test I have ONE picture, could have more...
     */
    private static final String RIKISHI_PICTURE = "42.jpg";

    @Test
    public void should_not_provide_select_method() {
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder().build();
        RikishiPicturesScrapper tested = new RikishiPicturesScrapper(params);
        Assertions.assertThrows(UnsupportedOperationException.class, tested::select);
    }

    @Test
    public void should_scrap_picture(@WiremockResolver.Wiremock WireMockServer server) throws IOException {
        // Given
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder()
            .withBaseUrl("http://localhost:" + server.port() + "/")
            .build();
        RikishiPicturesScrapper tested = new RikishiPicturesScrapper(params);
        int expectedId = 42;
        URL urlPicture = Resources.getResource("42.jpg");
        byte[] bodyPicture = Resources.toByteArray(urlPicture);
        server.stubFor(get(urlEqualTo("/" + RikishisPicturesScrapParameters.DEFAULT_PICS_PATH + "42.jpg"))
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
    public void should_return_null_on_missing_image(@WiremockResolver.Wiremock WireMockServer server) throws IOException, ParseException {
        // Given
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder()
            .withBaseUrl("http://localhost:" + server.port() + "/")
            .build();
        RikishiPicturesScrapper tested = new RikishiPicturesScrapper(params);
        server.stubFor(get(urlEqualTo("/" + RikishisPicturesScrapParameters.DEFAULT_PICS_PATH + "666.jpg"))
            .willReturn(aResponse().withStatus(404)));
        // When
        RikishiPicture result = tested.getDetail(666);
        // Then
        assertNull(result);
    }

}
