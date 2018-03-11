##################################################################################
# API
##################################################################################

variable "throttling_rate_limit" {
    default = 10
}

variable "throttling_burst_limit" {
    default = 5
}

############
#    S3
############

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

# Role for lambdas used by API
resource "aws_iam_role" "lambdas-api" {
    name = "lambdas-api"
    description = "Role for lambdas used by API Gateway (ReadOnly by the way)"
    assume_role_policy = "${file("policies/lambda-api-role.json")}"
}

# We add to this role the policy to allow logging
resource "aws_iam_role_policy_attachment" "lambdas-api-CloudWatchLogsFullAccess" {
    role = "${aws_iam_role.lambdas-api.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

# We add to this role the policy to read from DynamoDB
resource "aws_iam_role_policy_attachment" "lambdas-api-AmazonDynamoDBReadOnlyAccess" {
    role = "${aws_iam_role.lambdas-api.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess"
}

resource "aws_iam_role" "api" {
    name = "api"
    description = "Role for API to read S3 and Log"
    assume_role_policy = "${file("policies/api-role.json")}"
}

# We add to this role the policy to allow logging
resource "aws_iam_role_policy_attachment" "api-CloudWatchLogsFullAccess" {
    role = "${aws_iam_role.api.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

# We add to this role the policy to read from DynamoDB
resource "aws_iam_role_policy_attachment" "api-AmazonS3ReadOnlyAccess" {
    role = "${aws_iam_role.api.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
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

    role = "${aws_iam_role.lambdas-api.arn}"

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

    role = "${aws_iam_role.lambdas-api.arn}"

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
    name = "Rikishis API"
    description = "API to get rikishis and extract info"
    binary_media_types = [
        "image/jpeg"]
}

data "aws_region" "current-region" {}

##############
# ExtractInfo
##############

resource "aws_api_gateway_resource" "extract-info" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    parent_id = "${aws_api_gateway_rest_api.rikishis.root_resource_id}"
    path_part = "extract-info"
}

resource "aws_api_gateway_method" "extract-info" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.extract-info.id}"
    http_method = "GET"
    authorization = "NONE"
}

resource "aws_api_gateway_method_settings" "extract-info" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    stage_name = "${aws_api_gateway_stage.test.stage_name}"
    method_path = "${aws_api_gateway_resource.extract-info.path_part}/${aws_api_gateway_method.extract-info.http_method}"
    settings {
        throttling_rate_limit = "${var.throttling_rate_limit}"
        throttling_burst_limit = "${var.throttling_burst_limit}"
    }
}

resource "aws_api_gateway_integration" "extract-info" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_method.extract-info.resource_id}"
    http_method = "${aws_api_gateway_method.extract-info.http_method}"
    # Yes a POST ... but why ???
    # See https://docs.aws.amazon.com/apigateway/latest/developerguide/integrating-api-with-aws-services-lambda.html
    # "Lambda requires that the POST request be used to invoke any Lambda function"
    integration_http_method = "POST"
    type = "AWS_PROXY"
    uri = "${aws_lambda_function.extract-info-get.invoke_arn}"
}

resource "aws_lambda_permission" "allow-api-extract-info-lambda" {
    statement_id = "AllowAPIGatewayInvoke"
    action = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.extract-info-get.arn}"
    principal = "apigateway.amazonaws.com"

    # See https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-control-access-using-iam-policies-to-invoke-api.htmls
    source_arn = "arn:aws:execute-api:${data.aws_region.current-region.name}:${data.aws_caller_identity.current-caller.account_id}:${aws_api_gateway_deployment.rikishis.rest_api_id}/*/GET/${aws_api_gateway_resource.extract-info.path_part}"
}

##############
# Rikishis
##############

resource "aws_api_gateway_resource" "rikishis" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    parent_id = "${aws_api_gateway_rest_api.rikishis.root_resource_id}"
    path_part = "rikishis"
}

resource "aws_api_gateway_method" "rikishis" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.rikishis.id}"
    http_method = "GET"
    authorization = "NONE"
}

resource "aws_api_gateway_method_settings" "rikishis" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    stage_name = "${aws_api_gateway_stage.test.stage_name}"
    method_path = "${aws_api_gateway_resource.rikishis.path_part}/${aws_api_gateway_method.rikishis.http_method}"
    settings {
        throttling_rate_limit = "${var.throttling_rate_limit}"
        throttling_burst_limit = "${var.throttling_burst_limit}"
    }
}

resource "aws_api_gateway_integration" "rikishis" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_method.rikishis.resource_id}"
    http_method = "${aws_api_gateway_method.rikishis.http_method}"
    # Yes a POST ... but why ???
    # See https://docs.aws.amazon.com/apigateway/latest/developerguide/integrating-api-with-aws-services-lambda.html
    # "Lambda requires that the POST request be used to invoke any Lambda function"
    integration_http_method = "POST"
    type = "AWS_PROXY"
    uri = "${aws_lambda_function.rikishis-get.invoke_arn}"
}

resource "aws_lambda_permission" "allow-api-rikishis-lambda" {
    statement_id = "AllowAPIGatewayInvoke"
    action = "lambda:InvokeFunction"
    function_name = "${aws_lambda_function.rikishis-get.arn}"
    principal = "apigateway.amazonaws.com"

    # See https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-control-access-using-iam-policies-to-invoke-api.htmls
    source_arn = "arn:aws:execute-api:${data.aws_region.current-region.name}:${data.aws_caller_identity.current-caller.account_id}:${aws_api_gateway_deployment.rikishis.rest_api_id}/*/GET/${aws_api_gateway_resource.rikishis.path_part}"
}

##################################################################################
# DEPLOY API
##################################################################################

resource "aws_api_gateway_stage" "test" {
    stage_name = "test"
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    deployment_id = "${aws_api_gateway_deployment.rikishis.id}"
    description = "For internal tests with low rate limit)"
}

resource "aws_api_gateway_usage_plan" "usage-plan" {
    name = "usage-plan"
    description = "Usage Plan to limit queries to avoid costs"
    product_code = "sumo"

    api_stages {
        api_id = "${aws_api_gateway_rest_api.rikishis.id}"
        stage = "${aws_api_gateway_stage.test.stage_name}"
    }

    throttle_settings {
        burst_limit = 1
        rate_limit = 5
    }
}

resource "aws_api_gateway_deployment" "rikishis" {
    # Do not forget to add depends to deploy
    depends_on = [
        "aws_api_gateway_integration.extract-info",
        "aws_api_gateway_integration.rikishis",
        "aws_api_gateway_integration.picture",
        "aws_api_gateway_method_response.picture-200",
        "aws_api_gateway_method_response.picture-404",
        "aws_api_gateway_integration_response.picture-200",
        "aws_api_gateway_integration_response.picture-404"
    ]
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    stage_name = "test"
}

##################################################################################
# OUTPUT
##################################################################################

output "base_url" {
    value = "${aws_api_gateway_deployment.rikishis.invoke_url}"
}
