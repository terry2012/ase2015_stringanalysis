#!/bin/bash
echo "Analysing:$1"
path=/home/fengshen/projects/dataSet/blueseal/apk-datasets/apk_2014_top100/$1
ant -Darg0=$path runTimeOut >> ./result/$1.txt
