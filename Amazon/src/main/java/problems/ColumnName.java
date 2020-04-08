package problems;

public class ColumnName {
	
	public static String getColumnName(int columnNumber) {
		String name = "";
		int division = 26;
		while(columnNumber !=0) {
			int remainder = columnNumber%division;
			if(remainder==0) {
				columnNumber/=division;
				name+= 'A' + (char) (columnNumber-1);
			}else {
				name+= 'A' + (char)(remainder);
				columnNumber/=division;
			}
		}
		
		return name;
	}

}
