##############
# Pictures
##############

resource "aws_api_gateway_resource" "pictures" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    parent_id = "${aws_api_gateway_rest_api.rikishis.root_resource_id}"
    path_part = "pic"
}

module "cors-pictures" {
    source = "github.com/carrot/terraform-api-gateway-cors-module"
    resource_id = "${aws_api_gateway_resource.pictures.id}"
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
}

resource "aws_api_gateway_resource" "picture" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    parent_id = "${aws_api_gateway_resource.pictures.id}"
    path_part = "{id}"
}

module "cors-picture" {
    source = "github.com/carrot/terraform-api-gateway-cors-module"
    resource_id = "${aws_api_gateway_resource.picture.id}"
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
}

resource "aws_api_gateway_method" "picture" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.picture.id}"
    http_method = "GET"
    authorization = "NONE"
    # See https://docs.aws.amazon.com/cli/latest/reference/apigateway/put-method.html
    request_parameters = {
        "method.request.path.id" = true
        "method.request.header.Access-Control-Allow-Origin" = true
    }
}

resource "aws_api_gateway_method_settings" "picture" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    stage_name = "${aws_api_gateway_stage.test.stage_name}"
    method_path = "${aws_api_gateway_resource.picture.path_part}/${aws_api_gateway_method.picture.http_method}"
    settings {
        throttling_rate_limit = "${var.throttling_rate_limit}"
        throttling_burst_limit = "${var.throttling_burst_limit}"
    }
}

resource "aws_api_gateway_integration" "picture" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_method.picture.resource_id}"
    http_method = "${aws_api_gateway_method.picture.http_method}"
    # We are not a lambda ...
    integration_http_method = "GET"
    type = "AWS"
    credentials = "${aws_iam_role.api.arn}"
    # uri is arn:aws:apigateway:{region}:{subdomain.service|service}:{path|action}/{service_api}
    # arn:aws:apigateway:us-west-2:s3:path/rikishis/{file}.jpg
    uri = "arn:aws:apigateway:${data.aws_region.current-region.name}:s3:path/${aws_s3_bucket.rikishis.bucket}/{file}.jpg"
    request_parameters {
        "integration.request.path.file" = "method.request.path.id"
    }
    passthrough_behavior = "WHEN_NO_MATCH"
    content_handling = "CONVERT_TO_BINARY"
}

resource "aws_api_gateway_method_response" "picture-200" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.picture.id}"
    http_method = "${aws_api_gateway_method.picture.http_method}"
    status_code = "200"
    response_parameters = {
        "method.response.header.Content-Type" = true,
        "method.response.header.Content-Length" = true
    }
}

resource "aws_api_gateway_integration_response" "picture-200" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.picture.id}"
    http_method = "${aws_api_gateway_method.picture.http_method}"
    status_code = "${aws_api_gateway_method_response.picture-200.status_code}"
    # could add selection_pattern = "200"
    response_parameters {
        # Params here must be enabled first in aws_api_gateway_method_response
        "method.response.header.Content-Type" = "integration.response.header.Content-Type",
        "method.response.header.Content-Length" = "integration.response.header.Content-Length"
    }
}

resource "aws_api_gateway_method_response" "picture-404" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.picture.id}"
    http_method = "${aws_api_gateway_method.picture.http_method}"
    status_code = "404"
}

resource "aws_api_gateway_integration_response" "picture-404" {
    rest_api_id = "${aws_api_gateway_rest_api.rikishis.id}"
    resource_id = "${aws_api_gateway_resource.picture.id}"
    http_method = "${aws_api_gateway_method.picture.http_method}"
    status_code = "${aws_api_gateway_method_response.picture-404.status_code}"
    selection_pattern = "404"
}
