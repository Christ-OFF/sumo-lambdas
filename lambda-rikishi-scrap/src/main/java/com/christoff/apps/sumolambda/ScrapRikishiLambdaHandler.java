package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.christoff.apps.scrappers.RikishisScrapParameters;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
import com.christoff.apps.sumo.lambda.sns.RikishisListMethods;
import com.christoff.apps.sumolambda.config.SpringConfig;
import com.christoff.apps.sumolambda.services.RikishiDetailScrapperService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * The AWS Handler is a spring boot application
 * See : https://stackoverflow.com/a/45848699/95040
 */
public class ScrapRikishiLambdaHandler extends LambdaScrapBase implements RequestHandler<SNSEvent, Object> {

    private static final Logger LOGGER = LogManager.getLogger(ScrapRikishiLambdaHandler.class);

    private ApplicationContext applicationContext;

    public ScrapRikishiLambdaHandler() {
        applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
    }

    /**
     * This IS the lambda method called by AWS
     * @param context the AWS context needed to use AWS tools like DynamoDB
     */
    public Object handleRequest(SNSEvent event, Context context) {
        // Beans and services
        RikishiDetailScrapperService service = applicationContext.getBean(RikishiDetailScrapperService.class);
        RikishisScrapParameters params = applicationContext.getBean(RikishisScrapParameters.class);
        AmazonSNS sns = applicationContext.getBean(AmazonSNS.class);
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
