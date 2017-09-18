package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sns.AmazonSNS;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.github.junit5docker.Docker;
import com.github.junit5docker.Port;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This is an integration test of the services
 * Why no handler ?
 * More fine grain on mocks
 * Provides mock when no integration is possible (SNS cannot be run locally)
 * JUnit5 with Spring Boot 1.5.7 ?
 */
@Docker(image = "cnadiminti/dynamodb-local",
    ports = @Port(exposed = 8000, inner = 8000),
    newForEachCase = false
)
public class ExtractInfoReadWriteIT extends ReadWriteBaseIT {

    public static final String EXTRACT_INFO_ONLY = "true";
    private static final String FAKE_TOPIC = "FAKE_TOPIC";
    /**
     * The SNS is a real mock : as I don't have a integration of it
     */
    @Mock
    AmazonSNS sns;

    /**
     * Mandatory setup : tables must exist !
     */
    @BeforeAll
    public static void createTablesInDynamoDB() {
        ReadWriteBaseIT.createTablesInDynamoDB();
    }

    /**
     * Let's end test gracefully as port stay used
     */
    @AfterAll
    public static void shutdownDynamoDB() {
        ReadWriteBaseIT.shutdownDynamoDB();
    }

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_store_and_retrieve_extract_info() throws IOException {
        // Given
        DynamoDBMapper mapper = getDynamoDBMapper();
        RikishisScrapParameters scrapParameters = new RikishisScrapParameters.Builder(FAKE_TOPIC)
            .withextractInfoOnly(EXTRACT_INFO_ONLY)
            .build();
        RikishisScrapper scrapper = new RikishisScrapper(scrapParameters);
        RikishisScrapperService rikishisScrapperService = new RikishisScrapperService(mapper, sns, scrapper, scrapParameters);
        GetExtractInfoService getExtractInfoService = new GetExtractInfoService(mapper);
        // When
        rikishisScrapperService.scrap();
        ExtractInfo extractInfo = getExtractInfoService.read();
        // Then
        assertNotNull(extractInfo, "Should have a result");
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = date.format(formatter);
        assertEquals(today, extractInfo.getDate(), "Extract info should be today in format yyyy-MM-dd");
    }

}
