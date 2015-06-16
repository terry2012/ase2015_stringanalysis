package firstapplication.nestedtwo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

public class NestedTwo {
	
	public String returnMethodWithValueCalledTwiceV4(String value)
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


}
