package com.christoff.apps.sumolambda.rikishisread.pojos;

import com.christoff.apps.sumolambda.rikishisread.domain.Rikishi;

import java.util.List;

/**
 * Created by christophe on 19.06.17.
 */
public class RikishisResponse {

    public List<Rikishi> getRikishis() {
        return rikishis;
    }

    public void setRikishis(List<Rikishi> rikishis) {
        this.rikishis = rikishis;
    }

    private List<Rikishi> rikishis;
}
