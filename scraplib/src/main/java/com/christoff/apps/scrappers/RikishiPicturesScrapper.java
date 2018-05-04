package com.christoff.apps.scrappers;

import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by christophe on 30.04.17.
 */
public class RikishiPicturesScrapper implements Scrapper {

    private static final String IMAGE_EXTENSION = ".jpg";
    public static final String FAKE_HOST = "http://0.0.0.0/";

    private static final Logger LOGGER = LogManager.getLogger(RikishiPicturesScrapper.class);

    private RikishisPicturesScrapParameters scrapParameters;

    /**
     * We cannot start the process without thos basic properties
     *
     * @param scrapParameters is where will search for stuff to scrap
     */
    public RikishiPicturesScrapper(RikishisPicturesScrapParameters scrapParameters) {
        this.scrapParameters = scrapParameters;
    }

    @Override
    public List<Integer> select() {
        throw new UnsupportedOperationException("Pictures are not selected from scrapping");
    }

    /**
     * Retrieve on image may unimplemented as pictures are not always available
     * ex: yes for rikishis, no or maybe ate best for fights, bashos, ...
     *
     * @param id the id of the image
     */
    @Override
    public RikishiPicture getDetail(Integer id) {
        String downloadUrl = scrapParameters.getFullImageUrl() + id + IMAGE_EXTENSION;
        try {
            byte[] imageBytes = getImageBytes(new URL(downloadUrl));
            if (imageBytes != null) {
                LOGGER.debug("Successfully downloaded picture from " + downloadUrl);
                RikishiPicture result = new RikishiPicture();
                result.setId(id);
                result.setPicture(ByteBuffer.wrap(imageBytes));
                return result;
            }
        } catch (MalformedURLException e) {
            LOGGER.warn("Malformed Url " + downloadUrl, e);
        }
        return null;
    }

    /**
     * Load image bytes from the source website (so beware of exceptions)
     *
     * @param downloadURL is URL of the image
     * @return the byte array of the image, can be NULL
     */
    private byte[] getImageBytes(URL downloadURL) {
        try (InputStream in = new BufferedInputStream(downloadURL.openStream())) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            return out.toByteArray();
        } catch (IOException ioe) {
            LOGGER.warn("Unable to download image from " + downloadURL.toString());
            return null;
        }
    }
}
