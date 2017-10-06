package com.christoff.apps.scrappers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RikishisPicturesScrapParametersTest {

    @Test
    void should_accept_quality_string() {
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder().withQuality("0.3").build();
        assertEquals(0.3F, params.getQuality().floatValue());
    }

    @Test
    void should_fallback_to_default_with_bad_quality() {
        RikishisPicturesScrapParameters params = new RikishisPicturesScrapParameters.Builder().withQuality("Ph'nglui").build();
        assertEquals(0.3F, params.getQuality().floatValue());
    }

}
