package com.christoff.apps.sumolambda;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.domain.RikishiPicture;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * The service called from the handler to process rikishis
 * Aim : test this stuff in a mocked context
 * Remember : this service must stay STATELESS
 */
@Component
public class RikishiPictureScrapperService {

    private static final Logger LOGGER = Logger.getLogger(RikishiPictureScrapperService.class);

    private static final String DEFAULT_JPG = "default.jpg";

    private final
    DynamoDBMapper mapper;

    private final
    RikishisPicturesScrapParameters params;

    private final
    Scrapper scrapper;

    @Autowired
    public RikishiPictureScrapperService(@NotNull DynamoDBMapper mapper, @NotNull Scrapper scrapper,
                                         @NotNull RikishisPicturesScrapParameters params) {
        this.mapper = mapper;
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
            byte[] base64DefaultPicture = Base64.getEncoder().encode(defaulPicture);
            RikishiPicture result = (RikishiPicture) scrapper.getDetail(rikishiId);
            if (result != null) {
                LOGGER.info("Saving picture for rikishi " + rikishiId);
                result.setPicture(Base64.getEncoder().encode(result.getPicture()));
            } else {
                LOGGER.warn("Saving default picture for rikishi " + rikishiId);
                result = new RikishiPicture();
                result.setId(rikishiId);
                result.setPicture(ByteBuffer.wrap(base64DefaultPicture));
            }
            mapper.save(result);
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
