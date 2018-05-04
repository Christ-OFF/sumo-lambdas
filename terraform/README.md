# Terraform

## Prepare

You need to create a terraform.tfvars in your home dir
Format of the file is
aws_access_key = "...."
aws_secret_key = "...."
private_key_path = "path_to_your_pem_file"

1. Log on AWS as root user
2. On "Users" https://console.aws.amazon.com/iam/home?region=us-west-2#/users
3. Choose the user who will deploy
4. Choose Security Credentials Tab
5. Choose "Create access key"
6. Save in file terraform.tfvars  

## Commands

Run commands from the terraform folder

~/terraform workspace list
~/terraform workspace new dev
or 
~/terraform workspace select dev
~/terraform init -var-file='~/terraform.tfvars'
~/terraform plan -var-file='~/terraform.tfvars'
~/terraform apply -var-file='~/terraform.tfvars'
