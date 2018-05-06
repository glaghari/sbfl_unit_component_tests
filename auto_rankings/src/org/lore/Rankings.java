package org.lore;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lore.rankings.FaultyMethodID;
import org.lore.rankings.faultlocators.*;
import org.lore.sequencegenerator.SequenceGenerator;
import org.lore.testresultsanalyser.TestCasesReport;
import org.lore.util.RankPatternedAndRaw;
import org.lore.util.Utils;
import org.apache.log4j.Logger;

/**
 * 
 * @author Gulsher Laghari
 * @version 1.0
 * This class is to start the ranking process.
 *
 */
public class Rankings {

//	private static File testReportDir = null;
	private static File tracesDir = null;
	private static File groundTruthDir = null;
	private static File rankingsDir = null;
	
//	private static final boolean GENERATE_PATTERNS = true;
	private static final boolean GENERATE_PATTERNS = false;
	
	private static final boolean USE_ALL_PASSING_TESTS = true;
//	private static final boolean USE_ALL_PASSING_TESTS = false;
	
	
//	private  static final boolean PRINT_COMPLETE_LOG = true;
	private  static final boolean PRINT_COMPLETE_LOG = false;
	
	private static List<FaultLocator> faultLocators = null;
	static Logger logger = Logger.getLogger(Rankings.class);
	
	static int failsTestCounter = 0;
	
	public static void main(String[] args) {
		
		if(args.length < 2) {
			System.out.println("Project directory path missing...");
			System.exit(0);
		}
		else {
//			File projectDir = new File(args[0]);
			tracesDir = new File(args[0]);
			groundTruthDir = new File(args[1]);
			rankingsDir = new File(args[2]);
		}
		
		System.out.println("Started...");
		
		ArrayList<String> failingTests = getFailingTests();
		
		if(failingTests==null || failingTests.size() < 1) {
			System.err.println("Failing Test cases not found");
			System.exit(0);
		}
		
		TestCasesReport testCasesReport = null;
		ArrayList<String> failingTestCases = new ArrayList<String>();
		ArrayList<String> passingTestCases = new ArrayList<String>();
		
		if(USE_ALL_PASSING_TESTS)
			populateFailingAndPassingTest(tracesDir, failingTests, failingTestCases, passingTestCases); // for all pass tests
		else {
			ArrayList<String> passingTests = getPassingTests();
			populateFailingAndPassingTest(failingTests, passingTests, failingTestCases, passingTestCases);
		}

		testCasesReport = new TestCasesReport();
		
		System.out.println("Failing test cases = " + failingTestCases.size());
		System.out.println("Passing test cases = " + passingTestCases.size());
		
		testCasesReport.addFailingTestCases(failingTestCases);
		testCasesReport.addPassingTestCases(passingTestCases);

		// Lets first check if we have a fault ID.
//		FaultyMethodID.processFaultyMethodFile(tracesDir);
		FaultyMethodID.processFaultyMethodFile(tracesDir, groundTruthDir);
		List<String> faultyMethodIDList = FaultyMethodID.getFaultyMethodIDList();
		
		if(faultyMethodIDList == null) {
			System.err.println("Fault ID can't be generated!. Terminating...");
			System.exit(0);
		}
		
		// Now we generate the sequences of min size 1 in the traces.
		if(GENERATE_PATTERNS) {
			SequenceGenerator sequenceGenerator = null;
			int minSequenceSize = 1;
			sequenceGenerator = new SequenceGenerator(tracesDir, minSequenceSize, false);
			sequenceGenerator.generateSequencesWithCharm();
		}
		
		faultLocators = getFaultLocators();
		boolean success = true;
		
		// Now we calculate the rankings...
		RankPatternedAndRaw rankPatternedAndRaw = null;
		rankPatternedAndRaw = new RankPatternedAndRaw(tracesDir, rankingsDir, testCasesReport, faultLocators, Utils.PATTERNS, PRINT_COMPLETE_LOG);
		
		success = rankPatternedAndRaw.doRawRankings();
		
		rankPatternedAndRaw.setPatternsDirectory(Utils.PATTERNS);
		success &= rankPatternedAndRaw.doPatternRankings();

		if(success) { 
			System.out.println("Terminted normally...");
		}
		
	}
	
	private static List<FaultLocator> getFaultLocators() {
		ArrayList<FaultLocator> faultLocators = new ArrayList<>();
		faultLocators.add(new DStar());
		faultLocators.add(new Tarantula());
		faultLocators.add(new TarantulaStar());
		faultLocators.add(new Ochiai());
		faultLocators.add(new Naish2());
		faultLocators.add(new Barinel());
		faultLocators.add(new GP13());
		faultLocators.add(new GP19());
		
		return faultLocators;
	}

	private static void populateFailingAndPassingTest(File logTracesDir, ArrayList<String> failingTests, ArrayList<String> failingTestCases, ArrayList<String> passingTestCases) {
//		File[] allDirs = new File(logTracesDir.getAbsolutePath() + File.separatorChar + "traces").listFiles();
		File[] allDirs = logTracesDir.listFiles();
		for(File file:allDirs) {
			if(file.isDirectory()) {
				if(failingTests.contains(file.getName())) {
					failingTestCases.add(file.getName());
				}
				else
					passingTestCases.add(file.getName());
			}
		}
	}
	
	// To be refactored later duplicates above method
	private static void populateFailingAndPassingTest(ArrayList<String> failingTests, ArrayList<String> passingTests, ArrayList<String> failingTestCases, ArrayList<String> passingTestCases) {
//		File[] allDirs = new File(logTracesDir.getAbsolutePath() + File.separatorChar + "traces").listFiles();
		File[] allDirs = tracesDir.listFiles();
		for(File file:allDirs) {
			if(file.isDirectory()) {
				if(failingTests.contains(file.getName())) {
					failingTestCases.add(file.getName());
				}
				else if(passingTests.contains(file.getName()))
					passingTestCases.add(file.getName());
			}
		}
	}
	
	private static ArrayList<String> getFailingTests() {
		ArrayList<String> failingTestCases = new ArrayList<String>();
//		File failingTestsFile = new File(logTracesDir.getAbsolutePath() + File.separatorChar + Utils.FAILING_TESTS_FILE);
		File failingTestsFile = new File(groundTruthDir.getAbsolutePath() + File.separatorChar + Utils.FAILING_TESTS_FILE);
		if(!failingTestsFile.exists()) {
			System.err.println("File NOT found :" + failingTestsFile.getAbsolutePath());
			return null;
		}
		FileReader reader;
		try {
			reader = new FileReader(failingTestsFile);
		} catch (FileNotFoundException e) {
			System.err.println(failingTestsFile.getAbsolutePath() + " file not found!");
			System.err.println(Utils.getStackStrace(e));
			return null;
		}
		BufferedReader buffer = new BufferedReader(reader);
		String line = null;
		try {
			while((line = buffer.readLine()) !=null) {
				failingTestCases.add(line.replace("::","."));
			}
			
		} catch (Exception e) {
			System.err.println("Error processing file! : " + failingTestsFile.getAbsolutePath());
			System.err.println(Utils.getStackStrace(e));
			return null;
		}
		finally {
			try {
				buffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return failingTestCases;
	}
	
	// To be refactored later, duplicates code from getFailingTests(...)
	private static ArrayList<String> getPassingTests() {
		ArrayList<String> passingTestCases = new ArrayList<String>();
//		File failingTestsFile = new File(logTracesDir.getAbsolutePath() + File.separatorChar + Utils.PASSING_TESTS_FILE);
		File failingTestsFile = new File(groundTruthDir.getAbsolutePath() + File.separatorChar + Utils.PASSING_TESTS_FILE);
		if(!failingTestsFile.exists()) {
			System.err.println("File NOT found :" + failingTestsFile.getAbsolutePath());
			return null;
		}
		FileReader reader;
		try {
			reader = new FileReader(failingTestsFile);
		} catch (FileNotFoundException e) {
			System.err.println(failingTestsFile.getAbsolutePath() + " file not found!");
			System.err.println(Utils.getStackStrace(e));
			return null;
		}
		BufferedReader buffer = new BufferedReader(reader);
		String line = null;
		try {
			while((line = buffer.readLine()) !=null) {
				passingTestCases.add(line.replace("::","."));
			}
			
		} catch (Exception e) {
			System.err.println("Error processing file! : " + failingTestsFile.getAbsolutePath());
			System.err.println(Utils.getStackStrace(e));
			return null;
		}
		return passingTestCases;
	}
}
