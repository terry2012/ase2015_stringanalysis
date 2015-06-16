
#EC2 account information, PLEASE CHANGE THIS IF YOU ARE USING A DIFFERENT ACCOUNT
AWS_ACCESS_KEY_ID = "AKIAJLODVS5232SEOW7A"
AWS_SECRET_ACCESS_KEY = "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"

#define the ec2 region, PLSEAS CHANGE THIS IF YOU ARE USING A DIFFERENT REGION
REGION = "us-west-1"

#define the key pair for ec2 instances, PLEASE CHANGE THIS IF YOU ARE USING A DIFFERENT KEY PAIR
KEY_NAME = "blueseal"
KEY_FILE_NAME = "blueseal.pem"

#define instance security group id, PLEASE CHANGE THIS IF YOU ARE USING A DIFFERENT SECURITY GROUP
SECURE_GROUP_ID = "sg-e369cba7"

#get output config, PLEASE CHANGE THIS TO THE REAL DESTINATION
LOCAL_OUTPUT_NORMAL = "~/AWS_EC2/exp_results/normal"
LOCAL_OUTPUT_NORMAL_ERROR = "~/AWS_EC2/exp_results/normal_error"
LOCAL_OUTPUT_MALICIOUS = "~/AWS_EC2/exp_results/malicious"
LOCAL_OUTPUT_MALICIOUS_ERROR = "~/AWS_EC2/exp_results/malicious_error"
LOCAL_OUTPUT_SINGLE = "~/AWS_EC2/exp_results/outputSingle"
LOCAL_OUTPUT_SINGLE_ERROR = "~/AWS_EC2/exp_results/outputSingle_error"

# number of instances needed
NUM_OF_INSTANCE = 20 

#instance ami id
EC2_AMI_ID = "ami-26516863" 


#set process path, PLSASE CHANGE THIS IF YOU ARE ANALYZING A SINGLE FOLDER
SingleFolder = True # set to true for single folder analysis
SingleFolderName = "apk_2014_top100"
