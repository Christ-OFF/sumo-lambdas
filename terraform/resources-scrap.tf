##################################################################################
# SCRAP Only resources
##################################################################################

resource "aws_s3_bucket_object" "lambda-rikishis-scrap-jar" {
    key = "lambda-rikishis-scrap"
    bucket = "${aws_s3_bucket.lambdas.id}"
    source = "../lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "application/java-archive"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("../lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"))}"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishi-scrap-jar" {
    key = "lambda-rikishi-scrap"
    bucket = "${aws_s3_bucket.lambdas.id}"
    source = "../lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "application/java-archive"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("../lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"))}"
    tags {
        sumo = "scrap"
    }
}

resource "aws_s3_bucket_object" "lambda-rikishi-picture-scrap-jar" {
    key = "lambda-rikishi-picture-scrap"
    bucket = "${aws_s3_bucket.lambdas.id}"
    source = "../lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"
    content_type = "application/java-archive"
    acl = "private"
    # etag is here to detect changes
    etag = "${md5(file("../lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"))}"
    tags {
        sumo = "scrap"
    }
}


############
#  Roles
############

resource "aws_iam_role" "lambdas-scrap" {
    name = "lambdas-scrap"
    description = "Role for lambdas scrapping content"
    assume_role_policy = "${file("./policies/lambda-scrap-role.json")}"
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

    source_code_hash = "${base64sha256(file("../lambda-rikishis-scrap/target/lambda-rikishis-scrap-0.0.1-SNAPSHOT.jar"))}"

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
data "aws_caller_identity" "current-caller" {
    # Retrieves information about the AWS account corresponding to the
    # access key being used to run Terraform, which we need to populate
    # the "source_account" on the permission resource.
}

resource "aws_lambda_permission" "allow-cloudwatch-rikishis-scrap-lambda" {
    statement_id   = "AllowExecutionFromCloudWatch"
    action         = "lambda:InvokeFunction"
    function_name  = "${aws_lambda_function.rikishis-scrap.function_name}"
    principal      = "events.amazonaws.com"
    source_account = "${data.aws_caller_identity.current-caller.account_id}"
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

    source_code_hash = "${base64sha256(file("../lambda-rikishi-scrap/target/lambda-rikishi-scrap-0.0.1-SNAPSHOT.jar"))}"

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

    source_code_hash = "${base64sha256(file("../lambda-rikishi-picture-scrap/target/lambda-rikishi-picture-scrap-0.0.1-SNAPSHOT.jar"))}"

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
