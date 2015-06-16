from fabric.api import env, sudo, run, cd, shell_env, put, get
import boto.ec2
import time
import sys
from config import *

machineName = "BluesealTestingMachine"
instancesNameList=list()
instanceIdList=list()

def test():
  print 'test'

def start():
  conn = boto.ec2.connect_to_region(REGION, aws_access_key_id=AWS_ACCESS_KEY_ID, aws_secret_access_key=AWS_SECRET_ACCESS_KEY)
  instances=conn.get_all_instances()
  tags=conn.get_all_tags()
  for tag in tags:
    instancesNameList.append(str(tag.value))
    instanceIdList.append(str(tag.res_id))

  TotalNumberOfTestingMachines=NUM_OF_INSTANCE
  startAndRun(conn,TotalNumberOfTestingMachines)

def startAndRun(conn,TotalNumberOfTestingMachines):
  for i in range(0, TotalNumberOfTestingMachines):
    tempName = machineName+str(i+1)
    print tempName
    
    if tempName in instancesNameList:
      print "ERROR:Instance already exists", tempName
    else:
      print "Creating Instance", tempName
      #create machine
      reservation = conn.run_instances(EC2_AMI_ID, 
      min_count=1, max_count=1, key_name=KEY_NAME,
      instance_type='m1.xlarge',
      security_group_ids=[SECURE_GROUP_ID])
      reservation.instances[0].add_tag('Name', tempName)
      reservation.instances[0].add_tag('MachineNumber', str(i+1))
      reservation.instances[0].add_tag('TotalNumberOfTestingMachines', TotalNumberOfTestingMachines)

      print reservation.instances[0].update()
      instance = None
      while True:
        dns = reservation.instances[0].dns_name
        if dns:
          instance = reservation.instances[0]
          break
        time.sleep(5.0)
        reservation.instances[0].update()
      print 'Instance started, public DNS:', instance.dns_name
      env.host_string = instance.dns_name
    runTest(i, TotalNumberOfTestingMachines)

def runTest(i, TotalNumberOfTestingMachines):
  env.user='ubuntu'
  env.key_filename = KEY_FILE_NAME
  env.warn_only=True
  env.connection_attempts=10

  with shell_env(TotalNumberOfTestingMachines=str(TotalNumberOfTestingMachines), MachineNumber=str(i+1)):
    run('echo $TotalNumberOfTestingMachines')
    run('echo $MachineNumber')

  with cd("~/secure_app_store"):
    run('git pull')

  with cd("~/apk-datasets/"+SingleFolderName):
    run('git pull')
    TotalNumberOfApksToBeTested = run('ls -lR | grep .*.apk | wc -l')
    print "TotalNumberOfApksToBeTested Normal", TotalNumberOfApksToBeTested
    numberOfApksToBeTestedByMe=int(TotalNumberOfApksToBeTested)/int(TotalNumberOfTestingMachines)
    print "numberOfApksToBeTestedByMe Normal", numberOfApksToBeTestedByMe

    rangeEnds=numberOfApksToBeTestedByMe*int(i+1)

    if i+1==TotalNumberOfTestingMachines:
      leftOver = int(TotalNumberOfApksToBeTested)-int(rangeEnds)
      sudo('ls | head -%s | tail -%s >> ~/apk-datasets/apksToBeTestedByMe.txt' %(str(TotalNumberOfApksToBeTested), str(numberOfApksToBeTestedByMe+leftOver)))
    else:
      sudo('ls | head -%s | tail -%s >> ~/apk-datasets/apksToBeTestedByMe.txt' %(str(rangeEnds), str(numberOfApksToBeTestedByMe)))

  #create output folder
  with cd("~"):
    sudo('mkdir outputSingle')
    sudo('chmod 777 outputSingle')
    sudo('mkdir errorSingle')
    sudo('chmod 777 errorSingle')
    
  #set up environment to run blueseal
  with shell_env(JAVA_HOME='/opt/jdk1.6.0_45'):
    with cd("~/secure_app_store/src/BlueSeal"):
      with shell_env(ANT_OPTS='-Xms8196m -Xmx12288m'):
        pid = sudo('dtach -n /tmp/foozle ~/secure_app_store/src/BlueSeal/testSingleFolder.sh >& dtach.out')
        print "PID", pid
  print "BlueSeal in running on the instance"
  
#function to get output from machines
def getOutput():
  conn = boto.ec2.connect_to_region(REGION, aws_access_key_id=AWS_ACCESS_KEY_ID, aws_secret_access_key=AWS_SECRET_ACCESS_KEY)
  instances = conn.get_all_instances()
  tags = conn.get_all_tags()
  instancesNameList = list()
  instanceIdList = list()
  for tag in tags:
    instancesNameList.append(str(tag.value))
    instanceIdList.append(str(tag.res_id))

  machineName = "BluesealTestingMachine"
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
		
    fetchResults(i, TotalNumberOfTestingMachines)
    startedInstances[0].remove_tag('Name', tempName)
    conn.terminate_instances(instance_ids=[instanceId])

def fetchResults(i, TotalNumberOfTestingMachines):
  env.user = 'ubuntu'
  env.key_filename= KEY_FILE_NAME
  env.warn_only=True
  env.connection_attempts=10

  get('~/outputSingle/*', LOCAL_OUTPUT_SINGLE)
  get('~/errorSingle/*',LOCAL_OUTPUT_SINGLE_ERROR)
