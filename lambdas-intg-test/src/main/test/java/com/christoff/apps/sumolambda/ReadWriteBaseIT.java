package com.christoff.apps.sumolambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.Collections;

/**
 * Provides some useful methods for tests
 */
public class ReadWriteBaseIT {

    private static final String RIKISHIS_TABLE = "RIKISHIS";
    private static final String EXTRACT_INFO_TABLE = "EXTRACT_INFO";
    private static final String RIKISHIS_PICTURES_TABLE = "RIKISHIS_PICTURES";
    private static final String ID = "id";

    /**
     * We need our won client to do some setup
     *
     * @return the dynamoDB client to local docker
     */
    protected static AmazonDynamoDB getClient() {
        return AmazonDynamoDBClientBuilder
            .standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://0.0.0.0:8000", "us-west-2"))
            .build();
    }

    public static void createTablesInDynamoDB() {
        AmazonDynamoDB client = getClient();
        // using CLI : aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name RIKISHIS_TABLE --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
        CreateTableRequest createTableRequestRikishis = buildCreateTableRequest(RIKISHIS_TABLE);
        client.createTable(createTableRequestRikishis);
        // Using CLI aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name EXTRACT_INFO_TABLE --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
        CreateTableRequest createTableRequestExtractInfo = buildCreateTableRequest(EXTRACT_INFO_TABLE);
        client.createTable(createTableRequestExtractInfo);
        // and new one
        CreateTableRequest createTableRequestRikishisPictures = buildCreateTableRequest(RIKISHIS_PICTURES_TABLE);
        client.createTable(createTableRequestRikishisPictures);
    }

    /**
     * Static as it may be used in anf AfterAll
     */
    protected static void shutdownDynamoDB() {
        getClient().shutdown();
    }

    /**
     * All tables created are the same for now
     *
     * @return what we define here as standard table creation parameters
     */
    private static CreateTableRequest buildCreateTableRequest(String tablename) {
        CreateTableRequest createTableRequest = new CreateTableRequest();
        createTableRequest.setTableName(tablename);
        AttributeDefinition idAttribute = new AttributeDefinition();
        idAttribute.setAttributeName(ID);
        idAttribute.setAttributeType(ScalarAttributeType.N);
        createTableRequest.setAttributeDefinitions(Collections.singletonList(idAttribute));
        KeySchemaElement keySchemaElement = new KeySchemaElement();
        keySchemaElement.setAttributeName(ID);
        keySchemaElement.setKeyType(KeyType.HASH);
        createTableRequest.setKeySchema(Collections.singletonList(keySchemaElement));
        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
        provisionedThroughput.setReadCapacityUnits(5L);
        provisionedThroughput.setWriteCapacityUnits(5L);
        createTableRequest.setProvisionedThroughput(provisionedThroughput);
        return createTableRequest;
    }

    /**
     * @return
     */
    protected DynamoDBMapper getDynamoDBMapper() {
        return new DynamoDBMapper(getClient());
    }

}
