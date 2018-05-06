package org.lore.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lore.testresultsanalyser.TestCaseStatus;
import org.lore.testresultsanalyser.TestCasesReport;
import org.lore.testresultsanalyser.TestReportParser;
import org.lore.testresultsanalyser.TestReportStatistics;

public class TestReportStatisticsTest {
	
	static TestReportStatistics testReportStatistics;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testReportStatistics = new TestReportStatistics();
		File testReportDir = new File("test_data");
		TestReportParser testReportParser = new TestReportParser(testReportDir, testReportStatistics);
		testReportParser.parseTestReport();
	}

	@Test
	public void testTotalFailuresAre1() {
		assertEquals(1, testReportStatistics.getTotalFailures());
	}
	
	@Test
	public void testTotalErrorsAre1() {
		assertEquals(1, testReportStatistics.getTotalErrors());
	}
	
	@Test
	public void testTotalTestsAre69() {
		assertEquals(69, testReportStatistics.getTotalTests());
	}
	
	@Test
	public void testTotalTestSuitesAre12() {
		assertEquals(12, testReportStatistics.getTotalTestSuites());
	}
	
	@Test
	public void testTestCasesReport() {
		TestCasesReport testCasesReport = testReportStatistics.getTestCasesReport();
		ArrayList<String> failingTestCases =  testCasesReport.getFailingTestCases();
		assertEquals(2, failingTestCases.size());
		Iterator<String> it = failingTestCases.iterator();
		String testCases[] = {"jester.tests.ConfigurationTest.testDefaults",
				"jester.tests.ClassTestTesterTest.originalContentsAreWrittenBack"};
		while(it.hasNext()) {
			String testName = it.next();
			System.out.println(testName);
			assertTrue(testCaseFound(testCases, testName));
		}
		
		assertEquals(2, testCasesReport.getNumberOfFailingTestCases());
		assertEquals(67, testCasesReport.getNumberOfPassingTestCases());
		assertEquals(67, testCasesReport.getPassingTestCases().size());
		assertEquals(testCasesReport.getNumberOfPassingTestCases(), testCasesReport.getPassingTestCases().size());
	}

	private boolean testCaseFound(String[] testCases, String testName) {
		boolean found = false;
		for(String testCase:testCases) {
			if(testCase.equalsIgnoreCase(testName)) {
				found = true;
				break;
			}
		}
		return found;
	}

}
