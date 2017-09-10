Run integration tests with multiple lambdas locally
with the use of dynamoDB running in a docker container

# Run docker Container Manually
docker pull cnadiminti/dynamodb-local
docker run -v "$PWD":/dynamodb_local_db -p 8000:8000 cnadiminti/dynamodb-local:latest
(source ) : https://hub.docker.com/r/cnadiminti/dynamodb-local/

# Create required tables in dynamoDB Manually
`aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name RIKISHIS --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5`  
`aws dynamodb create-table --endpoint-url http://0.0.0.0:8000 --table-name EXTRACT_INFO --attribute-definitions AttributeName=id,AttributeType=N --key-schema AttributeName=id,KeyType=HASH --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5`  

# check if tables exists
`aws dynamodb list-tables --endpoint-url http://0.0.0.0:8000`  
should output :  
`{
    "TableNames": [
        "EXTRACT_INFO", 
        "RIKISHIS"
    ]
}
`

# Use docker in a Junit5 Test

Why Junit5 ?
Because I wanted to use : https://github.com/FaustXVI/junit5-docker
Seen at DevoxxFR 2017 : https://www.youtube.com/watch?v=4RsJjE-K3iA
Slides : http://vincent.demeester.fr/devoxxfr-junit-docker/#/

## sudo docker and uBuntu
The test need to use docker without sudo
Please read this answer https://askubuntu.com/a/477554/528946
Before applying :
1. `sudo groupadd docker`
2. `sudo gpasswd -a $USER docker`
3. Either do a newgrp docker or log out/in to activate the changes to groups.
4. `docker run hello-world`
