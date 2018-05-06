package org.lore.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lore.testresultsanalyser.TestCasesReport;
import org.lore.testresultsanalyser.TestReportParser;
import org.lore.testresultsanalyser.TestReportStatistics;

/**
 * @author Gulsher Laghari
 * @version 1.0
 * 
 * Test cases for test statistics
 *
 */
public class TestCasesReportTest {

	private static TestCasesReport testCasesReport;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestReportStatistics testReportStatistics = new TestReportStatistics();
		File testReportDir = new File("test_data");
		TestReportParser testReportParser = new TestReportParser(testReportDir, testReportStatistics);
		testReportParser.parseTestReport();
		testCasesReport = testReportStatistics.getTestCasesReport();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNumberOfFailingTestCasesIs2() {
		assertEquals(2, testCasesReport.getNumberOfFailingTestCases());
	}
	
	@Test
	public void testNumberOfPassingTestCasesIs67() {
		assertEquals(67, testCasesReport.getNumberOfPassingTestCases());
	}

}
