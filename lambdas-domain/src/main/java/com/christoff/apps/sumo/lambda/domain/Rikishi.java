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
    private String sumoName;

    @DynamoDBAttribute
    private String sumoRank;

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

    public String getSumoName() {
        return sumoName;
    }

    public void setSumoName(String sumoName) {
        this.sumoName = sumoName;
    }

    public String getSumoRank() {
        return sumoRank;
    }

    public void setSumoRank(String sumoRank) {
        this.sumoRank = sumoRank;
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
            ", sumoName='" + sumoName + '\'' +
            ", sumoRank='" + sumoRank + '\'' +
            ", realName='" + realName + '\'' +
            ", birthDate='" + birthDate + '\'' +
            ", shusshin='" + shusshin + '\'' +
            ", height=" + height +
            ", weight=" + weight +
            ", heya='" + heya + '\'' +
            '}';
    }
}
