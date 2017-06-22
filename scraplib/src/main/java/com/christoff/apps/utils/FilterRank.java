package com.christoff.apps.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by christophe on 04.05.17.
 */
public class FilterRank {

    private static final String[] RANKS_ARRAY = {
            "J10e", "J10w", "J11e", "J11w", "J12e", "J12w",
            "J13e", "J13w", "J14e", "J14w", "J1e", "J1w", "J2e",
            "J2w", "J3e", "J3w", "J4e", "J4w", "J5e", "J5w",
            "J6e", "J6w", "J7e", "J7w", "J8e", "J8w", "J9e",
            "J9w",
            "K1e", "K1w",
            "M10e", "M10w", "M11e", "M11w", "M12e", "M12w",
            "M13e", "M13w", "M14e", "M14w", "M15e", "M15w",
            "M16e", "M1e", "M1w", "M2e", "M2w", "M3e", "M3w",
            "M4e", "M4w", "M5e", "M5w", "M6e", "M6w", "M7e",
            "M7w", "M8e", "M8w", "M9e", "M9w",
            "O1e", "O1w",
            "S1e", "S1w", "S2e",
            "Y1e", "Y1w", "Y2e", "Y2w"
    };

    private static final List<String> RANKS = Arrays.asList(RANKS_ARRAY);

    /**
     * Pure static class
     */
    private FilterRank() {
    }

    /**
     * Return true if the rank is supposed to be included
     *
     * @param rank
     * @return
     */
    public static boolean includeRank(String rank) {
        return RANKS.contains(rank);
    }

}
