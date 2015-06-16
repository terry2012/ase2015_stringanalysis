package edu.buffalo.cse.blueseal.networkflow.interprocedural;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import edu.buffalo.cse.blueseal.BSFlow.BSInterproceduralTransformer;
import edu.buffalo.cse.blueseal.BSFlow.CgTransformer;
import edu.buffalo.cse.blueseal.blueseal.Constants;

import soot.Pack;
import soot.PackManager;
import soot.Transform;
import soot.jimple.toolkits.annotation.callgraph.CallGraphGrapher;

public class NetworkFlowInterProceduralMain {

	static Logger logger = Logger.getLogger(NetworkFlowInterProceduralMain.class);
	public static String APK_NAME;
	
	public static void main(String[] args) {
		if(args.length == 0){
			  System.err.print("Missing apk path. Exit!\n");
			  System.exit(1);
			}
		logger.debug("About to configure");
        	//DOMConfigurator.configure(args[1]);
			String apkFile = args[0];
			String outputDir = args[1];
			APK_NAME = args[2];
			String fileName = new File(apkFile).getName();
			Constants.OUTPUT_DIR = Constants.OUTPUT_DIR + File.separator + outputDir + File.separator + fileName;
			CgTransformer cgTransformer = new CgTransformer(apkFile);
			Pack pack = PackManager.v().getPack("cg");
			pack.add(new Transform("cg.mtran", cgTransformer));		

		
	        
			NetworkFlowTransformer nft = new NetworkFlowTransformer();
			PackManager.v().getPack("wjtp").add(new Transform("wjtp.inter", nft));	
				
			String[] sootArgs;
						sootArgs = new String[]{"-w","-f", "n", "-allow-phantom-refs", "-x",
								"android.support.", "-x", "android.annotation.", 
								"-process-dir", apkFile,
								"-android-jars", Constants.ANDROID_JARS, 
								"-src-prec", "apk",
								"-no-bodies-for-excluded"
								};
			soot.Main.main(sootArgs);
	}

	private static String getApkName(String[] args) {
		String apkpathDirectory = args[0];		
		File apkDirectory = new File(apkpathDirectory);
		File[] files = apkDirectory.listFiles();	
		String apkFile = files[0].getAbsolutePath();
		logger.debug("Analyzing: " + apkFile);
		return apkFile;
	}

	
}
