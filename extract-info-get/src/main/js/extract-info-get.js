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
    ExpressionAttributeNames: { "#id": "id" },
    ExpressionAttributeValues: {":id": 1 }
};

exports.handler = (event, context, callback) => {
    // We init the result saying by default that there is no extract-info
    var extractinforesult = {
        statusCode:  200,
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: "{}"
    };

    docClient.scan(params, function(err, data) {
        if (err) {
            // DynamoDB error
            console.error("Unable to scan the table. Error JSON:", JSON.stringify(err, null, 2));
            var http500result = {
                statusCode:  500,
                headers: { 'Content-Type': 'application/json; charset=utf-8' },
                body: JSON.stringify(err, null, 2)
            };
            context.done(null,http500result);
        } else {
            // We have data...Go through the data returned by DynamoDB
            data.Items.forEach(function(extractinfo) {
                // Save the value to the "global" variable
                extractinforesult = {
                    statusCode:  200,
                    headers: { 'Content-Type': 'application/json; charset=utf-8' },
                    body: JSON.stringify(extractinfo)
                };
            });
        }
        /* Generate Response */
        context.done(null,extractinforesult);
    });
}
