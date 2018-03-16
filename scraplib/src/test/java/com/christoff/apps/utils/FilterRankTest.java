package com.christoff.apps.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Test expected supported ranks
 * A fun way to use parametrized tests
 * Created by christophe on 04.05.17.
 */
public class FilterRankTest {

    private static final int LAST_MAEGESHIRA_RANK = 18;
    private static final int FIRST_RANK = 1;
    private static final int LAST_JURYO_RANK = 30;

    @Test
    public void should_say_no_to_null_ranks(){
        Assertions.assertFalse(FilterRank.isShortRankToBeIncluded(null));
    }

    @ParameterizedTest(name = "should contain a Maegashira of rank {0}")
    @MethodSource("createMaegashiras")
    void should_accept_mageshiras(String rank) {
        Assertions.assertTrue(FilterRank.isRankToBeIncluded(rank));
    }

    /**
     * Create Maegashiras up to the expected supported rank
     * @return
     */
    private static Stream<String> createMaegashiras() {
        return IntStream.rangeClosed(FIRST_RANK,LAST_MAEGESHIRA_RANK).mapToObj(rank -> "Maegashira " + rank);
    }

    @ParameterizedTest(name = "should contain a short Maegashira East of rank {0}")
    @MethodSource("createShortMaegashirasEast")
    void should_accept_short_east_mageshiras(String rank) {
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded(rank));
    }

    @ParameterizedTest(name = "should contain a short Maegashira West of rank {0}")
    @MethodSource("createShortMaegashirasWest")
    void should_accept_short_west_mageshiras(String rank) {
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded(rank));
    }

    /**
     * Create Maegashiras up to the expected supported rank
     * @return
     */
    private static Stream<String> createShortMaegashirasEast() {
        return IntStream.rangeClosed(FIRST_RANK, LAST_MAEGESHIRA_RANK).mapToObj(rank -> "M" + rank + "e");
    }

    /**
     * Create Maegashiras up to the expected supported rank
     * @return
     */
    private static Stream<String> createShortMaegashirasWest() {
        return IntStream.rangeClosed(FIRST_RANK, LAST_MAEGESHIRA_RANK).mapToObj(rank -> "M" + rank + "w");
    }

    @ParameterizedTest(name = "should contain a Juryo of rank {0}")
    @MethodSource("createJuryos")
    void should_accept_juryos(String rank) {
        Assertions.assertTrue(FilterRank.isRankToBeIncluded(rank));
    }

    /**
     * Create Maegashiras up to the expected supported rank
     * @return
     */
    private static Stream<String> createJuryos() {
        return IntStream.rangeClosed(FIRST_RANK, LAST_JURYO_RANK).mapToObj(rank -> "Juryo " + rank);
    }

    @ParameterizedTest(name = "should contain a short East Juryo of rank {0}")
    @MethodSource("createShortJuryosEast")
    void should_accept_short_east_juryos(String rank) {
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded(rank));
    }

    @ParameterizedTest(name = "should contain a short West Juryo of rank {0}")
    @MethodSource("createShortJuryosWest")
    void should_accept_short_west_juryos(String rank) {
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded(rank));
    }

    /**
     * Create Maegashiras up to the expected supported rank
     * @return
     */
    private static Stream<String> createShortJuryosEast() {
        return IntStream.rangeClosed(FIRST_RANK, LAST_JURYO_RANK).mapToObj(rank -> "J" + rank + "e");
    }

    /**
     * Create Maegashiras up to the expected supported rank
     * @return
     */
    private static Stream<String> createShortJuryosWest() {
        return IntStream.rangeClosed(FIRST_RANK, LAST_JURYO_RANK).mapToObj(rank -> "J" + rank + "w");
    }

    @Test
    public void should_accept_obvious_ranks()  {
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded("Y1e"));
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded("M16e"));
        Assertions.assertTrue(FilterRank.isShortRankToBeIncluded("J10e"));
    }

    @Test
    public void should_reject_lower_ranks() {
        Assertions.assertFalse(FilterRank.isShortRankToBeIncluded("Ms18e"));
        Assertions.assertFalse(FilterRank.isShortRankToBeIncluded("Jd1w"));
    }

    @Test
    public void should_reject_other_stuff() {
        Assertions.assertFalse(FilterRank.isShortRankToBeIncluded("toto"));
    }

}
