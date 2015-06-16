while read line           
do
        echo -e "$line \n"
        ant -Darg0=/home/ubuntu/apk-datasets/apk_2014_top100/$line run >>  /home/ubuntu/outputSingle/$line.txt 2>> /home/ubuntu/errorSingle/$line.txt

done<~/apk-datasets/apksToBeTestedByMe.txt

sudo poweroff
