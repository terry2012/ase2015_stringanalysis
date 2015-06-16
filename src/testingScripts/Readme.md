One time configuration:
1. sudo apt-get install python-pip
2. sudo pip install boto
3. sudo pip install fabric

Instruction to start testing:
1. $ fab start
2. It will ask for the number of machines to be used in testing.
3. That's it!!!

Note:: Only follow the below instruction once testing is completed, to check this log on to Amazon EC2 account with credentials:
username: chiragtodarka@gmail.com
password: blueseal
If it is showing no running instance this means testing is completed and you can get output using below instruction.

Instruction to get output from EC2 Machines to local machines:
1. create 4 folders on local machine, one folder for output of normal apks, second for error stream of normal apks, third for output of maliciois apks, fourth for error stream of maliciouls apks.
2. open fabfile.py replace the 'xxxxxx' string in last four lines with the path to the folders created above.
3. $ fab getOutput
4. It will again ask for the number of machines from which you want output, enter the same number of machines as entered at the time of testing.
5. That's it!!!
