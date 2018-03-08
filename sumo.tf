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

############
# DynamoDB
############

resource "aws_dynamodb_table" "rikishis-table" {
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

resource "aws_dynamodb_table" "extractinfo-table" {
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

resource "aws_s3_bucket" "lambdas-bucket" {
    bucket = "lambdas.sumo.christoff.net"
    acl = "private"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket" "rikishis-bucket" {
    bucket = "rikishis.sumo.christoff.net"
    acl = "private"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishis-scrap-jar" {
    key = "lambda-rikishis-scrap"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "application/java-archive"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"))}"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishi-scrap-jar" {
    key = "lambda-rikishi-scrap"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "application/java-archive"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"))}"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishi-picture-scrap-jar" {
    key = "lambda-rikishi-picture-scrap"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "application/java-archive"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"))}"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-extract-info-get-zip" {
    key = "lambda-extract-info-get"
    bucket = "${aws_s3_bucket.lambdas-bucket.id}"
    source = "./extract-info-get/target/extract-info-get-0.0.1-SNAPSHOT-assembly.zip"
    content_type = "application/zip"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./extract-info-get/target/extract-info-get-0.0.1-SNAPSHOT-assembly.zip"))}"
    tags {
        sumo = "get"
    }
}

############
#  Roles
############

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

resource "aws_iam_role" "lambdas-get-role" {
    name = "lambdas-get"
    description = "Role for lambdas reading content"
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

resource "aws_iam_role_policy_attachment" "lambdas-get-role-cloudwatchfullaccess-attach" {
    role = "${aws_iam_role.lambdas-get-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-get-role-dynamo-ro-attach" {
    role = "${aws_iam_role.lambdas-get-role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess"
}

############
#  Topics
############

resource "aws_sns_topic" "rikishi-create-update-detail-topic" {
    name = "rikishi-create-update-detail"
}

resource "aws_sns_topic" "rikishi-create-update-picture-topic" {
    name = "rikishi-create-update-picture"
}

############
#  Events
############

resource "aws_cloudwatch_event_rule" "every-month-eventrule" {
    name = "every-month"
    description = "Trigger refresh every month"
    schedule_expression = "cron(0 0 22 * ? *)"
    is_enabled = "true"
}

############
# Rikishis
############

resource "aws_cloudwatch_log_group" "rikishis-scrap-log-group" {
    name = "/aws/lambda/${aws_lambda_function.rikishis-scrap-lambda.function_name}"
    retention_in_days = "60"
    tags {
        sumo = "scrap"
    }
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

    tags {
        sumo = "scrap"
    }
}

resource "aws_cloudwatch_event_target" "every-month-target" {
    rule = "${aws_cloudwatch_event_rule.every-month-eventrule.name}"
    arn = "${aws_lambda_function.rikishis-scrap-lambda.arn}"
}

# lambda permission ? Not IAM role ?
# See https://stackoverflow.com/a/44313827/95040
data "aws_caller_identity" "current" {
    # Retrieves information about the AWS account corresponding to the
    # access key being used to run Terraform, which we need to populate
    # the "source_account" on the permission resource.
}

resource "aws_lambda_permission" "allow_cloudwatch-rikishis-scrap-lambda-permission" {
    statement_id   = "AllowExecutionFromCloudWatch"
    action         = "lambda:InvokeFunction"
    function_name  = "${aws_lambda_function.rikishis-scrap-lambda.function_name}"
    principal      = "events.amazonaws.com"
    source_account = "${data.aws_caller_identity.current.account_id}"
    source_arn     = "${aws_cloudwatch_event_rule.every-month-eventrule.arn}"
}

############
# Rikishi
############

resource "aws_cloudwatch_log_group" "rikishi-scrap-log-group" {
    name = "/aws/lambda/${aws_lambda_function.rikishi-scrap-lambda.function_name}"
    retention_in_days = "60"
    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_function" "rikishi-scrap-lambda" {

    function_name = "rikishi-scrap"
    description = "Triggered by SNS, scrap first rikishi of list. SNS remaining list"

    s3_bucket = "${aws_s3_bucket.lambdas-bucket.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishi-scrap-jar.key}"

    runtime = "java8"
    handler = "com.christoff.apps.sumolambda.ScrapRikishiLambdaHandler::handleRequest"

    role = "${aws_iam_role.lambdas-scrap-role.arn}"

    timeout = "60"
    memory_size = "320"

    source_code_hash = "${base64sha256(file("./lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"))}"

    environment {
        variables {
            publishdetailtopic = "${aws_sns_topic.rikishi-create-update-detail-topic.arn}"
            publishpicturetopic = "${aws_sns_topic.rikishi-create-update-picture-topic.arn}"
        }
    }

    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_permission" "allow-sns-rikishi-scrap-lambda-permission" {
    statement_id  = "AllowExecutionFromSNS"
    action        = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.rikishi-scrap-lambda.function_name}"
    principal     = "sns.amazonaws.com"
    source_arn    = "${aws_sns_topic.rikishi-create-update-detail-topic.arn}"
}

resource "aws_sns_topic_subscription" "rikishi-scrap-lambda-target" {
    topic_arn = "${aws_sns_topic.rikishi-create-update-detail-topic.arn}"
    protocol  = "lambda"
    endpoint  = "${aws_lambda_function.rikishi-scrap-lambda.arn}"
}

############
# Picture
############

resource "aws_cloudwatch_log_group" "rikishi-picture-scrap-log-group" {
    name = "/aws/lambda/${aws_lambda_function.rikishi-picture-scrap-lambda.function_name}"
    retention_in_days = "60"
    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_function" "rikishi-picture-scrap-lambda" {

    function_name = "rikishi-picture-scrap"
    description = "Triggered by SNS, scrap a rikishi picture to S3"

    s3_bucket = "${aws_s3_bucket.lambdas-bucket.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishi-picture-scrap-jar.key}"

    runtime = "java8"
    handler = "com.christoff.apps.sumolambda.ScrapRikishiPictureLambdaHandler::handleRequest"

    role = "${aws_iam_role.lambdas-scrap-role.arn}"

    timeout = "90"
    memory_size = "256"

    source_code_hash = "${base64sha256(file("./lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"))}"

    environment {
        variables {
            bucket = "${aws_s3_bucket.rikishis-bucket.bucket}"
        }
    }

    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_permission" "allow-sns-rikishi-picture-scrap-lambda-permission" {
    statement_id  = "AllowExecutionFromSNS"
    action        = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.rikishi-picture-scrap-lambda.function_name}"
    principal     = "sns.amazonaws.com"
    source_arn    = "${aws_sns_topic.rikishi-create-update-picture-topic.arn}"
}

resource "aws_sns_topic_subscription" "rikishi-picture-scrap-lambda-target" {
    topic_arn = "${aws_sns_topic.rikishi-create-update-picture-topic.arn}"
    protocol  = "lambda"
    endpoint  = "${aws_lambda_function.rikishi-picture-scrap-lambda.arn}"
}

############
# Extract
############

resource "aws_cloudwatch_log_group" "extract-info-get-log-group" {
    name = "/aws/lambda/${aws_lambda_function.extract-info-get-lambda.function_name}"
    retention_in_days = "7"
    tags {
        sumo = "get"
    }
}

resource "aws_lambda_function" "extract-info-get-lambda" {

    function_name = "extract-info-get"
    description = "Reads Extract info date from DynamoDB"

    s3_bucket = "${aws_s3_bucket.lambdas-bucket.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-extract-info-get-zip.key}"

    runtime = "nodejs6.10"
    handler = "main.handler"

    role = "${aws_iam_role.lambdas-get-role.arn}"

    timeout = "5"
    memory_size = "128"

    source_code_hash = "${base64sha256(file("./extract-info-get/target/extract-info-get-0.0.1-SNAPSHOT-assembly.zip"))}"

    tags {
        sumo = "get"
    }
}

##################################################################################
# OUTPUT
##################################################################################

/*
output "aws_instance_public_dns" {
    value = "${aws_instance.nginx.public_dns}"
}*/
