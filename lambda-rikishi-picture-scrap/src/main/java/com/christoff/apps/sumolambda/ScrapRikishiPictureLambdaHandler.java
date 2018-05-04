package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.christoff.apps.sumo.lambda.LambdaScrapBase;
import com.christoff.apps.sumolambda.config.SpringConfig;
import com.christoff.apps.sumolambda.services.RikishiPictureScrapperService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * The AWS Handler is a spring boot application
 * See : https://stackoverflow.com/a/45848699/95040
 */
public class ScrapRikishiPictureLambdaHandler extends LambdaScrapBase implements RequestHandler<SNSEvent, Object> {

    private static final Logger LOGGER = LogManager.getLogger(ScrapRikishiPictureLambdaHandler.class);

    private ApplicationContext applicationContext;

    public ScrapRikishiPictureLambdaHandler() {
        applicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
    }

    /**
     * This IS the lambda method called by AWS
     *
     * @param context the AWS context needed to use AWS tools like DynamoDB
     */
    public Object handleRequest(SNSEvent event, Context context) {
        // Beans and services
        RikishiPictureScrapperService service = applicationContext.getBean(RikishiPictureScrapperService.class);
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
