package calledclasses;

public class TestCase {
	private String testCaseName = null;
	
	public TestCase(String testCaseName) {
		this.testCaseName = testCaseName;
	}
	
	public boolean match(String testCaseName) {
		return this.testCaseName.equals(testCaseName);
	}
	
	public boolean match(String testCasePackage, String testCaseClass) {
		if(testCaseClass == null || testCasePackage == null)
			return false;
		return this.getPackage().equals(testCasePackage) && this.getTestClassName().equals(testCaseClass);
	}

	public String getName() {
		return this.testCaseName;
	}

	public String getTestClassName() {

		int lastDot = -1;
		lastDot = this.testCaseName.lastIndexOf(".");
		int secondLastDot = this.testCaseName.lastIndexOf(".", lastDot - 1);
				
		String className = this.testCaseName.substring(secondLastDot + 1, lastDot);
		return className;
	}

	public String getPackage() {		
		int lastDot = -1;
		lastDot = this.testCaseName.lastIndexOf(".");
		int secondLastDot = this.testCaseName.lastIndexOf(".", lastDot - 1);

		String packageName = this.testCaseName.substring(0, secondLastDot);
		return packageName;
	}

	public String getTestMethod() {
		int lastDot = -1;
		lastDot = this.testCaseName.lastIndexOf(".");
		String testMethodName = this.testCaseName.substring(lastDot + 1);
		return testMethodName;
	}

}
