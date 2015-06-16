package edu.buffalo.cse.blueseal.utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;


public class NonBlockingDriver {

	HashMap<Long, String> fileSizes = new HashMap<Long, String >();
	DefaultExecutor[] processes = new DefaultExecutor[5];
	private static long ONE_SECOND = 1000;
	private static long ONE_MINUTE = ONE_SECOND * 60;
	private static long TEN_MINUTE = ONE_MINUTE * 10;
	private static long TWENTY_MINUTE = ONE_MINUTE * 20;
	
	public static void main(String[] args){
		//Passed in directory where the files are
		String directoryLocation = args[0];
		String jarLocation = args[1];
		String xmxArg = args[2];
		String outputDir = args[3];
		NonBlockingDriver nonBlockingDriver = new NonBlockingDriver(directoryLocation, jarLocation, xmxArg, outputDir);
	}

	
	public NonBlockingDriver(String directoryLocation, String jarLocation, String xmxArg, String outputDir) {
		populateFileSizesHashMap(directoryLocation);
		Set<Long> keys = fileSizes.keySet();
		TreeSet<Long> treeSet = new TreeSet<Long>(keys);
		int count = 0;
		for (Long long1 : treeSet) {
			System.out.println(count++ + " tree Examined");
			String fileName = fileSizes.get(long1);
			File file = new File(fileName);
			String fileNameWithoutPath = file.getName();
			File output = new File("/local/jmdv/output/malgnome/" + fileNameWithoutPath);
			if(output.exists()){
				continue;
			}
			String command = "java -jar -XX:+UseSerialGC -Xmx" + xmxArg + " "+  jarLocation + " " + fileName + " " + outputDir + " " + fileNameWithoutPath;
			System.out.println("");
			boolean added = false;
			for (int i = 0; i < processes.length; i++) {
				if(processes[i] == null){
					addNewProcess(command, i);
					added = true;
					// You have added it, get out of this for loop!
					break;
				}
			}
			if(! added){
				boolean finished = false;				
				while(! finished){
					for (int i = 0; i < processes.length; i++) {
						if(processes[i].getWatchdog().isWatching()){
							finished = false;								   
						}
						else{
							finished = true;
						}
					}				
				}
				for (int i = 0; i < processes.length; i++) {
					if(! processes[i].getWatchdog().isWatching()){
						addNewProcess(command, i);
						break;
					}
				}					
			}
		}
	}


	private void addNewProcess(String command, int i) {
		try {
			DefaultExecutor executor = new DefaultExecutor();
			ExecuteWatchdog watchdog = new ExecuteWatchdog(TWENTY_MINUTE);
			executor.setWatchdog(watchdog);
			processes[i] = executor;
			CommandLine cmdLine = CommandLine.parse(command);
			executor.execute(cmdLine, null, new SootExecuteResultHandler());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void populateFileSizesHashMap(String directoryLocation) {
		File directory = new File(directoryLocation);
		String[] files = directory.list();
		for (String string : files) {
			String fileName = directoryLocation + File.separator + string;
			File file = new File(fileName);
			Long fileSize = new Long(file.length());
			boolean needsToBeAdded = true;
			while(needsToBeAdded){
				// If it's not in, add it
				if(fileSizes.get(fileSize) == null){
					fileSizes.put(fileSize, fileName);		
					needsToBeAdded = false;
				}
				else{
					// Just bump up the size until we find a good one
					fileSize = new Long(fileSize.longValue() + 1);
				}
			}
		}
	}	
	class SootExecuteResultHandler implements ExecuteResultHandler{

		@Override
		public void onProcessComplete(int arg0) {
			// TODO Auto-generated method stub	
		}

		@Override
		public void onProcessFailed(ExecuteException arg0) {
			// TODO Auto-generated method stub
		}
		
	}}
