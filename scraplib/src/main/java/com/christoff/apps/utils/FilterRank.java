package com.christoff.apps.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by christophe on 04.05.17.
 */
public class FilterRank {

    private static final String[] SHORT_RANKS_ARRAY = {
            "J26e", "J26w", "J27e", "J27w", "J28e", "J28w", "J29e", "J29w", "J30e", "J30w",
            "J21e", "J21w", "J22e", "J22w", "J23e", "J23w", "J24e", "J24w","J25e", "J25w",
            "J16e", "J16w", "J17e", "J17w", "J18e", "J18w", "J19e", "J19w","J20e", "J20w",
            "J11e", "J11w", "J12e", "J12w", "J13e", "J13w", "J14e", "J14w","J15e", "J15w",
            "J6e", "J6w", "J7e", "J7w", "J8e", "J8w", "J9e", "J9w","J10e", "J10w",
            "J1e", "J1w", "J2e", "J2w", "J3e", "J3w", "J4e", "J4w", "J5e", "J5w",
            "K1e", "K1w",
            "M10e", "M10w", "M11e", "M11w", "M12e", "M12w",
            "M13e", "M13w", "M14e", "M14w", "M15e", "M15w",
            "M16e", "M16w", "M17e", "M17w", "M18e", "M18w",
            "M1e", "M1w", "M2e", "M2w", "M3e", "M3w",
            "M4e", "M4w", "M5e", "M5w", "M6e", "M6w", "M7e",
            "M7w", "M8e", "M8w", "M9e", "M9w",
            "O1e", "O1w",
            "S1e", "S1w", "S2e",
            "Y1e", "Y1w", "Y2e", "Y2w"
    };

    private static final String[] RANKS_ARRAY = {
        "Juryo 26", "Juryo 27", "Juryo 28", "Juryo 29", "Juryo 30",
        "Juryo 21", "Juryo 22", "Juryo 23", "Juryo 24", "Juryo 25",
        "Juryo 16", "Juryo 17", "Juryo 18", "Juryo 19", "Juryo 20",
        "Juryo 11", "Juryo 12", "Juryo 13", "Juryo 14", "Juryo 15",
        "Juryo 1", "Juryo 2", "Juryo 3", "Juryo 4", "Juryo 5",
        "Juryo 6", "Juryo 7", "Juryo 8", "Juryo 9", "Juryo 10",
        "Komusubi",
        "Maegashira 10", "Maegashira 11", "Maegashira 12",
        "Maegashira 13", "Maegashira 14", "Maegashira 15",
        "Maegashira 16", "Maegashira 17", "Maegashira 18",
        "Maegashira 1", "Maegashira 2", "Maegashira 3",
        "Maegashira 4", "Maegashira 5", "Maegashira 6",
        "Maegashira 7", "Maegashira 8", "Maegashira 9",
        "Ozeki", "Sekiwake", "Yokozuna"
    };

    private static final List<String> SHORT_RANKS = Arrays.asList(SHORT_RANKS_ARRAY);
    private static final List<String> RANKS = Arrays.asList(RANKS_ARRAY);

    /**
     * Pure static class
     */
    private FilterRank() {
    }

    /**
     * Return true if the rank is supposed to be included
     * @param rank
     * @return
     */
    public static boolean isShortRankToBeIncluded(String rank) {
        return SHORT_RANKS.contains(rank);
    }

    /**
     * Return true if the rank is supposed to be included
     * @param rank
     * @return
     */
    public static boolean isRankToBeIncluded(String rank) {
        return RANKS.contains(rank);
    }

}
