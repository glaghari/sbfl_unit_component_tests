package extract;

public class Statement {
	private int lineNumber;
	
    public Statement(int lineNumber) {
    	this.lineNumber = lineNumber;
    }
        
    public String toString() {
    	return String.valueOf(this.lineNumber);
    }
    
    public int hashCode() {
    	return lineNumber + 120 + lineNumber * 31; 
    }
    
    public boolean equals(Object obj) {
    	if(! obj.getClass().isAssignableFrom(this.getClass()))
    		return false;
    	Statement targetStatement = (Statement) obj;
    	return this.lineNumber == targetStatement.lineNumber;
    }
}