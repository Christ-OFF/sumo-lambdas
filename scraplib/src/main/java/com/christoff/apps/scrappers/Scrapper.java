package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.DomainObject;

import java.util.List;

/**
 * Created by christophe on 30.04.17.
 */
public interface Scrapper {

    /**
     * There is alway a root page to start with
     * @return the list of selected ids
     */
    List<Integer> select();

    /**
     * Retrieve one element detail
     */
    DomainObject getDetail(Integer id);

    /**
     * Retrieve on image may unimplemented as pictures are not always available
     * ex: yes for rikishis, no or maybe ate best for fights, bashos, ...
     *
     * @param id                  only the scrapper must know were ALL images are
     * @param defaultIllustration if impossaible to download we must return a default content
     */
    byte[] getIllustration(Integer id, byte[] defaultIllustration);
}
