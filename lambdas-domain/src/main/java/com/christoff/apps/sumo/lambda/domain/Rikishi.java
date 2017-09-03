package com.christoff.apps.sumo.lambda.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.io.Serializable;

/**
 * Created by christophe on 20.06.17.
 */
@DynamoDBTable(tableName = "RIKISHIS")
public class Rikishi implements Serializable, DomainObject {

    @DynamoDBHashKey
    private int id;

    @DynamoDBAttribute
    private String name;

    @DynamoDBAttribute
    private String rank;

    @DynamoDBAttribute
    private String realName;

    @DynamoDBAttribute
    private String birthDate;

    @DynamoDBAttribute
    private String shusshin;

    @DynamoDBAttribute
    private int height;

    @DynamoDBAttribute
    private double weight;

    @DynamoDBAttribute
    private String heya;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getShusshin() {
        return shusshin;
    }

    public void setShusshin(String shusshin) {
        this.shusshin = shusshin;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getHeya() {
        return heya;
    }

    public void setHeya(String heya) {
        this.heya = heya;
    }

    @Override
    public String toString() {
        return "Rikishi{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", rank='" + rank + '\'' +
            ", realName='" + realName + '\'' +
            ", birthDate='" + birthDate + '\'' +
            ", shusshin='" + shusshin + '\'' +
            ", height=" + height +
            ", weight=" + weight +
            ", heya='" + heya + '\'' +
            '}';
    }
}
