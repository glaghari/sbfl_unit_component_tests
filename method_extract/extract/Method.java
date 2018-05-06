package extract;

public class Method {
	private int startLineNumber;
	private int endLineNumber;
	
	public Method(int startLineNumber, int endLineNumber) {
    	this.startLineNumber = startLineNumber;
    	this.endLineNumber = endLineNumber;
    }
        
    public String toString() {
    	return String.valueOf(this.startLineNumber) + "," + startLineNumber + "," + endLineNumber;
    }
    
    public int hashCode() {
    	return startLineNumber + 120 + endLineNumber + startLineNumber * 31 + endLineNumber * 31; 
    }
    
    public boolean equals(Object obj) {
    	if(! obj.getClass().isAssignableFrom(this.getClass()))
    		return false;
    	Method targetMethod = (Method) obj;
    	return this.startLineNumber == targetMethod.startLineNumber && this.endLineNumber == targetMethod.endLineNumber;
    }
}