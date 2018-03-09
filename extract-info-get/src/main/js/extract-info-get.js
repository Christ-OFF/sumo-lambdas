/**
 * Retrieve Extract Info
 * there is only on extract info for now under the id 1
 * See http://www.virtualsecrets.com/amazon-aws-lambda-nodejs-dynamo-synchronous.html
 * for
 */

var AWS = require("aws-sdk");

var docClient = new AWS.DynamoDB.DocumentClient();

var params = {
    TableName: "EXTRACT_INFO",
    ProjectionExpression: "#id, extractdate",
    FilterExpression: "#id = :id",
    ExpressionAttributeNames: {"#id": "id"},
    ExpressionAttributeValues: {":id": 1}
};

function onScan(err, data) {
    if (err) {
        console.error("Unable to scan the table. Error JSON:", JSON.stringify(err, null, 2));
    } else {
        // print all the movies
        console.log("Scan succeeded.");
        data.Items.forEach(function (info) {
            console.log(info.id + ": ", info.date);
        });
    }
}

exports.handler = (event, context, callback) => {
    var extractinforesult;
    docClient.scan(params, function (err, data) {
        if (err) {
            // DynamoDB error
            console.error("Unable to scan the table. Error JSON:", JSON.stringify(err, null, 2));
        } else {
            // We have data...Go through the data returned by DynamoDB
            data.Items.forEach(function (extractinfo) {
                // Save the value to the "global" variable
                extractinforesult = extractinfo;
            });
        }
        /* Generate Response */
        context.done(null, extractinforesult);
    });
}
