package org.lore.testresultsanalyser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * 
 * @author Gulsher Laghari
 * @version 1.0
 * This is for comprehensive report statistics.
 *
 */
public class TestReportStatistics {
	
	HashMap<String, TestSuite> testSuites;
	
	public TestReportStatistics() {
		this.testSuites = new HashMap<String, TestReportStatistics.TestSuite>();
	}
	
	public void addTestSuite(String name) {
		testSuites.put(name, new TestSuite(name));
	}
	
	public void addTestCaseFailures(String testsuite, int failures) {
		testSuites.get(testsuite).setFailures(failures);
	}
	
	public void addTotalTestCases(String testsuite, int total) {
		testSuites.get(testsuite).setTotal(total);
	}
	
	public void addTestCaseErrors(String testsuite, int errors) {
		testSuites.get(testsuite).setErrors(errors);
	}
	
	public int getTotalFailures() {
		int failures=0;
		Collection<TestSuite> testSuiteCollection = testSuites.values();
		Iterator<TestSuite> testSuiteIterator = testSuiteCollection.iterator();
		while(testSuiteIterator.hasNext()) {
			TestSuite testSuite = testSuiteIterator.next();
			failures += testSuite.getFailures();
		}
		return failures;
	}
	
	public TestCasesReport getTestCasesReport() {
		TestCasesReport testCasesReport = new TestCasesReport();
		Collection<TestSuite> collection = this.testSuites.values();
		Iterator<TestSuite> iterator = collection.iterator();
		while(iterator.hasNext()) {
			TestSuite testSuite = iterator.next();
			ArrayList<String> failingTestCases = testSuite.getFailingTestCases();
			ArrayList<String> passingTestCases = testSuite.getPassingTestCases();
			testCasesReport.addFailingTestCases(failingTestCases);
			testCasesReport.addPassingTestCases(passingTestCases);
		}
		return testCasesReport;
	}
	
	public void printSummary(boolean detailedReport) {
		int failures=0;
		int total=0;
		int errors=0;
		int success=0;
		
		Collection<TestSuite> c = testSuites.values();
		Iterator<TestSuite> iterator = c.iterator();
		while(iterator.hasNext()) {
			TestSuite suite = iterator.next();
			failures += suite.getFailures();
			errors	 += suite.getErrors();
			total	 += suite.getTotal();
			success	 += suite.getSuccess();
		}
		System.out.println("Test Suites Statistics \n------------------------");
		System.out.println("TestSuites   : " + getTotalTestSuites());
		System.out.println("Total Tests  : " + total);
		System.out.println("Test Failues : " + failures);
		System.out.println("Test Errors  : " + errors);
		System.out.println("Test Success : " + success);
		
		System.out.println("\nTest Suites \n--------------------");
		System.out.println(getTestSuiteDetails(detailedReport));
		
	}
	
	public int getTotalTestSuites() {
		return this.testSuites.size();
	}

	public String getTestSuiteDetails(boolean detailedReport) {
		StringBuilder testSuiteNames = new StringBuilder();
		int i=0;
		Collection<TestSuite> c = this.testSuites.values();
		Iterator<TestSuite> iterator = c.iterator();
		while(iterator.hasNext()) {
			TestSuite testSuite = iterator.next();
			testSuiteNames.append("\n" + ++i + ". " + testSuite.getName()+"\n");
			testSuiteNames.append("\tTotol Test Cases :" + testSuite.getTotal()+"\n");
			testSuiteNames.append("\tTest Failues     :" + testSuite.getFailures()+"\n");
			testSuiteNames.append("\tTest Errors      :" + testSuite.getErrors()+"\n");
			testSuiteNames.append("\tTest Succes      :" + testSuite.getSuccess()+"\n");
			
			if(detailedReport) {
				testSuiteNames.append(testSuite.getTestCaseDetails());
			}
		}
		
		return testSuiteNames.toString();
	}
	
	public int getTotalErrors() {
		int errors=0;
		Collection<TestSuite> c = this.testSuites.values();
		Iterator<TestSuite> iterator = c.iterator();
		while(iterator.hasNext()) {
			errors += iterator.next().getErrors();
		}
		return errors;
	}
	
	public int getTotalTests() {
		int total=0;
		Collection<TestSuite> c = this.testSuites.values();
		Iterator<TestSuite> iterator = c.iterator();
		while(iterator.hasNext()) {
			total += iterator.next().getTotal();
		}
		return total;
	}
	
	public void addTestCase(String testSuiteName, String testCaseClass,
			String testCaseMethod, TestCaseStatus testStatus) {
		TestSuite testSuite = this.testSuites.get(testSuiteName);
		testSuite.addTestCase(testCaseClass, testCaseMethod, testStatus);
	}
	
	private class TestSuite {
		
		String name;
		private int total;
		private int failures;
		private int errors;
		private ArrayList<TestCase> testCases;
		
		TestSuite(String name) {
			this.name = name;
			this.testCases = new ArrayList<TestCase>();
		}

		public String getTestCaseDetails() {
			StringBuilder testCaseNames = new StringBuilder();
			Iterator<TestCase> iterator = this.testCases.iterator();
			while(iterator.hasNext()) {
				TestCase testCase = iterator.next();
				testCaseNames.append("\n\t" + testCase.getTestStatus().toString() + " - [" + testCase.getMethod()+ "]");
			}
			return testCaseNames.toString();
		}

		public ArrayList<String> getFailingTestCases() {
			ArrayList<String> failingTestCases = new ArrayList<String>();
			Iterator<TestCase> testCaseIterator = this.testCases.iterator();
			while(testCaseIterator.hasNext()) {
				TestCase testCase = testCaseIterator.next();
				if(testCase.getTestStatus() == TestCaseStatus.ERROR || testCase.getTestStatus() == TestCaseStatus.FAIL) {
					failingTestCases.add(testCase.getTestCaseName());
				}
			}
			return failingTestCases;
		}
		
		public ArrayList<String> getPassingTestCases() {
			ArrayList<String> passingTestCases = new ArrayList<String>();
			Iterator<TestCase> testCaseIterator = this.testCases.iterator();
			while(testCaseIterator.hasNext()) {
				TestCase testCase = testCaseIterator.next();
				if(testCase.getTestStatus() == TestCaseStatus.SUCCESS) {
					passingTestCases.add(testCase.getTestCaseName());
				}
			}
			return passingTestCases;
		}
		
		public String getTestCaseDetails(boolean detailedReport) {
			StringBuilder testCaseNames = new StringBuilder();
			Iterator<TestCase> iterator = this.testCases.iterator();
			while(iterator.hasNext()) {
				TestCase testCase = iterator.next();
				testCaseNames.append("\n\tTest Class :" + testCase.getClazz());
				testCaseNames.append("\n\tTest Method :" + testCase.getMethod());
				testCaseNames.append("\n\tTest Status :" + testCase.getTestStatus().toString());
			}
			return testCaseNames.toString();
		}

		public int getSuccess() {
			return total - (errors + failures);
		}

		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
		public void setTotal(int total) {
			this.total = total;
		}
		
		public int getTotal() {
			return total;
		}
		
		public void setErrors(int errors) {
			this.errors += errors;
		}
		
		public int getErrors() {
			return errors;
		}
		
		public void setFailures(int failures) {
			this.failures += failures;
		}
		
		public int getFailures() {
			return failures;
		}
		
		public void addTestCase(String testCaseClass,
				String testCaseMethod, TestCaseStatus testStatus) {
			this.testCases.add(new TestCase(testCaseClass, testCaseMethod, testStatus));
			
			// Following code works well with the statistics. Needs revision.
			// But directly reading from XML file can be performance efficient.
//			this.total++;
//			System.out.println(testCaseClass+this.total);
//			switch (testStatus) {
//				case ERROR:
//					this.errors++;
//					break;
//				case FAIL:
//					this.failures++;
//					break;
//			}
		}
		
		private class TestCase {
			private String clazz;
			private String method;
			TestCaseStatus testStatus;
			
			public TestCase(String clazz, String method, TestCaseStatus testStatus) {
				this.clazz = clazz;
				this.method = method;
				this.testStatus = testStatus;
			}
			
			public String getTestCaseName() {
				return clazz + "." + method;
			}
			
			public String getClazz() {
				return clazz;
			}
			
			public String getMethod() {
				return method;
			}
			
			public TestCaseStatus getTestStatus() {
				return testStatus;
			}
		}
	}
	

}
