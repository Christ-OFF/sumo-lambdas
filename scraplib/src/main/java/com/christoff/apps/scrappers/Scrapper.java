package com.christoff.apps.scrappers;

import com.christoff.apps.sumolambda.rikishisread.domain.DomainObject;

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
    DomainObject getDetail(IdAndUrl idAndUrl);

    /**
     * Retrieve on image may unimplemented as pictures are not always available
     * ex: yes for rikishis, no or maybe ate best for fights, bashos, ...
     */
    byte[] getIllustration(IdAndUrl idAndUrl);
}
