package com.christoff.apps.sumolambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumolambda.rikishisread.function.ExtractInfoHandler;
import com.christoff.apps.sumolambda.rikishisread.function.RikishiPictureHandler;
import com.christoff.apps.sumolambda.rikishisread.function.RikishisHandler;
import com.christoff.apps.sumolambda.rikishisread.pojos.RikishiPictureRequest;
import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@Docker(image = "cnadiminti/dynamodb-local",
    ports = @Port(exposed = 8000, inner = 8000),
    newForEachCase = false
)
public class RikishiWriteReadTest {

    private static final String RIKISHIS_TABLE = "RIKISHIS";
    private static final String EXTRACT_INFO_TABLE = "EXTRACT_INFO";
    private static final String RIKISHIS_PICTURES_TABLE = "RIKISHIS_PICTURES";
    private static final String ID = "id";
    private static final String EXTRACT_INFO_ONLY = "true";
    private static final int HAKUHO_NB = 1123;
    private static final String WIREMOCK_HOST = "http://localhost:8089/";

    /**
     * Needed for fake web content
     * Expensive to create so created once
     */
    private static WireMockServer wireMockServer;

    /**
     * We need our won client to do some setup
     * @return the dynamoDB client to local docker
     */
    private static AmazonDynamoDB getClient() {
        return AmazonDynamoDBClientBuilder
            .standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://0.0.0.0:8000", "us-west-2"))
            .build();
    }

    /**
     * Mandatory setup : tables must exist !
     */
    @BeforeAll
    public static void create_tablesand_start_wiremock() {
        AmazonDynamoDB client = getClient();
        // using CLI : aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name RIKISHIS_TABLE --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
        CreateTableRequest createTableRequestRikishis = getCreateTableRequest(RIKISHIS_TABLE);
        client.createTable(createTableRequestRikishis);
        // Using CLI aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name EXTRACT_INFO_TABLE --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
        CreateTableRequest createTableRequestExtractInfo = getCreateTableRequest(EXTRACT_INFO_TABLE);
        client.createTable(createTableRequestExtractInfo);
        // and new one
        CreateTableRequest createTableRequestRikishisPictures = getCreateTableRequest(RIKISHIS_PICTURES_TABLE);
        client.createTable(createTableRequestRikishisPictures);
        // Wiremock !
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    /**
     * Let's end test gracefully as port stay used
     */
    @AfterAll
    public static void shutdownDynamoDB() {
        wireMockServer.shutdown();
        getClient().shutdown();
    }

    /**
     * All tables created are the same for now
     * @return what we define here as standard table creation parameters
     */
    private static CreateTableRequest getCreateTableRequest(String tablename) {
        CreateTableRequest createTableRequest = new CreateTableRequest();
        createTableRequest.setTableName(tablename);
        AttributeDefinition idAttribute = new AttributeDefinition();
        idAttribute.setAttributeName(ID);
        idAttribute.setAttributeType(ScalarAttributeType.N);
        createTableRequest.setAttributeDefinitions(Collections.singletonList(idAttribute));
        KeySchemaElement keySchemaElement = new KeySchemaElement();
        keySchemaElement.setAttributeName(ID);
        keySchemaElement.setKeyType(KeyType.HASH);
        createTableRequest.setKeySchema(Collections.singletonList(keySchemaElement));
        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
        provisionedThroughput.setReadCapacityUnits(5L);
        provisionedThroughput.setWriteCapacityUnits(5L);
        createTableRequest.setProvisionedThroughput(provisionedThroughput);
        return createTableRequest;
    }

    @Test
    public void should_connect_to_running_dynamoDB_with_tables() {
        // Given
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder
            .standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://0.0.0.0:8000", "us-west-2"))
            .build();
        // When
        List<String> tables = client.listTables().getTableNames();
        // Then
        assertEquals(3, tables.size(), "must have three tables");
        assertTrue(tables.contains(RIKISHIS_TABLE), "must have table " + RIKISHIS_TABLE);
        assertTrue(tables.contains(EXTRACT_INFO_TABLE), "must have table " + EXTRACT_INFO_TABLE);
        assertTrue(tables.contains(RIKISHIS_PICTURES_TABLE), "must have table " + RIKISHIS_PICTURES_TABLE);
    }

    @Test
    public void should_store_and_retrieve_extract_info() throws IOException {
        // Given
        ScrapRikishisLambdaMethodHandler lmh = new ScrapRikishisLambdaMethodHandler();
        RikishisScrapParameters parameters = new RikishisScrapParameters.Builder(WIREMOCK_HOST).extractInfoOnly(EXTRACT_INFO_ONLY).build();
        lmh.handleRequest(LambdaBase.buildLocalContext(), parameters);
        // When
        ExtractInfoHandler localHandler = new ExtractInfoHandler();
        ExtractInfo result = localHandler.handleRequest(null, LambdaBase.buildLocalContext());
        // Then
        assertNotNull(result, "Should have a result");
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = date.format(formatter);
        assertEquals(today, result.getDate(), "Extract info should be today in format yyyy-MM-dd");
    }

    @Test
    public void should_store_and_retrieve_rikishi() throws IOException {
        // Given the Rikishis list (one rikishi only)
        wireMockServer.resetAll();
        URL urlRikishis = Resources.getResource("rikishi_short_list.html");
        String bodyRikishis = Resources.toString(urlRikishis, Charsets.UTF_8);
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/" + RikishisScrapParameters.DEFAULT_LIST_QUERY))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withBody(bodyRikishis)));
        // Given the one rikishi detail
        URL urlHakuho = Resources.getResource("hakuho.html");
        String bodyHakuho = Resources.toString(urlHakuho, Charsets.UTF_8);
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/" + RikishisScrapParameters.DEFAULT_RIKISHI_QUERY + HAKUHO_NB))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withBody(bodyHakuho)));
        // Given the rikishi picture
        URL urlPicture = Resources.getResource("rikishi_picture.jpg");
        byte[] bodyPicture = Resources.toByteArray(urlPicture);
        byte[] bodyPictureBase64 = Base64.getEncoder().encode(bodyPicture);
        wireMockServer.stubFor(get(urlEqualTo("/" + RikishisPicturesScrapParameters.DEFAULT_PICS_PATH + HAKUHO_NB + ".jpg"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(bodyPicture)));
        // When scraping details
        ScrapRikishisLambdaMethodHandler lmh = new ScrapRikishisLambdaMethodHandler();
        RikishisScrapParameters parameters = new RikishisScrapParameters.Builder(WIREMOCK_HOST).build();
        lmh.handleRequest(LambdaBase.buildLocalContext(), parameters);
        // and pictures
        ScrapRikishisPicturesLambdaMethodHandler lambdaMethodHandler = new ScrapRikishisPicturesLambdaMethodHandler();
        RikishisPicturesScrapParameters picturesScrapParameters = new RikishisPicturesScrapParameters.Builder(WIREMOCK_HOST).build();
        lambdaMethodHandler.handleRequest(LambdaBase.buildLocalContext(), picturesScrapParameters);
        // and retrieving
        RikishisHandler rikishisHandler = new RikishisHandler();
        List<Rikishi> result = rikishisHandler.handleRequest(null, LambdaBase.buildLocalContext());
        RikishiPictureHandler rikishiPictureHandler = new RikishiPictureHandler();
        RikishiPictureRequest rikishiPictureRequest = new RikishiPictureRequest();
        rikishiPictureRequest.setId(HAKUHO_NB);
        ByteBuffer picture = rikishiPictureHandler.handleRequest(rikishiPictureRequest, LambdaBase.buildLocalContext());
        // Then
        assertNotNull(result, "Should have a result");
        assertFalse(result.isEmpty(), "Should have something in the result");
        assertEquals(1, result.size(), "We have only one Rikishi here");
        Rikishi hakuho = result.get(0);
        assertEquals(HAKUHO_NB, hakuho.getId(), "Id");
        assertEquals(192, hakuho.getHeight(), "Height");
        assertNotNull(picture, "Picture should have been retrieved");
        assertArrayEquals(bodyPictureBase64, picture.array(), "Picture should be the bas64 same");
    }
}
