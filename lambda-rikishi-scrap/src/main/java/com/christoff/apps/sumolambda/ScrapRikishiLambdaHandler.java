package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
import com.christoff.apps.sumo.lambda.sns.RikishisListMethods;
import org.apache.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * The AWS Handler is a spring boot application
 * See : https://stackoverflow.com/a/45848699/95040
 */
@SpringBootApplication
public class ScrapRikishiLambdaHandler extends LambdaScrapBase implements RequestHandler<SNSEvent, Object> {

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

    /**
     * The Lambda function will get it's properties from the env
     * Those properties are set via the admin console
     */
    @Bean
    public RikishisScrapParameters params() {
        return new RikishisScrapParameters.Builder(
            System.getenv(PUBLISH_DETAIL_TOPIC),System.getenv(PUBLISH_PICTURE_TOPIC))
            .withBaseUrl(System.getenv("baseurl"))
            .withListUrl(System.getenv("listurl"))
            .withRikishiUrl(System.getenv("rikishiurl"))
            .withextractInfoOnly(System.getenv("extractInfoOnly"))
            .build();
    }

    @Bean
    public AmazonSNS sns() {
        return AmazonSNSClientBuilder.standard().build();
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
        RikishisScrapParameters params = ctx.getBean(RikishisScrapParameters.class);
        AmazonSNS sns = ctx.getBean(AmazonSNS.class);
        // We now receive a List<Integer> as JSON instead of a single id
        List<Integer> ids = getRikishiIdFromEvent(event);
        if (!ids.isEmpty()) {
            LOGGER.info("Going to scrap detail for " + ids.get(0) + " " + (ids.size() -1) + " remains");
            service.scrap(ids.get(0));
            RikishisListMethods.publishRikishisListEvent(sns,params.getPublishDetailTopic(),ids.subList(1,ids.size()));
        } else {
            LOGGER.info("No Rikishis to process");
        }
        return null;
    }

}
