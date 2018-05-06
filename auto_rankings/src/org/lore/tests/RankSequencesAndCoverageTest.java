package org.lore.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lore.rankings.faultlocators.Ochiai;
import org.lore.testresultsanalyser.TestCasesReport;
import org.lore.testresultsanalyser.TestReportParser;
import org.lore.testresultsanalyser.TestReportStatistics;
import org.lore.util.RankPatternedAndRaw;
import org.lore.rankings.faultlocators.FaultLocator;

public class RankSequencesAndCoverageTest {

	private static RankPatternedAndRaw rankSequencesAndCoverage;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestReportStatistics testReportStatistics = new TestReportStatistics();
		File testReportDir = new File("test_data");
		TestReportParser testReportParser = new TestReportParser(testReportDir, testReportStatistics);
		testReportParser.parseTestReport();
		TestCasesReport testCasesReport = testReportStatistics.getTestCasesReport();
		File logTracesDir = new File("test_data/log");
		File rankingsDir = new File("rankingsDir");
		ArrayList<FaultLocator> faultLocators = new ArrayList<>();
		faultLocators.add(new Ochiai());
		rankSequencesAndCoverage = new RankPatternedAndRaw(logTracesDir, rankingsDir, testCasesReport, faultLocators, "patterns", false);
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTracesDirExists() {
		assertTrue(rankSequencesAndCoverage.tracesDirExists());
	}
	
	@Test
	public void testTtracesDirIsNotEmpty() {
		assertFalse(rankSequencesAndCoverage.tracesDirEmpty());
	}
	
	@Test
	public void testNumberOfTestStatisticsMatchesWithTestDirs() {
		assertTrue(rankSequencesAndCoverage.numberOfTestStatisticsMatchesWithTestDirs());
	}
	
	@Test
	public void testFailingTestCasesAvailaible() {
		assertTrue(rankSequencesAndCoverage.failingTestCasesAvailaible());
	}
	
	@Test
	public void testPassingTestCasesAvailaible() {
		assertTrue(rankSequencesAndCoverage.passingTestCasesAvailaible());
	}
	
	@Test
	public void testPassingTestCasesExist() {
		File[] dirs = rankSequencesAndCoverage.getPassingTestCases();
		for(File dir:dirs) {
			assertTrue(dir.exists());
		}
	}
	
	@Test
	public void testFailingTestCasesExist() {
		File[] dirs = rankSequencesAndCoverage.getFailingTestCases();
		for(File dir:dirs) {
			assertTrue(dir.exists());
		}	
	}
	
	@Test
	public void testDoRanking() {
		assertTrue(rankSequencesAndCoverage.doRankings());	
	}
	
}
