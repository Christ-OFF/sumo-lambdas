package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
import com.christoff.apps.sumolambda.config.SpringConfig;
import com.christoff.apps.sumolambda.services.RikishisScrapperService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The AWS Handler is a spring boot application
 * See : https://stackoverflow.com/a/45848699/95040
 */
public class ScrapRikishisLambdaHandler extends LambdaScrapBase {

    private ApplicationContext applicationContext;

    public ScrapRikishisLambdaHandler() {
        applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
    }

    /**
     * This IS the lambda method called by AWS
     *
     * @param context the AWS context needed to use AWS tools like DynamoDB
     */
    public void handleRequest(Context context) {
        // Beans and services
        RikishisScrapperService service = applicationContext.getBean(RikishisScrapperService.class);
        service.scrap();
    }
}
