package com.christoff.apps.sumo.lambda.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by christophe on 20.06.17.
 */
@DynamoDBTable(tableName = "RIKISHIS_PICTURES")
public class RikishiPicture implements Serializable, DomainObject {

    @DynamoDBHashKey
    private int id;

    /**
     * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.DataTypes.html
     * says that binary should be mapped to ByteBuffer (but Item class offers both)
     */
    @DynamoDBAttribute
    private ByteBuffer picture;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ByteBuffer getPicture() {
        return picture;
    }

    public void setPicture(ByteBuffer picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "Rikishi{" + "id=" + id + ", picture='" + (picture != null) + '\'' + '}';
    }
}
