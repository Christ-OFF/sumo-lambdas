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
        return "RikishiPicture{" +
            "id=" + id +
            ", picture=" + picture +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RikishiPicture that = (RikishiPicture) o;

        if (id != that.id) return false;
        return picture != null ? picture.equals(that.picture) : that.picture == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (picture != null ? picture.hashCode() : 0);
        return result;
    }
}
