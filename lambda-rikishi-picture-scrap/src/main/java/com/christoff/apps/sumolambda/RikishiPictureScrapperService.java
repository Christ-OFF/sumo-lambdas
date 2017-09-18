package com.christoff.apps.sumolambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * The service called from the handler to process rikishis
 * Aim : test this stuff in a mocked context
 * Remember : this service must stay STATELESS
 */
@Component
public class RikishiPictureScrapperService {

    private static final Logger LOGGER = Logger.getLogger(RikishiPictureScrapperService.class);

    private static final String DEFAULT_JPG = "default.jpg";
    public static final String CONTENT_TYPE = "image/jpeg";

    private final
    AmazonS3 s3;

    private final
    RikishisPicturesScrapParameters params;

    private final
    Scrapper scrapper;

    @Autowired
    public RikishiPictureScrapperService(@NotNull AmazonS3 s3, @NotNull Scrapper scrapper,
                                         @NotNull RikishisPicturesScrapParameters params) {
        this.s3 = s3;
        this.params = params;
        this.scrapper = scrapper;
    }

    /**
     * The main method called from the handler
     */
    public void scrap(int rikishiId) {
        LOGGER.info("Going to get rikishi " + rikishiId + " picture");
        byte[] defaulPicture = getDefaultRikishiPicture();
        if (defaulPicture == null) {
            LOGGER.error("No loading possible without a default picture");
        } else {
            RikishiPicture result = (RikishiPicture) scrapper.getDetail(rikishiId);
            if (result != null) {
                LOGGER.info("Saving picture for rikishi " + rikishiId);
            } else {
                LOGGER.warn("Saving default picture for rikishi " + rikishiId);
                result = new RikishiPicture();
                result.setId(rikishiId);
                result.setPicture(ByteBuffer.wrap(defaulPicture));
            }
            storePictureToS3(rikishiId, result);
        }
    }

    /**
     * This method purpose is to store the rikishi picture asIs in S3
     *
     * @param rikishiId
     * @param result
     */
    private void storePictureToS3(int rikishiId, RikishiPicture result) {
        // Storing to S3
        // First Input Stream
        try (ByteArrayInputStream bais = new ByteArrayInputStream(result.getPicture().array())) {

            // Second metadata with InputStream
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(CONTENT_TYPE);
            metadata.setContentLength(result.getPicture().array().length);
            s3.putObject(params.getBucket(),
                rikishiId + ".jpg",
                bais,
                metadata);
            LOGGER.info("Saved " + rikishiId + ".jpg to " + params.getBucket());
        } catch (IOException ioe) {
            LOGGER.error("Unable to read bytes from image. Abort S3Store of " + rikishiId);
        }

    }

    /**
     * Returns the default picture from the resources
     * Yes the the default picture is embedded here
     *
     * @return the byte array of the picture, otherwise NULL
     */
    public @Nullable
    byte[] getDefaultRikishiPicture() {
        URL urlDefaultPicture = Resources.getResource(DEFAULT_JPG);
        try {
            return Resources.toByteArray(urlDefaultPicture);
        } catch (IOException e) {
            LOGGER.error("Unable to load default picture " + DEFAULT_JPG, e);
            return null;
        }
    }
}
