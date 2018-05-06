package org.lore.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.lore.rankings.FaultyMethodID;
import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.methods.coverage.RawSpectrum;
import org.lore.rankings.methods.sequences.PatternedSpectrum;
import org.lore.testresultsanalyser.TestCasesReport;

public class RankPatternedAndRaw {
	
//	static Logger logger = Logger.getLogger(MethodsSequenceRanking.class);
	
	private File tracesDir;
	private  File rankingsDir;
	private  File aggregateFile;
	private  File perBugFile;
	private  boolean printCompleteLog = false;
	private TestCasesReport testCasesReport = null;
	private List<FaultLocator> faultLocators = null;
	/**
	 * Patterns directory either itemsets or sequences
	 */
	private  String patternsDirectory = null;
	
	public RankPatternedAndRaw(File logDir, File rankingsDir, TestCasesReport testCasesReport, List<FaultLocator> faultLocators, String patternsDirectory, boolean printCompleteLog) {
		this.tracesDir = logDir;
		this.testCasesReport = testCasesReport;
		this.rankingsDir = rankingsDir;
		this.faultLocators = faultLocators;
		this.patternsDirectory = patternsDirectory;
		this.printCompleteLog = printCompleteLog;
//		this.aggregateFile = new File(System.getProperty("user.dir") + File.separator + "_" + this.patternsDirectory + Utils.RANKINGS_FILE);
//		this.perBugFile = new File(rankingsDir.getAbsolutePath() + File.separatorChar + this.patternsDirectory + Utils.RANKINGS_FILE);
	}

	@Deprecated
	public boolean doRankings() {
//		StringBuilder perBugRankings = null;
//		if(printCompleteLog)
//			perBugRankings = new StringBuilder();
//		StringBuilder aggregateRankings = new StringBuilder();
//		
//		if(printCompleteLog) {
//			writePerBugFileHeader();
//		}
		
		if(!requiredChecksAreOK()) return false;
		
		File[] failingTestCases = getFailingTestCases();
		File[] passingTestCases = getPassingTestCases();
		String bugID = FaultyMethodID.getBugID();
		List<String> faultyMethodIDList = FaultyMethodID.getFaultyMethodIDList();
		File failingTestsSet[] = failingTestCases;
		writeAggregateFileHeader();
//		for(int i=0;i<passingTestCases.length;i++) {
//			File[] passingTestCases2 = new File[passingTestCases.length - i];
//			for(int j=i;j<passingTestCases.length;j++) {
//				passingTestCases2[j-i] = passingTestCases[j-i];
//				if(i == 27)
//					System.out.println(passingTestCases[j-i]);
//			}
		//OR
//			File[] passingTestCases2 = {passingTestCases[i]};
//			doRank(failingTestCases, passingTestCases2, bugID + "_" + i, faultyMethodIDList, failingTestsSet);
//		}
		doRank(failingTestCases, passingTestCases, bugID, faultyMethodIDList, failingTestsSet);
		return true;
	}

	public boolean doRawRankings() {
		if(!requiredChecksAreOK()) return false;
		
		File[] failingTestCases = getFailingTestCases();
		File[] passingTestCases = getPassingTestCases();
		String bugID = FaultyMethodID.getBugID();
		List<String> faultyMethodIDList = FaultyMethodID.getFaultyMethodIDList();
		File failingTestsSet[] = failingTestCases;
		doRawRanking(failingTestCases, passingTestCases, bugID, faultyMethodIDList, failingTestsSet);
		return true;
	}
	
	public boolean doPatternRankings() {
		if(!requiredChecksAreOK()) return false;
		
		File[] failingTestCases = getFailingTestCases();
		File[] passingTestCases = getPassingTestCases();
		String bugID = FaultyMethodID.getBugID();
		List<String> faultyMethodIDList = FaultyMethodID.getFaultyMethodIDList();
		File failingTestsSet[] = failingTestCases;
		doPatternRanking(failingTestCases, passingTestCases, bugID, faultyMethodIDList, failingTestsSet);
		return true;
	}

	@Deprecated
	public void doRank(File[] failingTestCases,
			File[] passingTestCases, String bugID,
			List<String> faultyMethodIDList, File[] failingTestsSet) {
		PatternedSpectrum methodsSequenceRanking = new PatternedSpectrum(failingTestsSet, passingTestCases, bugID, faultyMethodIDList, this.rankingsDir, this.printCompleteLog, patternsDirectory, this.faultLocators);
		RawSpectrum methodsCoverageRanking = new RawSpectrum(failingTestsSet, passingTestCases, bugID, faultyMethodIDList, rankingsDir, printCompleteLog, this.faultLocators);
		long startTime = System.currentTimeMillis();
		methodsSequenceRanking.initialise();
		methodsCoverageRanking.initialise();
		
		String methodsSequenceRankingWastedEffort = methodsSequenceRanking.getWastedEffort();
		String methodsCoverageRankingWastedEffort = methodsCoverageRanking.getWastedEffort();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		String timeString = String.format("%02d:%02d", 
			    TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
			    TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime))
			);
		StringBuilder aggregateRankings = new StringBuilder();
		
		aggregateRankings.append(methodsSequenceRanking.getID() + Utils.COMMA + methodsSequenceRankingWastedEffort + Utils.COMMA);
		aggregateRankings.append(methodsCoverageRankingWastedEffort + Utils.COMMA + failingTestCases.length + Utils.COMMA);
		aggregateRankings.append(passingTestCases.length + Utils.COMMA + methodsSequenceRanking.getTotalExecutedEntities() + Utils.COMMA);
		aggregateRankings.append(timeString);
		aggregateRankings.append(Utils.NEW_LINE);
		
//		String fileName = "_" + this.patternsDirectory + RANKINGS_FILE;
//		File file = new File(System.getProperty("user.dir") + File.separator + fileName);
		Utils.writeContents(this.aggregateFile.getAbsolutePath(), aggregateRankings, true);
		
		if(printCompleteLog) {
			writePerBugFileHeader();
			StringBuilder perBugRankings = new StringBuilder();
			perBugRankings.append(methodsSequenceRanking.getID() + Utils.COMMA + methodsSequenceRanking.getFaultyEntityIDListString() + Utils.COMMA + methodsSequenceRankingWastedEffort + Utils.COMMA);
			perBugRankings.append(methodsCoverageRankingWastedEffort + Utils.COMMA + failingTestCases.length + Utils.COMMA + passingTestCases.length + Utils.COMMA + methodsSequenceRanking.getTotalExecutedEntities() + Utils.NEW_LINE);
//			fileName = rankingsDir.getAbsolutePath() + File.separatorChar + this.patternsDirectory + RANKINGS_FILE;
			Utils.writeContents(this.perBugFile.getAbsolutePath(), perBugRankings, true);
		}
	}

	public void doRawRanking(File[] failingTestCases,
			File[] passingTestCases, String bugID,
			List<String> faultyMethodIDList, File[] failingTestsSet) {
		RawSpectrum rawRanking = new RawSpectrum(failingTestsSet, passingTestCases, bugID, faultyMethodIDList, rankingsDir, printCompleteLog, this.faultLocators);
		rawRanking.initialise();
	}
	
	public void doPatternRanking(File[] failingTestCases,
			File[] passingTestCases, String bugID,
			List<String> faultyMethodIDList, File[] failingTestsSet) {
		PatternedSpectrum patternedRanking = new PatternedSpectrum(failingTestsSet, passingTestCases, bugID, faultyMethodIDList, this.rankingsDir, this.printCompleteLog, patternsDirectory, this.faultLocators);
		patternedRanking.initialise();
	}

	@Deprecated
	private void writeAggregateFileHeader() {
		if(this.aggregateFile.exists())
			return;
		StringBuilder aggregateRankings = new StringBuilder();
		aggregateRankings.append("ID" + Utils.COMMA);
		String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators, Utils.SEQ);
		aggregateRankings.append(faultLocatorHeader);
		aggregateRankings.append(Utils.COMMA);
		faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators, Utils.COV);
		aggregateRankings.append(faultLocatorHeader);
		aggregateRankings.append(Utils.COMMA + Utils.FAILING_TESTS + Utils.COMMA + Utils.PASSING_TESTS + Utils.COMMA + Utils.EXEC_UUT + Utils.COMMA + "Time" + Utils.NEW_LINE);
		Utils.writeContents(this.aggregateFile.getAbsolutePath(), aggregateRankings, false);
	}

	@Deprecated
	private void writePerBugFileHeader() {
		StringBuilder perBugRankings = new StringBuilder();
		perBugRankings.append(Utils.COMMA + Utils.COMMA);
		perBugRankings.append(this.patternsDirectory.toUpperCase());
		perBugRankings.append(Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.COMMA + "COVERAGE" + Utils.NEW_LINE);
		perBugRankings.append(Utils.RANKING_ID + Utils.COMMA + Utils.METHOD_ID + Utils.COMMA);
		String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators, Utils.SEQ);
		perBugRankings.append(faultLocatorHeader);
		perBugRankings.append(Utils.COMMA);
		faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators, Utils.COV);
		perBugRankings.append(faultLocatorHeader);
		perBugRankings.append(Utils.COMMA + Utils.FAILING_TESTS + Utils.COMMA + Utils.PASSING_TESTS + Utils.COMMA + Utils.EXEC_UUT + Utils.NEW_LINE);
		Utils.writeContents(this.perBugFile.getAbsolutePath(), perBugRankings, false);
	}

	/**
	 * This is a check if rankings computation can start
	 */
	private boolean requiredChecksAreOK() {
		if(!tracesDirExists()) {
//			logger.error("No Traces in the log Dir");
//			logger.error("Terminating without process. See Logs directory");
			System.err.println("traces dir not found!... Terminating without process. See Logs directory");
			return false;
		}
		
		if(tracesDirEmpty()) {
//			logger.error("No Tests in the Traces dir in the log Dir");
//			logger.error("Terminating without process. See Logs directory");
			System.err.println("No traces yet!... Terminating without process. See Logs directory");
			return false;
		}
		
		// Enable following if all test cases are to be used
		
//		if(!numberOfTestStatisticsMatchesWithTestDirs()) {
////			logger.error("Conflict with test report statistics and actual test dirs in the traces dir");
////			logger.error("Terminating without process. See Logs directory");
//			System.err.println("Oops.. Test statistics don't match... Terminating without process. See Logs directory");
//			return false;
//		}
		
		if(!failingTestCasesAvailaible()) {
//			logger.error("There is no failing test!");
//			logger.error("Terminating without process. See Logs directory");
			System.err.println("There is no failing test(s)!.. Terminating without process. See Logs directory");
			return false;
		}
		
		if(!passingTestCasesAvailaible()) {
//			logger.error("There is no passing test!");
//			logger.error("Terminating without process. See Logs directory");
			System.err.println("There is no passing test(s)!... Terminating without process. See Logs directory");
			return false;
		}
		return true;
	}    
	
	public boolean tracesDirExists() {
//		File tracesDir = new File(tracesDir.getAbsolutePath() + File.separatorChar + "traces");
		return tracesDir.exists();
	}
	
	public boolean tracesDirEmpty() {
//		File tracesDir = new File(tracesDir.getAbsolutePath() + File.separatorChar + "traces");
		Utils.remove_DS_Store(tracesDir);
		return tracesDir.listFiles().length < 0;
	}
	
	public boolean numberOfTestStatisticsMatchesWithTestDirs() {
//		File tracesDir = new File(tracesDir.getAbsolutePath() + File.separatorChar + "traces");
		Utils.remove_DS_Store(tracesDir);
		int totalTestDirs = testCasesReport.getNumberOfFailingTestCases() + testCasesReport.getNumberOfPassingTestCases();
		return tracesDir.listFiles().length == totalTestDirs;
	}
	
	public boolean failingTestCasesAvailaible() {
		return testCasesReport.getNumberOfFailingTestCases() > 0;
	}
	
	public boolean passingTestCasesAvailaible() {
		return testCasesReport.getNumberOfPassingTestCases() > 0;
	}
	
	public File[] getFailingTestCases() {
		ArrayList<File> failingTestCases = new ArrayList<File>();
		Iterator<String> failTestsCasesIterator = testCasesReport.getFailingTestCases().iterator();
		while(failTestsCasesIterator.hasNext()) {
//			String testCaseName = tracesDir.getAbsolutePath() + File.separatorChar + "traces" + File.separatorChar + failTestsCasesIterator.next();
			String testCaseName = tracesDir.getAbsolutePath() + File.separatorChar + failTestsCasesIterator.next();
			File testCaseFile = new File(testCaseName);
			failingTestCases.add(testCaseFile);
		}
		File dirs[] = new File[failingTestCases.size()];
		failingTestCases.toArray(dirs);
		return dirs;
	}
	
	public File[] getPassingTestCases() {
		ArrayList<File> passingTestCases = new ArrayList<File>();
		Iterator<String> passTestsCasesIterator = testCasesReport.getPassingTestCases().iterator();
		while(passTestsCasesIterator.hasNext()) {
//			String testCaseName = tracesDir.getAbsolutePath() + File.separatorChar + "traces" + File.separatorChar + passTestsCasesIterator.next();
			String testCaseName = tracesDir.getAbsolutePath() + File.separatorChar + passTestsCasesIterator.next();
			File testCaseFile = new File(testCaseName);
			passingTestCases.add(testCaseFile);
		}
		File dirs[] = new File[passingTestCases.size()];
		passingTestCases.toArray(dirs);
		return dirs;
	}

	public void setPatternsDirectory(String patternsDirectory) {
		this.patternsDirectory = patternsDirectory;
	}

}
