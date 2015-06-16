package edu.buffalo.cse.blueseal.BSFlow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.sun.org.apache.xml.internal.security.keys.content.SPKIData;

import edu.buffalo.cse.blueseal.blueseal.Constants;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowInterproceduralAnalysis;
import edu.buffalo.cse.blueseal.networkflow.interprocedural.NetworkFlowTransformer;
import edu.buffalo.cse.blueseal.networkflow.summary.PerApkSummaryGenerator;

import soot.G;
import soot.G.Global;
import soot.Pack;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;

public class InterProceduralMain {
	
	static Logger logger = Logger.getLogger(InterProceduralMain.class);
	
	public final static PrintStream ps = System.out;
	
	private static String currentAppName;
	
	public static String currentAppName(){
		return currentAppName;
	}
	
	public static void mainOld(String[] args) throws FileNotFoundException {
		if(args.length == 0){
		  System.err.print("Missing apk path. Exit!\n");
		  System.exit(1);
		}
		String apkFile = getApkName(args);
	
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.inter", NetworkFlowTransformer.instance()));	
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
		String[] fileParts = apkFile.split(PerApkSummaryGenerator.SPLIT_STRING);
		currentAppName = fileParts[fileParts.length - 1];
		return apkFile;
	}

	
	public static void main(String[] args) throws FileNotFoundException {
		if(args.length == 0){
		  System.err.print("Missing apk path. Exit!\n");
		  System.exit(1);
		}
		String apkpathDirectory = args[0];
		
		File apkDirectory = new File(apkpathDirectory);
		
		File[] files = apkDirectory.listFiles();
		
		SceneTransformer cgTransformer = null;
				
		for (int i = 0; i < 1; i++) {
			
			String apkFile = files[i].getAbsolutePath();
			if(apkFile.contains("com.bestbuy.android-1.apk")
					|| apkFile.contains("com.shazam.android-1.apk") /*serious error that we cannot overcome*/
					|| apkFile.contains("com.zynga.scramble-1.apk") /*serious error - simply hangs*/
					|| apkFile.endsWith(".txt")
					|| apkFile.contains("org.me.mobiexpensifyg-1.apk")) /*serious error - zip file cannot be opened*/ {
				continue;
			}
			logger.debug("Analyzing: " + apkFile);
			String[] fileParts = apkFile.split(PerApkSummaryGenerator.SPLIT_STRING);
			currentAppName = fileParts[fileParts.length - 1];
			//redirect the stdout to get rid of Soot output info
			//System.setOut(new PrintStream("/dev/null"));
			//Get the sources and sinks from the input files
			SourceSink.extractSootSourceSink();
			
			/*
			 * this part is for Jimple
			 */
			//the following transform modifies the callgraph
			{
				cgTransformer = new CgTransformer(apkFile);
//				if(i==0){
					Pack pack = PackManager.v().getPack("cg");
					pack.add(new Transform("cg.mtran", cgTransformer));		
//				}
//				else{
//					Transform t = PackManager.v().getPack("cg").get("cg.mtran");
//					((CgTransformer)t.getTransformer()).setApkLoc(apkFile);
//				}
			
				// TODO Comment out this part and use my own transformer!
//				PackManager.v().getPack("wjtp").
//					add(new Transform("wjtp.inter", BSInterproceduralTransformer.v()));

				//Added custom InterProc Transformer on 4 Dec 2014
					PackManager.v().getPack("wjtp").add(new Transform("wjtp.inter", BSInterproceduralTransformer.v()));
					
				String[] sootArgs;
//				if(i == 0){
					sootArgs = new String[]{"-w","-f", "n", "-allow-phantom-refs", "-x",
							"android.support.", "-x", "android.annotation.", 
							"-process-dir", apkFile,
							"-android-jars", Constants.ANDROID_JARS, 
							"-src-prec", "apk",
							"-no-bodies-for-excluded"
//							"-dump-cfg", "cg.mtran"
							};
					
//				}
//				else {
//					sootArgs = new String[]{ 
//							"-process-dir", apkFile,
//							};					
//				}
				//add the following class to solve a CHATransform exception
				//TODO: if this is the only way, create a separate file to add all basic classes
				Scene.v().addBasicClass("android.support.v4.widget.DrawerLayout",SootClass.BODIES);
				Scene.v().addBasicClass("org.apache.http.client.utils.URLEncodedUtils",SootClass.SIGNATURES);
				Scene.v().addBasicClass("org.apache.http.protocol.BasicHttpContext",SootClass.HIERARCHY);
				logger.debug("Version is " + soot.Main.v().versionString);
				soot.Main.main(sootArgs);
				
				G.reset();
			}
			/** Remove the summary stats file*/
			File file = new File("output.csv");
			if(file.exists()){
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			String header =
					"AppName" + "," +
					"ClassName" + "," +
					"MethodName" + "," +
					"ConstructorType" + "," +
					"NumberURLs" + "," +
					"URLs" + "," +
					"Is Intra" + "," +
					"MethodInset" + "," +
					"Inset" + "," +
					"Outset" + "\r\n";
			String summary = header + NetworkFlowInterproceduralAnalysis.getSummaryStatisticsAsString();
			
			try {
				FileUtils.writeStringToFile(file, summary,"UTF-8");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
//		/*testing shimple*/
//		{
//			SceneTransformer cgTransformer = new CgTransformer(args[0]);
//			
//			PackManager.v().getPack("cg")
//				.add(new Transform("cg.mtran", cgTransformer));
//			PackManager.v().getPack("wstp")
//				.add(new Transform("wstp.inter", BSInterproceduralTransformer.v()));
//	 
//
//			String[] sootArgs = {"-w","-ws","-f", "S", "-allow-phantom-refs", "-x",
//									"android.support.", "-x", "android.annotation.", 
//									"-process-dir", args[0],
//									"-android-jars", Constants.ANDROID_JARS, 
//									"-src-prec", "apk"};
//			soot.Main.main(sootArgs);
//		}

		
		

  }
  
  public static void println(Object o){
	  ps.println(o);
	  
  }

}
