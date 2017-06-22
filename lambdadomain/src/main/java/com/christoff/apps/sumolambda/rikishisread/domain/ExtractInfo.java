package com.christoff.apps.sumolambda.rikishisread.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Stores information about the extract process
 * For the moment date, number of rikishis
 * Created by christophe on 14.06.17.
 */
@DynamoDBTable(tableName = "EXTRACT_INFO")
public class ExtractInfo implements DomainObject {

    @DynamoDBHashKey
    private int id;

    @DynamoDBAttribute
    private String date;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
