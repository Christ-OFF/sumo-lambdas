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

var result = [];

var resultContext;

function onScan(err, data, context) {
    if (err) {
        console.error("Unable to scan the table. Error JSON:", JSON.stringify(err, null, 2));
    } else {
        data.Items.forEach(function(rikishi) {
            result.push(rikishi);
        });
    }
    // continue scanning if we have more rikishis, because scan can retrieve a maximum of 1MB of data
    if (typeof data.LastEvaluatedKey != "undefined") {
        console.log("Scanning for more...");
        params.ExclusiveStartKey = data.LastEvaluatedKey;
        docClient.scan(params, onScan);
    } else {
        /* Generate Response */
        resultContext.done(null, result);
    }
}

exports.handler = (event, context, callback) => {
    result = [];
    resultContext = context;
    docClient.scan(params, onScan);
}
