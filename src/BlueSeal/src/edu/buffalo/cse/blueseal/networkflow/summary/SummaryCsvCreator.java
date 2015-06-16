package edu.buffalo.cse.blueseal.networkflow.summary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class SummaryCsvCreator {

	private static HashMap<String, Integer> countsHashMap = new HashMap<String, Integer>();
	private static HashMap<String, Object[]> methodCountsHashMap = new HashMap<String, Object[]>();
	
		
	public static void main(String args[]){
	     CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(args[0]), '\t');
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {		    	 
		    	 if(nextLine.length == 2){
			    	// nextLine[] is an array of values from the line
			        ////System.out.println(nextLine[0] + " " + nextLine[1]);
					//Odd - we need this for adding keys to a HashMap themselves are Hashes.  This blows out the hash map - need to avoid it ...
		        	if(nextLine[0].length() > 300){
						System.out.println("Hold here");
						continue;
					}
			        Integer count = countsHashMap.get(nextLine[0]);
			        if(count == null){
			        	count = new Integer(Integer.parseInt(nextLine[1]));
			        	countsHashMap.put(nextLine[0], count);
			        	if(nextLine[0].equals("<<CL:")){
			        		//System.out.println("Ok...");
			        	}
			        }
			        else{
			        	count = new Integer(count.intValue() + Integer.parseInt(nextLine[1]));
			        	countsHashMap.put(nextLine[0], count);		        	
			        }		        	
		        } else {
		        	Object[] list = methodCountsHashMap.get(nextLine[0]);
			        if(list == null){
			        	list = new Object[3];
			        	list[0] = new Integer(Integer.parseInt(nextLine[1]));
			        	list[1] = new Integer(Integer.parseInt(nextLine[2]));
			        	list[2] = nextLine[3].toString();
			        	methodCountsHashMap.put(nextLine[0], list);
			        }
			        else{
			        	list[0] = new Integer(((Integer)list[0]).intValue() + Integer.parseInt(nextLine[1]));
			        	list[1] = new Integer(((Integer)list[1]).intValue() + Integer.parseInt(nextLine[2]));
			        	String elementTwo = (String)list[2];
			        	list[2] = elementTwo + nextLine[3];
			        	methodCountsHashMap.put(nextLine[0], list);		        	
			        }		        	
		        }
		     }
		     //System.out.println("ok ... we just examined  " + args[0]);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		CSVWriter csvWriter;
		File file = new File(args[0]);
		try {
			csvWriter = new CSVWriter(new FileWriter(file.getParent() + File.separator + file.getName().replace(".",  "_") + "_" + "summed.csv"), '\t');
			if(countsHashMap.keySet().size() > 0){
				String [] line = new String[2] ;
			     Set<String> keys = countsHashMap.keySet();
			     for (String key : keys) {
			    	 //System.out.println("Key is: " + key);
			    	 if(key.contains("<<CL")){
						System.out.println("hold here");
					}
			    	line[0] = key;
					line[1] = countsHashMap.get(key).toString();
					csvWriter.writeNext(line);
			     }
			     csvWriter.flush();
			     csvWriter.close();				
			} else {
				String [] line = new String[4] ;
			     Set<String> keys = methodCountsHashMap.keySet();
			     for (String key : keys) {
					line[0] = key;
					Object[] list = methodCountsHashMap.get(key);
					line[1] = ((Integer)list[0]).toString();
					line[2] = ((Integer)list[1]).toString();
					line[3] = list[2].toString();
					csvWriter.writeNext(line);
			     }
			     csvWriter.flush();
			     csvWriter.close();				
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private static void createExcelSpreadsheet(){
		//http://viralpatel.net/blogs/java-read-write-excel-file-apache-poi/
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sample sheet");
         
        Map<String, Object[]> data = new HashMap<String, Object[]>();
        data.put("1", new Object[] {"Emp No.", "Name", "Salary"});
        data.put("2", new Object[] {1d, "John", 1500000d});
        data.put("3", new Object[] {2d, "Sam", 800000d});
        data.put("4", new Object[] {3d, "Dean", 700000d});
         
        Set<String> keyset = data.keySet();
        int rownum = 0;
        for (String key : keyset) {
            Row row = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if(obj instanceof Date) 
                    cell.setCellValue((Date)obj);
                else if(obj instanceof Boolean)
                    cell.setCellValue((Boolean)obj);
                else if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Double)
                    cell.setCellValue((Double)obj);
            }
        }
         
        try {
            FileOutputStream out = 
                    new FileOutputStream(new File("C:\\new.xls"));
            workbook.write(out);
            out.close();
            System.out.println("Excel written successfully..");
             
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
}
