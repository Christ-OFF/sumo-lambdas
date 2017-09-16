package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.LambdaBase;
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
public class ScrapRikishisLambdaHandler extends LambdaBase {

    /**
     * This main method is NOT required by AWS
     * It just let you run the application locally
     * Why ? To populate DynamoDB, to debug, optimize, ...
     */
    public static void main(String[] args) {
        ScrapRikishisLambdaHandler app = new ScrapRikishisLambdaHandler();
        app.handleRequest(null);
    }

    /**
     * Build the spring application context by hand
     *
     * @param args not used
     * @return spring context
     */
    private ApplicationContext getApplicationContext(String[] args) {
        return new SpringApplicationBuilder(ScrapRikishisLambdaHandler.class)
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
    public void handleRequest(Context context) {
        // Get Spring context
        ApplicationContext ctx = getApplicationContext(new String[]{});
        // Beans and services
        RikishisScrapperService service = ctx.getBean(RikishisScrapperService.class);
        service.scrap();
    }
}
