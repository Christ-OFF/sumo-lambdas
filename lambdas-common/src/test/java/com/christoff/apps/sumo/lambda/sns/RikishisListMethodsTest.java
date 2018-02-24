package com.christoff.apps.sumo.lambda.sns;

import com.amazonaws.services.sns.AmazonSNS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;


public class RikishisListMethodsTest {

    private static final String FAKE_TOPIC = "FAKE_TOPIC";
    private static final int FAKE_ID = 42;

    @Test
    public void should_emit_sns_message_with_list() {
        AmazonSNS sns = mock(AmazonSNS.class);
        RikishisListMethods.publishRikishisListEvent(sns, FAKE_TOPIC, Collections.singletonList(FAKE_ID));
        verify(sns,times(1)).publish(FAKE_TOPIC, "["+FAKE_ID+"]");
    }

    @Test
    public void should_not_emit_sns_message_without_list() {
        AmazonSNS sns = mock(AmazonSNS.class);
        RikishisListMethods.publishRikishisListEvent(sns, FAKE_TOPIC, new ArrayList<>(0));
        verify(sns,never()).publish(FAKE_TOPIC, "["+FAKE_ID+"]");
    }

    @Test
    public void should_parse_array() {
        List<Integer> result = new ArrayList<>(2);
        result.add(42);
        result.add(24);
        Assertions.assertArrayEquals(result.toArray(),
            RikishisListMethods.getListIdsFromJsonString("[42,24]").toArray());
    }

    @Test
    public void should_return_empty_array() {
        List<Integer> result = new ArrayList<>(0);
        Assertions.assertArrayEquals(result.toArray(),
            RikishisListMethods.getListIdsFromJsonString("").toArray());
    }
}
