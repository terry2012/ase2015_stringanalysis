from fabric.api import env, sudo, run, cd, shell_env, put, get
import boto.ec2
import time
import sys
from config import *

def start():
	'''conn = boto.ec2.connect_to_region("us-west-2", aws_access_key_id='AKIAIYPXU4Q5ELOERABA', aws_secret_access_key= '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')'''

	conn = boto.ec2.connect_to_region(REGION, aws_access_key_id=AWS_ACCESS_KEY_ID, aws_secret_access_key=AWS_SECRET_ACCESS_KEY)

	instances = conn.get_all_instances()
	tags = conn.get_all_tags()

	instancesNameList = list()
	instanceIdList = list()
	for tag in tags:
		instancesNameList.append(str(tag.value))
		instanceIdList.append(str(tag.res_id))
	
	machineName = "BluesealTestingMachine"
	#NumberOfMachines = raw_input("Enter the Number of Testing Machines:: ")
	#TotalNumberOfTestingMachines = int(NumberOfMachines)
	TotalNumberOfTestingMachines = NUM_OF_INSTANCE

	for i in range(0, TotalNumberOfTestingMachines):
		tempName = machineName+str(i+1)
		print tempName
		if tempName in instancesNameList:
			print "ERROR::Instance already present ", tempName

		else:
			print "Creating Instance ", tempName
			reservation =  conn.run_instances(EC2_AMI_ID,
			min_count=1, max_count=1, key_name= KEY_NAME, 
			instance_type='m1.xlarge', 
			security_group_ids=[SECURE_GROUP_ID])
			'''security_group_ids=['sg-253ad24a'])'''
			reservation.instances[0].add_tag('Name', tempName)
			reservation.instances[0].add_tag('MachineNumber', str(i+1))
			reservation.instances[0].add_tag('TotalNumberOfTestingMachines', TotalNumberOfTestingMachines)
				
			print reservation.instances[0].update()
    			instance = None
    			while True:
        			print '.',
			        sys.stdout.flush()
			        dns = reservation.instances[0].dns_name
			        if dns:
			            instance = reservation.instances[0]
			            break
			        time.sleep(5.0)
			        reservation.instances[0].update()
			print 'Instance started.\nPublic DNS: ', instance.dns_name

			env.host_string = instance.dns_name
			
			
		start1(i, TotalNumberOfTestingMachines)


def start1(i, TotalNumberOfTestingMachines):
	env.user = 'ubuntu'
	env.key_filename = KEY_FILE_NAME
	env.warn_only=True
	env.connection_attempts=10

	with shell_env(TotalNumberOfTestingMachines=str(TotalNumberOfTestingMachines), MachineNumber=str(i+1)):
		run('echo $TotalNumberOfTestingMachines')
		run('echo $MachineNumber')

	with cd("~/secure_app_store"):
		run('git pull')

	with cd("~/apk-datasets/normal_apks"):
		run('git pull')
		TotalNumberOfApksToBeTested = run('ls -1R | grep .*.apk | wc -l')
		print "TotalNumberOfApksToBeTested Normal", TotalNumberOfApksToBeTested
		numberOfApksToBeTestedByMe=int(TotalNumberOfApksToBeTested)/int(TotalNumberOfTestingMachines)
		print "numberOfApksToBeTestedByMe Normal", numberOfApksToBeTestedByMe
		
		rangeEnds=numberOfApksToBeTestedByMe*int(i+1)
		
		if i+1==TotalNumberOfTestingMachines:
			leftOver = int(TotalNumberOfApksToBeTested)-int(rangeEnds)
			sudo('ls | head -%s | tail -%s >> ~/apk-datasets/apksToBeTestedByMe.txt' %(str(TotalNumberOfApksToBeTested), str(numberOfApksToBeTestedByMe+leftOver)))
		else:
			sudo('ls | head -%s | tail -%s >> ~/apk-datasets/apksToBeTestedByMe.txt' %(str(rangeEnds), str(numberOfApksToBeTestedByMe)))

	
	with cd("~/apk-datasets/malicious_apks"):
		run('git pull')
		TotalNumberOfApksToBeTested = run('ls -1R | grep .*.apk | wc -l')
		print "TotalNumberOfApksToBeTested Malicious", TotalNumberOfApksToBeTested
		numberOfApksToBeTestedByMe=int(TotalNumberOfApksToBeTested)/int(TotalNumberOfTestingMachines)
		print "numberOfApksToBeTestedByMe Malicious", numberOfApksToBeTestedByMe
		
		rangeEnds=numberOfApksToBeTestedByMe*int(i+1)

		if i+1==TotalNumberOfTestingMachines:
			leftOver = int(TotalNumberOfApksToBeTested)-int(rangeEnds)
			sudo('ls | head -%s | tail -%s >> ~/apk-datasets/apksToBeTestedByMeMalicious.txt' %(str(TotalNumberOfApksToBeTested), str(numberOfApksToBeTestedByMe+leftOver)))
		else:
			sudo('ls | head -%s | tail -%s >> ~/apk-datasets/apksToBeTestedByMeMalicious.txt' %(str(rangeEnds), str(numberOfApksToBeTestedByMe)))

	with cd("~"):
		sudo('mkdir outputMalicious')
		sudo('chmod 777 outputMalicious')
		sudo('mkdir errorMalicious')
		sudo('chmod 777 errorMalicious')
		sudo('mkdir output')
		sudo('chmod 777 output')
		sudo('mkdir error')
		sudo('chmod 777 error')

	with shell_env(JAVA_HOME='/opt/jdk1.6.0_45'):
		with cd("~/secure_app_store/src/BlueSeal"):
			with shell_env(ANT_OPTS='-Xms8196m -Xmx12288m'):
				pid = sudo('dtach -n /tmp/foozle ~/secure_app_store/src/BlueSeal/initScript.sh >& dtach.out')
				print "PID ", pid
	
	print "Machine successfully created\n================================\n"



#function to get output from machines

def getOutput():
	'''conn = boto.ec2.connect_to_region("us-west-2", aws_access_key_id='AKIAIYPXU4Q5ELOERABA', aws_secret_access_key= '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')'''

	conn = boto.ec2.connect_to_region(REGION, aws_access_key_id=AWS_ACCESS_KEY_ID, aws_secret_access_key=AWS_SECRET_ACCESS_KEY)

	instances = conn.get_all_instances()
	tags = conn.get_all_tags()

	instancesNameList = list()
	instanceIdList = list()
	for tag in tags:
		instancesNameList.append(str(tag.value))
		instanceIdList.append(str(tag.res_id))
	
	machineName = "BluesealTestingMachine"
	#print "[Warning!!!] Enter the same number of Machines as entered at the time of Testing"
	#NumberOfMachines = raw_input("Enter the Number of Testing Machines:: ")
	#TotalNumberOfTestingMachines = int(NumberOfMachines)
	TotalNumberOfTestingMachines = NUM_OF_INSTANCE

	for i in range(0, TotalNumberOfTestingMachines):
		tempName = machineName+str(i+1)
		print tempName
		if tempName in instancesNameList:
			print "Starting Instance ", tempName
			indexValue=instancesNameList.index(tempName)
			instanceId=instanceIdList[indexValue]
			startedInstances=conn.start_instances(instance_ids=[instanceId])
			print startedInstances[0].update()
			
			instance = None
    			while True:
        			print '.',
			        sys.stdout.flush()
			        dns = startedInstances[0].dns_name
			        if dns:
			            instance = startedInstances[0]
			            break
			        time.sleep(5.0)
			        startedInstances[0].update()
			print 'Instance started.\nPublic DNS: ', instance.dns_name

			
			env.host_string = instance.dns_name

		else:
			print "ERROR::Instance not present ", tempName
			
		getOutput1(i, TotalNumberOfTestingMachines)
		startedInstances[0].remove_tag('Name', tempName)
		conn.terminate_instances(instance_ids=[instanceId])

def getOutput1(i, TotalNumberOfTestingMachines):
	env.user = 'ubuntu'
	env.key_filename = KEY_FILE_NAME
	env.warn_only=True
	env.connection_attempts=10

	get('~/output/*', LOCAL_OUTPUT_NORMAL)
	get('~/error/*', LOCAL_OUTPUT_NORMAL_ERROR)
	get('~/outputMalicious/*', LOCAL_OUTPUT_MALICIOUS)
	get('~/errorMalicious/*', LOCAL_OUTPUT_MALICIOUS_ERROR)
