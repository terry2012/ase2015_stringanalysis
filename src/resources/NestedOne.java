package firstapplication.nestedone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;

import firstapplication.nestedtwo.NestedTwo;

public class NestedOne {
	
	public String returnMethodWithValueCalledTwiceV3(String value)
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
		NestedTwo nestedTwo = new NestedTwo();
		String nested2 = nestedTwo.returnMethodWithValueCalledTwiceV4(dateString);
		return nested2;
	}

}
