package com.christoff.apps.sumo.lambda.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Stores information about the extract process
 * For the moment extractdate
 * TODO Add number of rikishis
 * Created by christophe on 14.06.17.
 */
@DynamoDBTable(tableName = "EXTRACT_INFO")
public class ExtractInfo implements DomainObject {

    @DynamoDBHashKey
    private int id;

    @DynamoDBAttribute
    private String extractdate;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getExtractdate() {
        return extractdate;
    }

    public void setExtractdate(String extractdate) {
        this.extractdate = extractdate;
    }

    @Override
    public String toString() {
        return "ExtractInfo{" +
            "id=" + id +
            ", extractdate='" + extractdate + '\'' +
            '}';
    }
}
