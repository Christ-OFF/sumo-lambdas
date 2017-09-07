package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.DomainObject;

import java.util.List;

/**
 * Created by christophe on 30.04.17.
 */
public interface Scrapper {

    /**
     * There is alway a root page to start with
     * @return the list of urls to call in the next step
     */
    List<IdAndUrl> select() ;

    /**
     * Retrieve one element detail
     */
    DomainObject getDetail(IdAndUrl idAndUrl, byte[] defaultIllustration);

}
