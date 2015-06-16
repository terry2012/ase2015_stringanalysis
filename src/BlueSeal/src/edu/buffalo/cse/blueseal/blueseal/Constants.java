package edu.buffalo.cse.blueseal.blueseal;

public class Constants {

	public final static boolean debugOn = true;
	
	public static String TOOLS;
	public static String DEX2JAR;
	public static String apktool;
	public static String ANDROID_JARS;
	public static String OUTPUT_DIR;
	static{
		if(OsUtils.isWindows()){
			TOOLS = "C:\\apktool\\apktool-install-windows-r05-ibot\\";			
			DEX2JAR = TOOLS + "dex2jar-0.0.9.9/dex2jar.sh";
			apktool = TOOLS + "apktool.bat";
			ANDROID_JARS = "C:\\blueseal\\src\\BlueSeal\\android-jars\\";
			OUTPUT_DIR = "C:\\sac_outputs\\";
		}
		else if(OsUtils.isMac()){
			TOOLS = "/Users/justindelvecchio/blueseal/tools/";						
			DEX2JAR = TOOLS + "dex2jar-0.0.9.9/dex2jar.sh";
			apktool = TOOLS + "apktool";
			ANDROID_JARS = "/Users/justindelvecchio/blueseal/android-jars/";
			OUTPUT_DIR = "/Users/justindelvecchio/blueseal/output/";						
		}
		else{
			TOOLS = "/home/ievolunt/jmdv/tools/";						
			DEX2JAR = TOOLS + "dex2jar-0.0.9.9/dex2jar.sh";
			apktool = TOOLS + "apktool";
			ANDROID_JARS = "/local/fengshen/blueseal/BlueSeal/android-jars/";
			OUTPUT_DIR = "/local/jmdv/output/";
		}
	}
		
	//the following setting should be changed to the local path
	public static String aapt;
	
	public static void setAAPTpath(String path)
	{
		aapt = path;
	}
}
