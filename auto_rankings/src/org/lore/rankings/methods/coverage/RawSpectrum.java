package org.lore.rankings.methods.coverage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.tcm.coverage.ProgramTCM;
import org.lore.util.Utils;

public class RawSpectrum {
	
	
//    static Logger logger = Logger.getLogger(MethodsCoverageRanking.class);
	
    private boolean printCompleteLog = false;
    
    /**
     * This is the TCM of all the entities involved in the program
     */
    private ProgramTCM programTCM = null;
    
    /**
     * The ID of this particular combination of tests for a ranking
     */
    private String ID;
	
    /**
	 * This field refers to all the failing test folders
	 */
	File failingTests[];
	
	/**
	 * This field refers to all the passing test folders
	 */
	private File passingTests[];

	/**
	 * Rankings director where to put the results
	 */
	private File rankingsDir;
		
	/**
	 * This indicates the ID of a faulty entity that needs to be localised.
	 */
	private List<String> faultyMethodIDList = null;

	
	/**
	 * Fault locator functions to calculate the suspiciousness with
	 */
	private List<FaultLocator> faultLocators = null;
	
	/**
	 * Wasted effort with all fault locator functions
	 */
	private String wastedEffort;
	
	/**
	 * Initialises the ranking with a combination of failing and passing tests
	 * @param the ID of this ranking
	 * @param failingingTests failing tests used
	 * @param passingingTests passing tests used
	 * @param rankingsDir 
	 * @param printCompleteLog 
	 */
	public RawSpectrum(File failingingTests[], File passingingTests[], String bugID, List<String> faultyMethodIDList, File rankingsDir, boolean printCompleteLog, List<FaultLocator> faultLocators) {
		this.ID = bugID;
		this.failingTests = failingingTests;
		this.passingTests = passingingTests;
		this.rankingsDir = new File(rankingsDir.getAbsolutePath() + File.separator + RawSpectrum.class.getSimpleName() + File.separator);
		this.printCompleteLog = printCompleteLog;
		this.faultyMethodIDList = faultyMethodIDList;
		this.faultLocators = faultLocators;
		this.programTCM = new ProgramTCM(ID, failingingTests.length, passingingTests.length, this.faultyMethodIDList, this.faultLocators);
	}
	
	/**
	 * This method reads all the patterns files and builds a TCM of all the entities.
	 */
	private void parseCoverage() {
		buildTCM(true);
		buildTCM(false);
	}
	
	
	/**
	 * Saves the TCM of this particular combination of tests for a ranking
	 */
	public void saveTCM() {
		BufferedOutputStream stream = null;
		try {
			if(!rankingsDir.exists())
				rankingsDir.mkdirs();
			File file = new File(rankingsDir.getAbsolutePath() + File.separator + this.ID + Utils.TCM);
			stream = new BufferedOutputStream(new FileOutputStream(file));
			
			StringBuilder buffer = new StringBuilder();
//			buffer.append("Diagnostic accuracy = wasted effort = number of methods to be examined. " + NEW_LINE);
			String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators);
//			buffer.append(faultLocatorHeader);
//			buffer.append(NEW_LINE);
//			buffer.append(this.wastedEffort + NEW_LINE);
			buffer.append("FAULTY Entity ID," + this.programTCM.getFaultyEntityIDListString());
			buffer.append(Utils.NEW_LINE + Utils.FAILING_TESTS + Utils.NEW_LINE);
			
			int i=1;
			StringBuilder failingTests = new StringBuilder();
			StringBuilder passingTests = new StringBuilder();
			StringBuilder commas = new StringBuilder();

			for(File test:this.failingTests) {
				buffer.append(i + Utils.COMMA + test.getName() + Utils.NEW_LINE);
				commas.append(Utils.COMMA);
				failingTests.append("T" + i++ + Utils.COMMA);
			}
			
			buffer.append(Utils.NEW_LINE + Utils.PASSING_TESTS + Utils.NEW_LINE);
			i=1;
			for(File test:this.passingTests) {
				buffer.append(i + Utils.COMMA + test.getName() + Utils.NEW_LINE);
				passingTests.append("T" + i++ + Utils.COMMA);
			}
			
			buffer.append(Utils.NEW_LINE + Utils.COMMA + Utils.FAILING_TESTS + commas.toString() + Utils.PASSING_TESTS + Utils.NEW_LINE);
			buffer.append(Utils.METHOD_ID + Utils.COMMA + failingTests + passingTests + Utils.SPECTRUM);
			buffer.append(faultLocatorHeader);
			
			stream.write(buffer.toString().getBytes());
			String TCM = this.programTCM.getTCM();
			stream.write(TCM.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {try { if(stream!= null) {stream.flush(); stream.close();}} catch (IOException e) {}}
	}
	
//	public void saveTCM() {
//		StringBuilder buffer = new StringBuilder();
////		buffer.append("Diagnostic accuracy = wasted effort = number of methods to be examined. " + NEW_LINE);
//		String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators);
////		buffer.append(faultLocatorHeader);
////		buffer.append(NEW_LINE);
////		buffer.append(this.wastedEffort + NEW_LINE);
//		buffer.append("FAULTY Entity ID," + this.programTCM.getFaultyEntityIDListString());
//		buffer.append(Utils.NEW_LINE + Utils.FAILING_TESTS + Utils.NEW_LINE);
//		int i=1;
//		StringBuilder failingTests = new StringBuilder();
//		StringBuilder passingTests = new StringBuilder();
//		StringBuilder commas = new StringBuilder();
//
//		for(File test:this.failingTests) {
//			buffer.append(i + Utils.COMMA + test.getName() + Utils.NEW_LINE);
//			commas.append(Utils.COMMA);
//			failingTests.append("T" + i++ + Utils.COMMA);
//		}
//		
//		buffer.append(Utils.NEW_LINE + Utils.PASSING_TESTS + Utils.NEW_LINE);
//		i=1;
//		for(File test:this.passingTests) {
//			buffer.append(i + Utils.COMMA + test.getName() + Utils.NEW_LINE);
//			passingTests.append("T" + i++ + Utils.COMMA);
//		}
//		
//		buffer.append(Utils.NEW_LINE + Utils.COMMA + Utils.FAILING_TESTS + commas.toString() + Utils.PASSING_TESTS + Utils.NEW_LINE);
//		buffer.append(Utils.METHOD_ID + Utils.COMMA + failingTests + passingTests + Utils.SPECTRUM);
//		buffer.append(faultLocatorHeader);
//		buffer.append(this.programTCM.getTCM());
//		if(!rankingsDir.exists())
//			rankingsDir.mkdirs();
//		
//		Utils.writeContents(rankingsDir.getAbsolutePath() + File.separator + this.ID + Utils.TCM, buffer, false);
//		
//	}
	
	/**
	 * Saves the TCM of this particular combination of tests for a ranking
	 */
	public void saveRanking() {
		BufferedOutputStream stream = null;
		try {
			if(!rankingsDir.exists())
				rankingsDir.mkdirs();
			File file = new File(rankingsDir.getAbsolutePath() + File.separator + this.ID + Utils.RANKINGS);
			stream = new BufferedOutputStream(new FileOutputStream(file));
			
			StringBuilder buffer = new StringBuilder();
			String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators);
			buffer.append(Utils.METHOD_ID + Utils.COMMA + faultLocatorHeader);
			buffer.append(Utils.NEW_LINE);
			stream.write(buffer.toString().getBytes());
			String rankings = this.programTCM.getRankings();
			stream.write(rankings.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {try { if(stream!= null) {stream.flush(); stream.close();}} catch (IOException e) {}}
	}

//	public void saveRanking() {
//		StringBuilder buffer = new StringBuilder();
//		String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators);
//		buffer.append(Utils.METHOD_ID + Utils.COMMA + faultLocatorHeader);
//		buffer.append(Utils.NEW_LINE);
//		buffer.append(this.programTCM.getRankings());
//		if(!rankingsDir.exists())
//			rankingsDir.mkdirs();
//		Utils.writeContents(rankingsDir.getAbsolutePath() + File.separator + this.ID + Utils.RANKINGS, buffer, false);
//	}
	
	/**
	 * This method reads method coverage file and builds a TCM of all the entities.
	 * @param testStatus whether the tests are passing or failing
	 */
	private void buildTCM(boolean testStatus) {
		
		// If testStatus is true we are dealing with passing tests
//		if(testStatus)
//			logger.debug("Parsing Passing tests");
//		else
//			logger.debug("Parsing Failing tests");
		
		File tests[] = null;
		
		// If testStatus is true we are dealing with passing tests
		if(testStatus)
			tests = passingTests;
		else
			tests = failingTests;
		
		int testIndex = -1;
		// Parse all tests
		for(File testDir:tests) {
			testIndex++;
			File coverageFile = new File(testDir.getPath() + File.separator + Utils.TRACE_ID_FILE);
			if(coverageFile.exists()) {
				String entities[] = Utils.readMethodIDs(coverageFile);
//				logger.debug(Arrays.toString(methods));
				if(entities !=null)
				for(String entity:entities) {
					programTCM.addEntity(entity, testIndex, testStatus);
				} // for
			} // if
		} // outer for
			
	}
	
	
	
	public void initialise() {
//		logger.debug("Starting " + this.getClass().getSimpleName());
		parseCoverage();
//		calculateWastedEffort();
		saveRanking();
		if(printCompleteLog) {
			saveTCM();
		}
	}

//	private void calculateWastedEffort() {
//		this.wastedEffort = this.programTCM.getWastedEffort();
//	}

	public String getWastedEffort() {
		return this.wastedEffort;
	}

	public String getID() {
		return this.ID;
	}

	public int getTotalExecutedEntities() {
		return this.programTCM.getTotalExecutedEntities();
	}

	public String getFaultyEntityIDListString() {
		return this.faultyMethodIDList.toString().replace(',',':');
	}
   
		
}
