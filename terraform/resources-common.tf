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
    cors_rule {
        allowed_headers = [ "Authorization" ]
        allowed_methods = [ "GET"]
        allowed_origins = [ "*"]
        max_age_seconds = "3000"
    }
}

# See https://stackoverflow.com/a/13300444/95040
resource "aws_s3_bucket_policy" "rikishis" {
    bucket = "${aws_s3_bucket.rikishis.id}"
    policy = "${file("./policies/s3-rikishis-bucket.json")}"
}

