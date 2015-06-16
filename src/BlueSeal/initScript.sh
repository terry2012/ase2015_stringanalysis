while read line           
do
        echo -e "$line \n"
        ant -Darg0=/home/ubuntu/apk-datasets/malicious_apks/$line run >>  /home/ubuntu/outputMalicious/$line.txt 2>> /home/ubuntu/errorMalicious/$line.txt

done<~/apk-datasets/apksToBeTestedByMeMalicious.txt


while read line           
do
        echo -e "$line \n"
        ant -Darg0=/home/ubuntu/apk-datasets/normal_apks/$line run >>  /home/ubuntu/output/$line.txt 2>> /home/ubuntu/error/$line.txt

done<~/apk-datasets/apksToBeTestedByMe.txt

sudo poweroff
