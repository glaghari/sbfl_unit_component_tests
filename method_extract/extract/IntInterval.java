package extract;

import java.util.List;

public class IntInterval {
	private static int start, end;
	private static List<Integer> lineNumbers = null;

//	public IntInterval(int start, int end) {
//		this.start = start;
//		this.end = end;
//	}
	
	private static boolean contains(int lineNumber) {
		if(start <= lineNumber && lineNumber <= end)
			return true;
		return false;
	}
	
	public static boolean match(int startLine, int endLine) {
		start = startLine;
		end = endLine;
    	for(int lineNumber : lineNumbers) {
    		if(contains(lineNumber))
				return true;
		}
		return false;
    }

	public static void setLineNumbers(List<Integer> lineNumbers2) {
		lineNumbers = lineNumbers2;
	}
}