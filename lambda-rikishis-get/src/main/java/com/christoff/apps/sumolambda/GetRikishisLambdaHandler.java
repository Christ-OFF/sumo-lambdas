package com.christoff.apps.sumolambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.christoff.apps.sumo.lambda.LambdaBase;
import com.christoff.apps.sumo.lambda.domain.Rikishi;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * The AWS Handler is a spring boot application
 * See : https://stackoverflow.com/a/45848699/95040
 */
@SpringBootApplication
public class GetRikishisLambdaHandler extends LambdaBase implements RequestHandler<RikishisRequest, List<Rikishi>> {

    /**
     * Build the spring application context by hand
     *
     * @param args not used but mandatory
     * @return spring context
     */
    private ApplicationContext getApplicationContext(String[] args) {
        return new SpringApplicationBuilder(GetRikishisLambdaHandler.class)
            .web(false)
            .bannerMode(Banner.Mode.OFF)
            .run(args);
    }

    /**
     * From this entry point we are going to process ALL actions
     *
     * @param context
     * @return "DONE" or ... this result is not used. It's just useful to get it on AWS Console
     */
    public List<Rikishi> handleRequest(RikishisRequest input, Context context) {
        // Get Spring context
        ApplicationContext ctx = getApplicationContext(new String[]{});
        // Beans and services
        GetRikishisService service = ctx.getBean(GetRikishisService.class);
        return service.read();
    }

}
