##################################################################################
# PROVIDERS
##################################################################################

provider "aws" {
    access_key = "${var.aws_access_key}"
    secret_key = "${var.aws_secret_key}"
    region = "us-west-2"
}

##################################################################################
# RESOURCES
##################################################################################

############
# DynamoDB
############

resource "aws_dynamodb_table" "rikishis" {
    name = "RIKISHIS"
    read_capacity = 5
    write_capacity = 5
    hash_key = "id"

    attribute {
        name = "id"
        type = "N"
    }
    tags {
        sumo = "scrap"
    }
}

resource "aws_dynamodb_table" "extractinfo" {
    name = "EXTRACT_INFO"
    read_capacity = 5
    write_capacity = 5
    hash_key = "id"

    attribute {
        name = "id"
        type = "N"
    }

    tags {
        sumo = "scrap"
    }
}

############
#    S3
############

resource "aws_s3_bucket" "lambdas" {
    bucket = "lambdas.sumo.christoff.net"
    acl = "private"
    force_destroy = "true"
}

resource "aws_s3_bucket" "rikishis" {
    bucket = "rikishis.sumo.christoff.net"
    acl = "private"
    force_destroy = "true"
}
