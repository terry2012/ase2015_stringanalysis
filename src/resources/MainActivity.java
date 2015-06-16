package firstapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import firstapplication.nestedone.NestedOne;
import firstapplication.nestedtwo.NestedTwo;

import jmdv.cse586.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class MainActivity extends Activity {

	public static String URL_STRING = "http://www.publicstaticstring.com";
	private String urlString = "http://www.yahoo.com/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			String value = "valued call";
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy/MMM/dd HH:mm:ss");
			String dateNow = formatter.format(currentDate.getTime());
			String valueEnhanced = value + dateNow;
			String valuedCallPlus = valueEnhanced
					+ returnMethodWithValueCalledTwice();
			System.out.println(valuedCallPlus);
			
			String call1 = returnMethodWithValue(value);
			System.out.println(call1);
			

			String call2 = returnMethodWithValueCalledTwice("Some string"); 
			String call3 = returnMethodWithValueCalledTwiceV2(call2);
			System.out.println(call2 + call3);
			System.out.println(createsMultipleString());
			System.out.println(createsMultipleString1());
			System.out.println(createsMultipleString2());
			System.out.println(createsMultipleString3());
			
			performUrlConnections();
			
			performParameterPassingTechnique(URL_STRING);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void performParameterPassingTechnique(String urlString) throws MalformedURLException {
		URL url = new URL(urlString);
		url.notify();
	}

	private void performUrlConnections() {
		try {
			System.out.println(createUrlFromStringOneArgConstructor());
			System.out.println(createUrlFromStringOneMultiConstructor());
			System.out.println(createUrlFromFileReadOneArgConstructor());
			System.out.println(createUrlFromFileReadMultiArgConstructor());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean createUrlFromFileReadMultiArgConstructor() throws IOException {
		File file = new File("resources" + File.separator + "research_log.txt");
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");
		URL url = null;
		try {
			while (it.hasNext()) {
				String line = it.nextLine();
				url = new URL(line, "host", "nonsense");			}
		} finally {
			LineIterator.closeQuietly(it);
		}
		return url.equals(new URL("Tate"));
	}

	private boolean createUrlFromFileReadOneArgConstructor() throws IOException {
		File file = new File("resources" + File.separator + "research_log.txt");
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");
		URL url = null;
		try {
			while (it.hasNext()) {
				String line = it.nextLine();
				url = new URL(line);			}
		} finally {
			LineIterator.closeQuietly(it);
		}
		return url.equals(new URL("Tate"));
	}

	private boolean createUrlFromStringOneMultiConstructor() throws MalformedURLException {
		URL url = new URL(urlString, "host", "file");
		return url.equals(new URL("Tate"));
	}

	private String createUrlFromStringOneArgConstructor() throws MalformedURLException {
		URL url = new URL(urlString);
		return url.toString();
	}

	private String createsMultipleString() {
		String call1 = "here is a new String";
		String call2 = this.toString();
		String call3 = "half as long";
		return call1 + call2 + call3;
	}

	private String createsMultipleString1() {
		String call1 = "here is a new String";
		System.out.println(call1);
		String call2 = this.toString();
		System.out.println(call2);
		String call3 = "half as long";
		System.out.println(call3);
		return call1 + call2 + call3;
	}

	private String createsMultipleString2() {
		String call1 = new char[]{'a','b'}.toString();
		System.out.println(call1);
		String call2 = call1 + "howdy";
		System.out.println(call2);
		String call3 = "half as long";
		System.out.println(call3);
		return call1;
	}

	private String createsMultipleString3() {
		String call1 = "here is a new String";
		if(this.getClass().toString().length() > call1.length()){
			String call2 = this.toString();			
		}
		String call3 = "half as long";
		return call3;
	}

	
	private String returnMethodWithValueCalledTwice() {
		Calendar currentDate = Calendar.getInstance(); // Get the current date
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy/MMM/dd HH:mm:ss"); // format it as per your requirement
		String dateNow = formatter.format(currentDate.getTime());
		return dateNow;
	}
	
	private String returnMethodWithValueCalledTwice(String value)
			throws IOException {
		Date date = new Date();
		Date cloneDate = (Date) date.clone();
		String dateString = value;
		if (cloneDate != null) {
			if (cloneDate.after(date)) {
				File file = new File(cloneDate.toString());
				FileInputStream fis = new FileInputStream(file);
				fis.read();
			}
		}
		int intValue = cloneDate.hashCode();
		if (intValue > 0) {
			File file = new File(cloneDate.toString());
			FileInputStream fis = new FileInputStream(file);
			fis.read();
		}
		dateString = dateString + intValue;
		return dateString;
	}

	private String returnMethodWithValueCalledTwiceV2(String value)
			throws IOException {
		Date date = new Date();
		Date cloneDate = (Date) date.clone();
		String dateString = value;
		if (cloneDate != null) {
			if (cloneDate.after(date)) {
				File file = new File(cloneDate.toString());
				FileInputStream fis = new FileInputStream(file);
				fis.read();
			}
		}
		int intValue = cloneDate.hashCode();
		if (intValue > 0) {
			File file = new File(cloneDate.toString());
			FileInputStream fis = new FileInputStream(file);
			fis.read();
		}
		dateString = dateString + intValue;
		NestedOne nestedOne = new NestedOne();
		String nested1 = nestedOne.returnMethodWithValueCalledTwiceV3(dateString);
		return nested1;
	}

	private String returnMethodWithValue(String value) throws IOException {
		Date date = new Date();
		Date cloneDate = (Date) date.clone();
		String dateString = value;
		if (cloneDate != null) {
			if (cloneDate.after(date)) {
				File file = new File(cloneDate.toString());
				FileInputStream fis = new FileInputStream(file);
				fis.read();
			}
		}
		int intValue = cloneDate.hashCode();
		if (intValue > 0) {
			File file = new File(cloneDate.toString());
			FileInputStream fis = new FileInputStream(file);
			fis.read();
		}
		dateString = dateString + intValue;
		return dateString;
	}

	// For testMultipleReturnMethodWithTraceSameClassDeepNesting
	/**
	 * @Override protected void onCreate(Bundle savedInstanceState) {
	 *           super.onCreate(savedInstanceState);
	 *           setContentView(R.layout.activity_main);
	 * 
	 *           try { //Runs void return // voidReturnMethod(); //Runs return
	 *           with no val passed in // String value = returnMethodNoTrace();
	 *           //Runs retrun linked to passed in val. String value =
	 *           "valued call";
	 * 
	 *           Calendar currentDate = Calendar.getInstance(); //Get the
	 *           current date SimpleDateFormat formatter= new
	 *           SimpleDateFormat("yyyy/MMM/dd HH:mm:ss"); //format it as per
	 *           your requirement String dateNow =
	 *           formatter.format(currentDate.getTime());
	 * 
	 *           String valueEnhanced = value + dateNow; String valuedCallPlus =
	 *           returnMethodWithValueCalledTwice(valueEnhanced); String
	 *           valuedCallPlusPlus =
	 *           returnMethodWithValueCalledTwiceV2(valueEnhanced);
	 *           System.out.println(valuedCallPlus + valuedCallPlusPlus);
	 * 
	 *           } catch (Exception e) { // TODO Auto-generated catch block
	 *           e.printStackTrace(); } }
	 * 
	 *           private String returnMethodWithValueCalledTwice(String value)
	 *           throws IOException{ Date date = new Date(); Date cloneDate =
	 *           (Date)date.clone(); String dateString = value; if(cloneDate !=
	 *           null){ if(cloneDate.after(date)){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } } int intValue =
	 *           cloneDate.hashCode(); if(intValue > 0){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } dateString = dateString +
	 *           intValue; return dateString; }
	 * 
	 *           private String returnMethodWithValueCalledTwiceV2(String value)
	 *           throws IOException{ Date date = new Date(); Date cloneDate =
	 *           (Date)date.clone(); String dateString = value; if(cloneDate !=
	 *           null){ if(cloneDate.after(date)){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } } int intValue =
	 *           cloneDate.hashCode(); if(intValue > 0){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } dateString = dateString +
	 *           intValue; String nested1 =
	 *           returnMethodWithValueCalledTwiceV3(dateString); return nested1;
	 *           }
	 * 
	 * 
	 *           private String returnMethodWithValueCalledTwiceV3(String value)
	 *           throws IOException{ Date date = new Date(); Date cloneDate =
	 *           (Date)date.clone(); String dateString = value; if(cloneDate !=
	 *           null){ if(cloneDate.after(date)){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } } int intValue =
	 *           cloneDate.hashCode(); if(intValue > 0){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } dateString = dateString +
	 *           intValue; String nested2 =
	 *           returnMethodWithValueCalledTwiceV4(dateString); return nested2;
	 *           }
	 * 
	 *           private String returnMethodWithValueCalledTwiceV4(String value)
	 *           throws IOException{ Date date = new Date(); Date cloneDate =
	 *           (Date)date.clone(); String dateString = value; if(cloneDate !=
	 *           null){ if(cloneDate.after(date)){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } } int intValue =
	 *           cloneDate.hashCode(); if(intValue > 0){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } dateString = dateString +
	 *           intValue; return dateString; }
	 */

	// For
	// testReturnMethodWithTraceSameClass()
	// testOutsetCaptured()
	// testGraphConnectedBetweenMethods()
	// testGraphCreation()

	/**
	 * @Override protected void onCreate(Bundle savedInstanceState) {
	 *           super.onCreate(savedInstanceState);
	 *           setContentView(R.layout.activity_main);
	 * 
	 *           try { //Runs void return // voidReturnMethod(); //Runs return
	 *           with no val passed in // String value = returnMethodNoTrace();
	 *           //Runs retrun linked to passed in val. String value =
	 *           "valued call"; String valuedCallPlus =
	 *           returnMethodWithValue(value);
	 *           System.out.println(valuedCallPlus);
	 * 
	 *           } catch (Exception e) { // TODO Auto-generated catch block
	 *           e.printStackTrace(); } }
	 * 
	 *           private String returnMethodWithValue(String value) throws
	 *           IOException{ Date date = new Date(); Date cloneDate =
	 *           (Date)date.clone(); String dateString = value; if(cloneDate !=
	 *           null){ if(cloneDate.after(date)){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } } int intValue =
	 *           cloneDate.hashCode(); if(intValue > 0){ File file = new
	 *           File(cloneDate.toString()); FileInputStream fis = new
	 *           FileInputStream(file); fis.read(); } dateString = dateString +
	 *           intValue; return dateString; }
	 */

	// For
	// testReturnMethodNoTrace()
	// testGraphCreation()

	// private String returnMethodNoTrace() throws IOException{
	// Date date = new Date();
	// Date cloneDate = (Date)date.clone();
	// String dateString = "12 jun 2014";
	// if(cloneDate != null){
	// if(cloneDate.after(date)){
	// File file = new File(cloneDate.toString());
	// FileInputStream fis = new FileInputStream(file);
	// fis.read();
	// }
	// }
	// int value = cloneDate.hashCode();
	// if(value > 0){
	// File file = new File(cloneDate.toString());
	// FileInputStream fis = new FileInputStream(file);
	// fis.read();
	// }
	// dateString = dateString + value;
	// return dateString;
	// }

	// For
	// testIntraproceduralSummaryNotNull()
	// private void voidReturnMethod() throws IOException {
	// Date date = new Date();
	// Date cloneDate = (Date)date.clone();
	// if(cloneDate != null){
	// if(cloneDate.after(date)){
	// File file = new File(cloneDate.toString());
	// FileInputStream fis = new FileInputStream(file);
	// fis.read();
	// }
	// }
	// }

}
