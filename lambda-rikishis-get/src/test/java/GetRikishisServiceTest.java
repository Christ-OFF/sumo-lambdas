import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.christoff.apps.sumo.lambda.domain.ExtractInfo;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import com.christoff.apps.sumolambda.GetRikishisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GetRikishisServiceTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private GetRikishisService tested;
    @Mock
    private PaginatedScanList<Rikishi> paginatedList;

    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        tested = new GetRikishisService(dynamoDBMapper);
    }

    @Test
    public void should_get_rikishis() {
        // Given
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        Rikishi expected = new Rikishi();
        expected.setId(42);
        expected.setName("Test");
        List<Rikishi> expectedList = Collections.singletonList(expected);
        Mockito.when(dynamoDBMapper.scan(Mockito.eq(Rikishi.class),
            Mockito.any()))
            .thenReturn(paginatedList);
        Mockito.when(paginatedList.size()).thenReturn(1);
        Mockito.when(paginatedList.get(0)).thenReturn(expected);
        // When
        List<Rikishi> result = tested.read();
        // Then
        Mockito.verify(dynamoDBMapper).scan(Mockito.eq(ExtractInfo.class), Mockito.any(DynamoDBScanExpression.class));
        assertNotNull(result);
        assertEquals(1, result.size(), "Not the expected result size");
        assertEquals(expected, result.get(0), "Rikishi is not the expected one");
    }
}
