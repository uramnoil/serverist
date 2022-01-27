module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "3.1.0"

  name = "sample-vpc"
  cidr = "10.0.0.0/16"

  azs             = ["ap-northeast-3a", "ap-northeast-3b"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true    # 検証用なのでコストを抑えるためNATGatewayは１つにする
  enable_vpn_gateway = false    # 今回必要ないためfalse

  tags = {
    Terraform   = "true"
    Environment = "dev"
  }
}