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
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket" "rikishis" {
    bucket = "rikishis.sumo.christoff.net"
    acl = "private"
    force_destroy = "true"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishis-scrap-jar" {
    key = "lambda-rikishis-scrap"
    bucket = "${aws_s3_bucket.lambdas.id}"
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
    bucket = "${aws_s3_bucket.lambdas.id}"
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
    bucket = "${aws_s3_bucket.lambdas.id}"
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
    bucket = "${aws_s3_bucket.lambdas.id}"
    source = "./extract-info-get/target/extract-info-get-0.0.1-SNAPSHOT-assembly.zip"
    content_type = "application/zip"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./extract-info-get/target/extract-info-get-0.0.1-SNAPSHOT-assembly.zip"))}"
    tags {
        sumo = "get"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishis-get-zip" {
    key = "rikishis-get"
    bucket = "${aws_s3_bucket.lambdas.id}"
    source = "./rikishis-get/target/rikishis-get-0.0.1-SNAPSHOT-assembly.zip"
    content_type = "application/zip"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("./rikishis-get/target/rikishis-get-0.0.1-SNAPSHOT-assembly.zip"))}"
    tags {
        sumo = "get"
    }
}

############
#  Roles
############

resource "aws_iam_role" "lambdas-scrap" {
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

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-s3fullaccess" {
    role = "${aws_iam_role.lambdas-scrap.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-dynamodbfullaccess" {
    role = "${aws_iam_role.lambdas-scrap.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-cloudwatchfullaccess" {
    role = "${aws_iam_role.lambdas-scrap.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-scrap-role-snsfullacess" {
    role = "${aws_iam_role.lambdas-scrap.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonSNSFullAccess"
}

resource "aws_iam_role" "lambdas-get" {
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

resource "aws_iam_role_policy_attachment" "lambdas-get-role-cloudwatchfullaccess" {
    role = "${aws_iam_role.lambdas-get.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "lambdas-get-role-dynamo-ro-attach" {
    role = "${aws_iam_role.lambdas-get.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess"
}

############
#  Topics
############

resource "aws_sns_topic" "rikishi-create-update-detail" {
    name = "rikishi-create-update-detail"
}

resource "aws_sns_topic" "rikishi-create-update-picture" {
    name = "rikishi-create-update-picture"
}

############
#  Events
############

resource "aws_cloudwatch_event_rule" "every-month" {
    name = "every-month"
    description = "Trigger refresh every month"
    schedule_expression = "cron(0 0 22 * ? *)"
    is_enabled = "true"
}

############
# Rikishis
############

resource "aws_cloudwatch_log_group" "rikishis-scrap" {
    name = "/aws/lambda/${aws_lambda_function.rikishis-scrap.function_name}"
    retention_in_days = "60"
    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_function" "rikishis-scrap" {

    function_name = "rikishis-scrap"
    description = "Scraps the list of rikishis and push to SNS"

    s3_bucket = "${aws_s3_bucket.lambdas.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishis-scrap-jar.key}"

    runtime = "java8"
    handler = "com.christoff.apps.sumolambda.ScrapRikishisLambdaHandler::handleRequest"

    role = "${aws_iam_role.lambdas-scrap.arn}"

    timeout = "180"
    memory_size = "256"

    source_code_hash = "${base64sha256(file("./lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"))}"

    environment {
        variables {
            extractInfoOnly = "false",
            publishdetailtopic = "${aws_sns_topic.rikishi-create-update-detail.arn}"
            publishpicturetopic = "not_supported_yet"
        }
    }

    tags {
        sumo = "scrap"
    }
}

resource "aws_cloudwatch_event_target" "every-month" {
    rule = "${aws_cloudwatch_event_rule.every-month.name}"
    arn = "${aws_lambda_function.rikishis-scrap.arn}"
}

# lambda permission ? Not IAM role ?
# See https://stackoverflow.com/a/44313827/95040
data "aws_caller_identity" "current" {
    # Retrieves information about the AWS account corresponding to the
    # access key being used to run Terraform, which we need to populate
    # the "source_account" on the permission resource.
}

resource "aws_lambda_permission" "allow-cloudwatch-rikishis-scrap-lambda" {
    statement_id   = "AllowExecutionFromCloudWatch"
    action         = "lambda:InvokeFunction"
    function_name  = "${aws_lambda_function.rikishis-scrap.function_name}"
    principal      = "events.amazonaws.com"
    source_account = "${data.aws_caller_identity.current.account_id}"
    source_arn     = "${aws_cloudwatch_event_rule.every-month.arn}"
}

############
# Rikishi
############

resource "aws_cloudwatch_log_group" "rikishi-scrap" {
    name = "/aws/lambda/${aws_lambda_function.rikishi-scrap.function_name}"
    retention_in_days = "60"
    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_function" "rikishi-scrap" {

    function_name = "rikishi-scrap"
    description = "Triggered by SNS, scrap first rikishi of list. SNS remaining list"

    s3_bucket = "${aws_s3_bucket.lambdas.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishi-scrap-jar.key}"

    runtime = "java8"
    handler = "com.christoff.apps.sumolambda.ScrapRikishiLambdaHandler::handleRequest"

    role = "${aws_iam_role.lambdas-scrap.arn}"

    timeout = "60"
    memory_size = "320"

    source_code_hash = "${base64sha256(file("./lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"))}"

    environment {
        variables {
            publishdetailtopic = "${aws_sns_topic.rikishi-create-update-detail.arn}"
            publishpicturetopic = "${aws_sns_topic.rikishi-create-update-picture.arn}"
        }
    }

    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_permission" "allow-sns-rikishi-scrap-lambda" {
    statement_id  = "AllowExecutionFromSNS"
    action        = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.rikishi-scrap.function_name}"
    principal     = "sns.amazonaws.com"
    source_arn    = "${aws_sns_topic.rikishi-create-update-detail.arn}"
}

resource "aws_sns_topic_subscription" "rikishi-scrap-lambda" {
    topic_arn = "${aws_sns_topic.rikishi-create-update-detail.arn}"
    protocol  = "lambda"
    endpoint  = "${aws_lambda_function.rikishi-scrap.arn}"
}

############
# Picture
############

resource "aws_cloudwatch_log_group" "rikishi-picture-scrap-" {
    name = "/aws/lambda/${aws_lambda_function.rikishi-picture-scrap.function_name}"
    retention_in_days = "60"
    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_function" "rikishi-picture-scrap" {

    function_name = "rikishi-picture-scrap"
    description = "Triggered by SNS, scrap a rikishi picture to S3"

    s3_bucket = "${aws_s3_bucket.lambdas.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishi-picture-scrap-jar.key}"

    runtime = "java8"
    handler = "com.christoff.apps.sumolambda.ScrapRikishiPictureLambdaHandler::handleRequest"

    role = "${aws_iam_role.lambdas-scrap.arn}"

    timeout = "90"
    memory_size = "256"

    source_code_hash = "${base64sha256(file("./lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"))}"

    environment {
        variables {
            bucket = "${aws_s3_bucket.rikishis.bucket}"
        }
    }

    tags {
        sumo = "scrap"
    }
}

resource "aws_lambda_permission" "allow-sns-rikishi-picture-scrap-lambda" {
    statement_id  = "AllowExecutionFromSNS"
    action        = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.rikishi-picture-scrap.function_name}"
    principal     = "sns.amazonaws.com"
    source_arn    = "${aws_sns_topic.rikishi-create-update-picture.arn}"
}

resource "aws_sns_topic_subscription" "rikishi-picture-scrap-lambda" {
    topic_arn = "${aws_sns_topic.rikishi-create-update-picture.arn}"
    protocol  = "lambda"
    endpoint  = "${aws_lambda_function.rikishi-picture-scrap.arn}"
}

############
# Extract
############

resource "aws_cloudwatch_log_group" "extract-info-get" {
    name = "/aws/lambda/${aws_lambda_function.extract-info-get.function_name}"
    retention_in_days = "7"
    tags {
        sumo = "get"
    }
}

resource "aws_lambda_function" "extract-info-get" {

    function_name = "extract-info-get"
    description = "Reads Extract info date from DynamoDB"

    s3_bucket = "${aws_s3_bucket.lambdas.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-extract-info-get-zip.key}"

    runtime = "nodejs6.10"
    handler = "main.handler"

    role = "${aws_iam_role.lambdas-get.arn}"

    timeout = "5"
    memory_size = "128"

    source_code_hash = "${base64sha256(file("./extract-info-get/target/extract-info-get-0.0.1-SNAPSHOT-assembly.zip"))}"

    tags {
        sumo = "get"
    }
}

############
# Rikishis
############

resource "aws_cloudwatch_log_group" "rikishis-get" {
    name = "/aws/lambda/${aws_lambda_function.rikishis-get.function_name}"
    retention_in_days = "7"
    tags {
        sumo = "get"
    }
}

resource "aws_lambda_function" "rikishis-get" {

    function_name = "rikishis-get"
    description = "Reads Rikishis from DynamoDB"

    s3_bucket = "${aws_s3_bucket.lambdas.bucket}"
    s3_key = "${aws_s3_bucket_object.lambda-rikishis-get-zip.key}"

    runtime = "nodejs6.10"
    handler = "main.handler"

    role = "${aws_iam_role.lambdas-get.arn}"

    timeout = "60"
    memory_size = "128"

    source_code_hash = "${base64sha256(file("./rikishis-get/target/rikishis-get-0.0.1-SNAPSHOT-assembly.zip"))}"

    tags {
        sumo = "get"
    }
}

############
#    API
############

resource "aws_api_gateway_rest_api" "rikishis" {
    name        = "Rikishis API"
    description = "API to get rikishis and extract info"
}

resource "aws_api_gateway_resource" "extract-info" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    parent_id   = "${aws_api_gateway_rest_api.rikishis.root_resource_id}"
    path_part   = "extract-info"
}

resource "aws_api_gateway_method" "extract-info" {
    rest_api_id   = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id   = "${aws_api_gateway_resource.extract-info.id}"
    http_method   = "GET"
    authorization = "NONE"
}

resource "aws_api_gateway_integration" "extract-info" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_method.extract-info.resource_id}"
    http_method = "${aws_api_gateway_method.extract-info.http_method}"
    # Yes a POST ... but why ???
    # See https://docs.aws.amazon.com/apigateway/latest/developerguide/integrating-api-with-aws-services-lambda.html
    # "Lambda requires that the POST request be used to invoke any Lambda function"
    integration_http_method = "POST"
    type                    = "AWS_PROXY"
    uri                     = "${aws_lambda_function.extract-info-get.invoke_arn}"
}

resource "aws_api_gateway_deployment" "rikishis" {
    depends_on = [
        "aws_api_gateway_integration.extract-info"
    ]

    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    stage_name  = "test"
}

resource "aws_lambda_permission" "allow-api-extract-info-lambda" {
    statement_id  = "AllowAPIGatewayInvoke"
    action        = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.extract-info-get.arn}"
    principal     = "apigateway.amazonaws.com"

    # See https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-control-access-using-iam-policies-to-invoke-api.htmls
    source_arn = "${aws_api_gateway_deployment.rikishis.execution_arn}/GET/extract-info"
}

##################################################################################
# OUTPUT
##################################################################################

output "base_url" {
    value = "${aws_api_gateway_deployment.rikishis.invoke_url}"
}
