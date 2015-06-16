package edu.buffalo.cse.blueseal.networkflow.summary;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

public class CSVToExcelConverter {

	public static void main(String args[]) throws IOException {
		ArrayList arList = null;
		ArrayList al = null;
		HSSFWorkbook hwb = new HSSFWorkbook();
		
		for (int m = 0; m < args.length; m++) {
			String fileName = args[m];
			String thisLine;
			int count = 0;
			System.out.println("Processing " + fileName);
			FileInputStream fis = new FileInputStream(fileName);
			DataInputStream myInput = new DataInputStream(fis);
			int i = 0;
			arList = new ArrayList();
			while ((thisLine = myInput.readLine()) != null) {
				al = new ArrayList();
				String strar[] = thisLine.split(",");
				for (int j = 0; j < strar.length; j++) {
					al.add(strar[j]);
				}
				arList.add(al);
				i++;
			}

			try {
				File file = new File(fileName);
				HSSFSheet sheet = hwb.createSheet(file.getName());
				for (int k = 0; k < arList.size(); k++) {
					ArrayList ardata = (ArrayList) arList.get(k);
					HSSFRow row = sheet.createRow((short) 0 + k);
					for (int p = 0; p < ardata.size(); p++) {
						HSSFCell cell = row.createCell((short) p);
						String data = ardata.get(p).toString();
						if (data.startsWith("=")) {
							cell.setCellType(Cell.CELL_TYPE_STRING);
							data = data.replaceAll("\"", "");
							data = data.replaceAll("=", "");
							cell.setCellValue(data);
						} else if (data.startsWith("\"")) {
							data = data.replaceAll("\"", "");
							cell.setCellType(Cell.CELL_TYPE_STRING);
							System.out.println("Length is: " + data.length());
							if(data.length() > 32700){
								data = data.substring(0, 32700);
							}
							cell.setCellValue(data);
						} else {
							data = data.replaceAll("\"", "");
							cell.setCellType(Cell.CELL_TYPE_NUMERIC);
							cell.setCellValue(data);
						}
						// */
						// cell.setCellValue(ardata.get(p).toString());
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} // main method ends
			
		}
		
			FileOutputStream fileOut = new FileOutputStream("/Users/justindelvecchio/blueseal/results/composite.xls");
			hwb.write(fileOut);
			fileOut.close();
			System.out.println("Your excel file has been generated");
	}
}