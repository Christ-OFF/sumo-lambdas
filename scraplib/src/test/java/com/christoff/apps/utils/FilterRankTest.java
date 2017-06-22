package com.christoff.apps.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by christophe on 04.05.17.
 */
public class FilterRankTest {

    @Test
    public void should_say_no_to_null_ranks(){
        assertFalse(FilterRank.includeRank(null));
    }

    @Test
    public void should_accept_obvious_ranks() throws Exception {
        assertTrue(FilterRank.includeRank("Y1e"));
        assertTrue(FilterRank.includeRank("M16e"));
        assertTrue(FilterRank.includeRank("J10e"));
    }

    @Test
    public void should_reject_lower_ranks() throws Exception {
        assertFalse(FilterRank.includeRank("Ms18e"));
        assertFalse(FilterRank.includeRank("Jd1w"));
    }

    @Test
    public void should_reject_other_stuff() throws Exception {
        assertFalse(FilterRank.includeRank("toto"));
    }


}