##################################################################################
# VARIABLES
##################################################################################

variable "aws_access_key" {}
variable "aws_secret_key" {}
variable "private_key_path" {}
variable "key_name" {
    default = "SumoKeys"
}

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

resource "aws_dynamodb_table" "rikishis-table" {
    name = "RIKISHIS"
    read_capacity = 5
    write_capacity = 5
    hash_key = "id"

    attribute {
        name = "id"
        type = "N"
    }
}

resource "aws_dynamodb_table" "extractinfo-table" {
    name = "EXTRACT_INFO"
    read_capacity = 5
    write_capacity = 5
    hash_key = "id"

    attribute {
        name = "id"
        type = "N"
    }
}

resource "aws_s3_bucket" "lambdas-bucket" {
    bucket = "lambdas.sumo.christoff.net"
    acl = "private"
}

resource "aws_s3_bucket" "rikishis-bucket" {
    bucket = "rikishis.sumo.christoff.net"
    acl = "private"
}

resource "aws_s3_bucket_object" "lambda-rikishis-scrap-jar" {
    key = "lambda-rikishis-scrap"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "'application/java-archive'"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"))}"
}

resource "aws_s3_bucket_object" "lambda-rikishi-scrap-jar" {
    key = "lambda-rikishi-scrap"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "'application/java-archive'"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"))}"
}

resource "aws_s3_bucket_object" "lambda-rikishi-picture-scrap-jar" {
    key = "lambda-rikishi-picture-scrap"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "'application/java-archive'"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"))}"
}

resource "aws_iam_role" "lambdas-scrap-role" {
    name = "lambdas-scrap"
    description = "Role for lambdas scrapping content"
    assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role" "lambdas-invoke-role" {
    name = "lambdas-invoke-scrap"
    description = "Role to Allow call to lambda"
    assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "",
            "Effect": "Allow",
            "Principal": {
                "Service": "events.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-s3fullaccess-attach" {
    role = "${aws_iam_role.lambdas-scrap-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-dynamodbfullaccess-attach" {
    role = "${aws_iam_role.lambdas-scrap-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-cloudwatchfullaccess-attach" {
    role = "${aws_iam_role.lambdas-scrap-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-snsfullacess-attach" {
    role = "${aws_iam_role.lambdas-scrap-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonSNSFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-invoke-role-lambda-attach" {
    role = "${aws_iam_role.lambdas-invoke-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaRole"
}

resource "aws_sns_topic" "rikishi-create-update-detail-topic" {
    name = "rikishi-create-update-detail"
}

resource "aws_sns_topic" "rikishi-create-update-picture-topic" {
    name = "rikishi-create-update-picture"
}

resource "aws_cloudwatch_event_rule" "every-month-eventrule" {
    name = "every-month"
    description = "Trigger refresh every month"
    schedule_expression = "cron(0 0 22 * ? *)"
    # TODO Enable it when all scrap lambdas are OK
    # Note : this flag is NOT seen by terraform as different when changed on AWS Side
    is_enabled = "false"
    role_arn = "${aws_iam_role.lambdas-invoke-role.arn}"
}

resource "aws_lambda_function" "rikishis-scrap-lambda" {

    function_name = "rikishis-scrap"
    description = "Scraps the list of rikishis and push to SNS"

    s3_bucket = "${aws_s3_bucket.lambdas-bucket.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishis-scrap-jar.key}"

    runtime = "java8"
    handler = "com.christoff.apps.sumolambda.ScrapRikishisLambdaHandler::handleRequest"

    role = "${aws_iam_role.lambdas-scrap-role.arn}"

    timeout = "180"
    memory_size = "256"

    source_code_hash = "${base64sha256(file("./lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"))}"

    environment {
        variables {
            extractInfoOnly = "false",
            publishdetailtopic = "${aws_sns_topic.rikishi-create-update-detail-topic.arn}"
            publishpicturetopic = "not_supported_yet"
        }
    }
}

resource "aws_cloudwatch_event_target" "every-month-target" {
    rule = "${aws_cloudwatch_event_rule.every-month-eventrule.name}"
    arn = "${aws_lambda_function.rikishis-scrap-lambda.arn}"
}

##################################################################################
# OUTPUT
##################################################################################

/*
output "aws_instance_public_dns" {
    value = "${aws_instance.nginx.public_dns}"
}*/
