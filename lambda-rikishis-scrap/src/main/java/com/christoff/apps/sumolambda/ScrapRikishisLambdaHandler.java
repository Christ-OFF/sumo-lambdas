package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.scrappers.RikishisScrapper;
import com.christoff.apps.scrappers.Scrapper;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
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
public class ScrapRikishisLambdaHandler extends LambdaScrapBase {

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

    /**
     * The Lambda function will get it's properties from the env
     * Those properties are set via the admin console
     */
    @Bean
    public RikishisScrapParameters params() {
        return new RikishisScrapParameters.Builder(System.getenv("publishtopic"))
            .withBaseUrl(System.getenv("baseurl"))
            .withListUrl(System.getenv("listurl"))
            .withRikishiUrl(System.getenv("rikishiurl"))
            .withextractInfoOnly(System.getenv("extractInfoOnly"))
            .build();
    }

    @Bean
    Scrapper scrapper(RikishisScrapParameters params) {
        return new RikishisScrapper(params);
    }

    @Bean
    public AmazonSNS sns() {
        return AmazonSNSClientBuilder.standard().build();
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
