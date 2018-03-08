package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.christoff.apps.scrappers.RikishiPicturesScrapper;
import com.christoff.apps.scrappers.RikishisPicturesScrapParameters;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * The AWS Handler is a spring boot application
 * See : https://stackoverflow.com/a/45848699/95040
 */
@SpringBootApplication
public class ScrapRikishiPictureLambdaHandler extends LambdaScrapBase implements RequestHandler<SNSEvent, Object> {

    private static final Logger LOGGER = Logger.getLogger(ScrapRikishiPictureLambdaHandler.class);

    /**
     * Build the spring application context by hand
     *
     * @param args not used but mandatory
     * @return spring context
     */
    private ApplicationContext getApplicationContext(String[] args) {
        return new SpringApplicationBuilder(ScrapRikishiPictureLambdaHandler.class)
            .web(false)
            .bannerMode(Banner.Mode.OFF)
            .run(args);
    }

    /**
     * The Lambda function will get it's properties from the env
     * Those properties are set via the admin console
     */
    @Bean
    public RikishisPicturesScrapParameters params() {
        return new RikishisPicturesScrapParameters.Builder()
            .withBaseUrl(System.getenv("baseurl"))
            .withImageUrl(System.getenv("imageurl"))
            .withBucket(System.getenv("bucket"))
            .build();
    }

    @Bean
    Scrapper scrapper(RikishisPicturesScrapParameters params) {
        return new RikishiPicturesScrapper(params);
    }

    @Bean
    AmazonS3 s3Client() {
        return AmazonS3Client.builder().build();
    }

    /**
     * This IS the lambda method called by AWS
     *
     * @param context the AWS context needed to use AWS tools like DynamoDB
     */
    public Object handleRequest(SNSEvent event, Context context) {
        // Get Spring context
        ApplicationContext ctx = getApplicationContext(new String[]{});
        // Beans and services
        RikishiPictureScrapperService service = ctx.getBean(RikishiPictureScrapperService.class);
        //
        Integer id = firstRikishiIdFromEvent(event);
        if (id != null) {
            LOGGER.info("Going to scrap picture for " + id);
            service.scrap(id);
        }
        return null;
    }

    /**
     * Common method with heavy null checks to extract id from message
     *
     * @param event message containing raw id
     * @return id or NULL
     */
    @Nullable
    private Integer firstRikishiIdFromEvent(SNSEvent event) {
        if (event == null
            || event.getRecords() == null
            || event.getRecords().isEmpty()
            || event.getRecords().get(0) == null
            || event.getRecords().get(0).getSNS() == null
            || event.getRecords().get(0).getSNS().getMessage() == null
            || event.getRecords().get(0).getSNS().getMessage().isEmpty()) {
            LOGGER.error("Event is null or empty");
            return null;
        } else {
            return Integer.parseInt(event.getRecords().get(0).getSNS().getMessage());
        }
    }

}
