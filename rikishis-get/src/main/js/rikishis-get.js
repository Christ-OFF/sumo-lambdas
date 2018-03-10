/**
 * Retrieve Rikishis
 * See http://www.virtualsecrets.com/amazon-aws-lambda-nodejs-dynamo-synchronous.html
 */

var AWS = require("aws-sdk");

var docClient = new AWS.DynamoDB.DocumentClient();

var params = {
    TableName: "RIKISHIS",
    ProjectionExpression: "id, sumoName, sumoRank, realName, birthDate, shusshin, height, weight, heya"
};

var rikishisArray = [];

var resultContext;

function onScan(err, data, context) {
    if (err) {
        console.error("Unable to scan the table. Error JSON:", JSON.stringify(err, null, 2));
        var http500Result = {
            statusCode:  500,
            headers: { 'Content-Type': 'application/json; charset=utf-8' },
            body: JSON.stringify(err, null, 2)
        };
        resultContext.done(null, http500Result);
    } else {
        data.Items.forEach(function(rikishi) {
            rikishisArray.push(rikishi);
        });
    }
    // continue scanning if we have more rikishis, because scan can retrieve a maximum of 1MB of data
    if (typeof data.LastEvaluatedKey != "undefined") {
        console.log("Scanning for more...");
        params.ExclusiveStartKey = data.LastEvaluatedKey;
        docClient.scan(params, onScan);
    } else {
        /* Generate Response */
        var httpResult =  {
            statusCode:  200,
            headers: { 'Content-Type': 'application/json; charset=utf-8' },
            body: JSON.stringify(rikishisArray)
        };
        resultContext.done(null, httpResult);
    }
}

exports.handler = (event, context, callback) => {
    rikishisArray = [];
    resultContext = context;
    docClient.scan(params, onScan);
}
