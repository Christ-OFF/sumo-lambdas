# Purpose
Provides lambda to be used by sumoport ionic app

# Run locally

1. Install and run DynamoDB locally
    http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.Endpoint.html  
    or -better- use Docker !
    
2. To Run : `java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb`    
2. Follow this documentation to run locally
    http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/CodeSamples.Java.html#CodeSamples.Java.RegionAndEndpoint  
    Main code is :   
`AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", "us-west-2"))
.build();`  

3. Create local tables

- `aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name EXTRACT_INFO --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5`
- `aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name RIKISHIS --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5`
- `aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name RIKISHIS_PICTURES --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
`
# Reference

## Useful commands
- `aws dynamodb list-tables --endpoint-url http://localhost:8000`
- `aws dynamodb describe-table --table-name EXTRACT_INFO`
- `aws dynamodb describe-table --table-name RIKISHIS` 
- `aws dynamodb describe-table --table-name RIKISHIS_PICTURES` 
