package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.LambdaBase;
import org.apache.log4j.Logger;
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
public class ScrapRikishiLambdaHandler extends LambdaBase implements RequestHandler<SNSEvent, Object> {

    private static final Logger LOGGER = Logger.getLogger(ScrapRikishiLambdaHandler.class);

    /**
     * Build the spring application context by hand
     *
     * @param args not used
     * @return spring context
     */
    private ApplicationContext getApplicationContext(String[] args) {
        return new SpringApplicationBuilder(ScrapRikishiLambdaHandler.class)
            .web(false)
            .bannerMode(Banner.Mode.OFF)
            .run(args);
    }

    @Bean
    Scrapper scrapper(RikishisScrapParameters params) {
        return new RikishisScrapper(params);
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
        RikishiDetailScrapperService service = ctx.getBean(RikishiDetailScrapperService.class);
        //
        Integer id = rikishiIdFromEvent(event);
        if (id != null) {
            LOGGER.info("Going to scrap detail for " + id);
            service.scrap(id);
        }
        return null;
    }

}
