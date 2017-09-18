package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetExtractInfoServiceTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private GetExtractInfoService tested;
    @Mock
    private PaginatedScanList<ExtractInfo> paginatedList;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        tested = new GetExtractInfoService(dynamoDBMapper);
    }

    @Test
    public void should_get_extract_info() {
        // Given
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        ExtractInfo expected = new ExtractInfo();
        expected.setId(42);
        expected.setDate("FAKE_DATE");
        List<ExtractInfo> expectedList = Collections.singletonList(expected);
        Mockito.when(dynamoDBMapper.scan(Mockito.eq(ExtractInfo.class),
            Mockito.any()))
            .thenReturn(paginatedList);
        Mockito.when(paginatedList.size()).thenReturn(1);
        Mockito.when(paginatedList.get(0)).thenReturn(expected);
        // When
        ExtractInfo result = tested.read();
        // Then
        Mockito.verify(dynamoDBMapper).scan(Mockito.eq(ExtractInfo.class), Mockito.any(DynamoDBScanExpression.class));
        assertEquals(expected, result, "Extract info is altered");
    }
}
