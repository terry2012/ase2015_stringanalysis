#!/usr/bin/env python
import os
import re
import subprocess as sub
from datetime import datetime
import subprocess as sub
import threading
import sys


class RunCmd(threading.Thread):
    def __init__(self, cmd, timeout):
        threading.Thread.__init__(self)
        self.apk = cmd[0]
        self.timeout = timeout
    
    def run(self):
        global error_count
        #before execution
        outfile = open(outfile_name, "a")
        sys.stderr = open(outfile_name, "a")
        outputList = []
        t_start = datetime.now()
        print "Executing "+self.apk+" at "+t_start.strftime('%m/%d/%Y %H:%M:%S')
        outfile.write("Executing "+self.apk+" at "+t_start.strftime('%m/%d/%Y %H:%M:%S')+"\n")
        outfile.flush()
        #execution
        self.p = sub.Popen(["java", "-jar", "-Xmx4096m", "BlueSeal.jar",
                    self.apk, android_jars]#)
                    ,stdout=outfile, stderr=outfile) 
        self.p.wait()
        returncode =  self.p.returncode
        outfile.flush()
        #after execution
        t_end = datetime.now()
        errstr = ""
        if returncode != 0:
            error_count = error_count+1
            errstr = "ERROR!  "
        print errstr+"Done at "+t_end.strftime('%m/%d/%Y %H:%M:%S')
        outfile.write(errstr+"Done at "+t_end.strftime('%m/%d/%Y %H:%M:%S')+"\n")
        t_delta = t_end - t_start
        delta = t_delta.seconds
        hours, remainder = divmod(delta, 3600)
        minutes, seconds = divmod(remainder, 60)
        print errstr+"Summary:"+self.apk+":Time taken-%s:%s:%s" % (hours, minutes, seconds)
        outfile.write(errstr+"Summary:"+self.apk+":Time taken-%s:%s:%s\n" % (hours, minutes, seconds))
        outfile.close()

    
    def Run(self):
        self.start()
        self.join(self.timeout)
        
        if self.is_alive():
            print "force exit on timeout!!!!"
            self.p.terminate()
            self.join()


def dir_list_folder(head_dir):
    global timeout
    global total_count
    outputList = []
    for root, dirs, files in os.walk(head_dir):
        for d in files:
            outputList.append(os.path.join(root, d))
    for list in outputList:
        if re.match(".*apk$", list):
            total_count = total_count+1
            outfile = open(outfile_name, "a")
            print "--------APK number:%s --------------------" % (total_count)
            outfile.write("--------APK number:%s --------------------\n" % (total_count))
            outfile.close()
            RunCmd([list], timeout).Run()
    return outputList

#Edit values below to change timeout or android jar path
total_count = 0;
error_count = 0;
timeout = 300
if len(sys.argv) > 2:
    outfile_name = sys.argv[1]
    print "Output file is "+outfile_name
    android_jars = "/Users/evolutiontheory/codeLibrary/EWorkspace/BlueSeal/android-jars"
    dir_list_folder(sys.argv[2])
    outfile = open(outfile_name, "a")
    print "------------------------------------------"
    print "Total APKS analyzed:%s" % (total_count)
    print "Errors:%s" % error_count
    outfile.write("------------------------------------------")
    outfile.write("Total APKS analyzed:%s" % (total_count))
    outfile.write("Errors:%s" % error_count)
    outfile.close()
    quit()
else:
    print "Specify output file name and directory to browse as arguments!!!"
    quit()
