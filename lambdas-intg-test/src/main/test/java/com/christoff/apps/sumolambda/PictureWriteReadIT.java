package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.christoff.apps.scrappers.RikishiPicturesScrapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.io.Resources;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Docker(image = "cnadiminti/dynamodb-local",
    ports = @Port(exposed = 8000, inner = 8000),
    newForEachCase = false
)
public class PictureWriteReadIT extends ReadWriteBaseIT {

    private static final String WIREMOCK_HOST = "http://localhost:8089/";
    private static final int FAKE_NUMBER = 42;

    /**
     * Needed for fake web content
     * Expensive to create so created once
     */
    private static WireMockServer wireMockServer;

    /**
     * Mandatory setup : tables must exist !
     */
    @BeforeAll
    public static void createTablesInDynamoDB() {
        ReadWriteBaseIT.createTablesInDynamoDB();
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    /**
     * Let's end test gracefully as port stay used
     */
    @AfterAll
    public static void shutdownDynamoDB() {
        wireMockServer.shutdown();
        ReadWriteBaseIT.shutdownDynamoDB();
    }

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_store_and_retrieve_picture() throws IOException {
        // Given
        DynamoDBMapper mapper = getDynamoDBMapper();
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder()
            .withBaseUrl(WIREMOCK_HOST).build();
        URL urlPicture = Resources.getResource("rikishi_picture.jpg");
        byte[] bodyPicture = Resources.toByteArray(urlPicture);
        byte[] bodyPictureBase64 = Base64.getEncoder().encode(bodyPicture);
        wireMockServer.stubFor(
            WireMock.get(WireMock.urlEqualTo("/" + RikishisPicturesScrapParameters.DEFAULT_PICS_PATH + FAKE_NUMBER + ".jpg"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withBody(bodyPicture)));
        RikishiPicturesScrapper scrapper = new RikishiPicturesScrapper(params);
        RikishiPictureScrapperService scrapperService = new RikishiPictureScrapperService(mapper, scrapper, params);
        GetRikishiPictureService readerService = new GetRikishiPictureService(mapper);
        // When
        scrapperService.scrap(FAKE_NUMBER);
        ByteBuffer result = readerService.read(FAKE_NUMBER);
        // Then
        assertNotNull(result, "Picture should have been retrieved");
        assertArrayEquals(bodyPictureBase64, result.array(), "Picture should be the bas64 same");
    }

}
