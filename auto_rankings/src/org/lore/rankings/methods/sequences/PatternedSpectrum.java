package org.lore.rankings.methods.sequences;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.tcm.sequences.EntityTCM;
import org.lore.rankings.tcm.sequences.PatternTCM;
import org.lore.util.Utils;

public class PatternedSpectrum {
	
	
//    static Logger logger = Logger.getLogger(MethodsSequenceRanking.class);
    

	private boolean printCompleteLog = false;
    
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
	 * This map holds TCMs of all the entities. The String holds the IDs of entities.
	 */
	public Map<String, EntityTCM> entities;

	/**
	 * This is the set of all the entities (methods) executed. The String holds the IDs of entities.
	 */
	private Set<Integer> totalEntities = null;
	
	/**
	 * This indicates the ID of a faulty entity that needs to be localised.
	 */
	private List<String> faultyMethodIDList = null;
	
	/**
	 * Rankings directory where to put the results
	 */
	private File rankingsDir;
	
	/**
	 * Patterns directory either itemsets or sequences
	 */
	private String patternsDirectory;
	
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
	public PatternedSpectrum(File failingingTests[], File passingingTests[], String bugID, List<String> faultyMethodIDList, File rankingsDir, boolean printCompleteLog, String patternsDirectory, List<FaultLocator> faultLocators) {
		this.ID = bugID;
		this.failingTests = failingingTests;
		this.passingTests = passingingTests;
		this.entities = new HashMap<String, EntityTCM>();
		this.totalEntities = new HashSet<Integer>();
		this.patternsDirectory = patternsDirectory;
		this.rankingsDir = new File(rankingsDir.getAbsolutePath() + File.separatorChar + PatternedSpectrum.class.getSimpleName() + File.separatorChar + this.patternsDirectory + File.separatorChar);
		this.printCompleteLog = printCompleteLog;
		this.faultLocators = faultLocators;
		this.faultyMethodIDList = faultyMethodIDList;
	}
	
	/**
	 * This method reads all the patterns files and builds a TCM of all the entities.
	 */
	private void parsePatterns() {
		parsePatterns(true);
		parsePatterns(false);
	}
	
	/**
	 * Saves the ranking of this particular combination of tests for a ranking
	 */
	public void saveRanking() {
		BufferedOutputStream stream = null;
		try {
			if(!rankingsDir.exists())
				rankingsDir.mkdirs();
			File file = new File(rankingsDir.getAbsolutePath() + File.separatorChar + this.ID + "_" + this.patternsDirectory + Utils.RANKINGS);
			stream = new BufferedOutputStream(new FileOutputStream(file));
		
			StringBuilder buffer = new StringBuilder();
			buffer.append(Utils.METHOD_ID + Utils.COMMA);
			buffer.append(Utils.getFaultLocatorHeader(this.faultLocators));
			//		buffer.append(",Number of " + this.patternsDirectory + NEW_LINE);
			buffer.append(Utils.NEW_LINE);
			stream.write(buffer.toString().getBytes());
			buffer.setLength(0); // Clear the buffer
			
			Iterator<Integer> totalMethodsIterator = this.totalEntities.iterator();
			// Iterate over all executed methods
			while(totalMethodsIterator.hasNext()) {
				String methodID = totalMethodsIterator.next().toString();
				EntityTCM entity = this.entities.get(methodID);
				if(entity == null) {
					String zeroValues = Utils.getZeroValues(this.faultLocators);
					buffer.append(methodID + Utils.COMMA);
					buffer.append(zeroValues);
					//				buffer.append(",0"); // Since entity is null there are already zero patterns 
					buffer.append(Utils.NEW_LINE);
				}
				else {
					buffer.append(methodID + Utils.COMMA);
					for(FaultLocator faultLocator: this.faultLocators) {
						double ranking = entity.getSuspiciousness(faultLocator);
						buffer.append(Utils.getDecimalFormat(ranking));
						buffer.append(Utils.COMMA);
					}
					int length = buffer.length();
					buffer.replace(length-1, length, Utils.NEW_LINE);
					//				buffer.append(entity.getNumberOfPatterns() + NEW_LINE);
				}
				
				stream.write(buffer.toString().getBytes());
				buffer.setLength(0); // Clear the buffer
				
			} // while
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {try { if(stream!= null) {stream.flush(); stream.close();}} catch (IOException e) {}}
	}
	
//	public void saveRanking() {
//		StringBuilder buffer = new StringBuilder();
//		buffer.append(Utils.METHOD_ID + Utils.COMMA);
//		buffer.append(Utils.getFaultLocatorHeader(this.faultLocators));
////		buffer.append(",Number of " + this.patternsDirectory + NEW_LINE);
//		buffer.append(Utils.NEW_LINE);
//		Iterator<Integer> totalMethodsIterator = this.totalEntities.iterator();
//		// Iterate over all executed methods
//		while(totalMethodsIterator.hasNext()) {
//			String methodID = totalMethodsIterator.next().toString();
//			EntityTCM entity = this.entities.get(methodID);
//			if(entity == null) {
//				String zeroValues = Utils.getZeroValues(this.faultLocators);
//				buffer.append(methodID + Utils.COMMA);
//				buffer.append(zeroValues);
////				buffer.append(",0"); // Since entity is null there are already zero patterns 
//				buffer.append(Utils.NEW_LINE);
//			}
//			else {
//				buffer.append(methodID + Utils.COMMA);
//				for(FaultLocator faultLocator: this.faultLocators) {
//					double ranking = entity.getSuspiciousness(faultLocator);
//					buffer.append(Utils.getDecimalFormat(ranking));
//					buffer.append(Utils.COMMA);
//				}
//				int length = buffer.length();
//				buffer.replace(length-1, length, Utils.NEW_LINE);
////				buffer.append(entity.getNumberOfPatterns() + NEW_LINE);
//			}
//		}
//		
//		if(!rankingsDir.exists())
//			rankingsDir.mkdirs();
//		Utils.writeContents(rankingsDir.getAbsolutePath() + File.separatorChar + this.ID + "_" + this.patternsDirectory + Utils.RANKINGS, buffer, false);
//	}
	
	/**
	 * Saves the TCM of this particular combination of tests for a ranking
	 */
	public void saveTCM() {
		BufferedOutputStream stream = null;
		try {
			if(!rankingsDir.exists())
				rankingsDir.mkdirs();
			File file = new File(rankingsDir.getAbsolutePath() + File.separatorChar  + this.ID + "_" + this.patternsDirectory + Utils.TCM);
			stream = new BufferedOutputStream(new FileOutputStream(file));
			
			StringBuilder buffer = new StringBuilder();
			//		buffer.append("Diagnostic accuracy = wasted effort = number of methods to be examined. " + NEW_LINE);
			String faultLocatorHeader = Utils.getFaultLocatorHeader(this.faultLocators);
			//		buffer.append(faultLocatorHeader);
			//		buffer.append(NEW_LINE);
			//		buffer.append(getWastedEffort() + NEW_LINE);
			// Replacing this to avoid double calculations
			//		buffer.append(this.wastedEffort + NEW_LINE);

			buffer.append("FAULTY Entity ID" + Utils.COMMA + this.faultyMethodIDList.toString().replace(',', ':'));
			ArrayList<EntityTCM> faultyEntities = new ArrayList<>();
			for(String faultyEntity:this.faultyMethodIDList)
				faultyEntities.add(this.entities.get(faultyEntity));

			buffer.append(Utils.NEW_LINE + "Number of " + this.patternsDirectory + Utils.COMMA);
			buffer.append("[");
			Iterator<EntityTCM> faultyEntitiesIterator = faultyEntities.iterator();
			while(faultyEntitiesIterator.hasNext()) {
				EntityTCM faultyEntity = faultyEntitiesIterator.next();
				int numberOfSequences = 0;
				if(faultyEntity != null)
					numberOfSequences = faultyEntity.getNumberOfPatterns();
				buffer.append(numberOfSequences);
				if(faultyEntitiesIterator.hasNext())
					buffer.append(":");
			}
			buffer.append("]");
			buffer.append(Utils.NEW_LINE + Utils.FAILING_TESTS + Utils.NEW_LINE);

			stream.write(buffer.toString().getBytes());
			buffer.setLength(0);
			
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
				passingTests.append("T"+i++ + Utils.COMMA);
			}
			
			stream.write(buffer.toString().getBytes());
			buffer.setLength(0);

			// Iterate over all the entities (methods)
			Iterator<Entry<String,EntityTCM>> entityIterator = this.entities.entrySet().iterator();

			while(entityIterator.hasNext()) {
				Entry<String, EntityTCM> entityEntry = entityIterator.next();
				// Entity ID
				buffer.append(Utils.NEW_LINE + Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.NEW_LINE);
				buffer.append(Utils.METHOD_ID + Utils.COMMA + entityEntry.getKey());
				buffer.append(Utils.NEW_LINE + Utils.COMMA + Utils.FAILING_TESTS + commas.toString() + Utils.PASSING_TESTS + Utils.NEW_LINE);
				buffer.append(this.patternsDirectory.toUpperCase());
				buffer.append(Utils.COMMA);
				buffer.append(failingTests);
				buffer.append(passingTests);
				buffer.append(Utils.SPECTRUM);
				buffer.append(faultLocatorHeader);

				EntityTCM entity = entityEntry.getValue();
				Iterator<Entry<String,PatternTCM>> patternIterator = entity.getPatternsIterator();

				// Iterate over all the patterns
				while(patternIterator.hasNext()) {

					Entry<String, PatternTCM> patternEntry = patternIterator.next();
					// Pattern ID
					buffer.append(Utils.NEW_LINE + patternEntry.getKey());
					// Add the TCM of the pattern
					buffer.append(Utils.COMMA+ patternEntry.getValue().getTCM(this.faultLocators));
				}
				
				stream.write(buffer.toString().getBytes());
				buffer.setLength(0);
				
			} // while
		
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
////		buffer.append(getWastedEffort() + NEW_LINE);
//		// Replacing this to avoid double calculations
////		buffer.append(this.wastedEffort + NEW_LINE);
//		
//		buffer.append("FAULTY Entity ID" + Utils.COMMA + this.faultyMethodIDList.toString().replace(',', ':'));
//		ArrayList<EntityTCM> faultyEntities = new ArrayList<>();
//		for(String faultyEntity:this.faultyMethodIDList)
//			faultyEntities.add(this.entities.get(faultyEntity));
//		
//		buffer.append(Utils.NEW_LINE + "Number of " + this.patternsDirectory + Utils.COMMA);
//		buffer.append("[");
//		Iterator<EntityTCM> faultyEntitiesIterator = faultyEntities.iterator();
//		while(faultyEntitiesIterator.hasNext()) {
//			EntityTCM faultyEntity = faultyEntitiesIterator.next();
//			int numberOfSequences = 0;
//			if(faultyEntity != null)
//				numberOfSequences = faultyEntity.getNumberOfPatterns();
//			buffer.append(numberOfSequences);
//			if(faultyEntitiesIterator.hasNext())
//				buffer.append(":");
//		}
//		buffer.append("]");
//		buffer.append(Utils.NEW_LINE + Utils.FAILING_TESTS + Utils.NEW_LINE);
//		
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
//			passingTests.append("T"+i++ + Utils.COMMA);
//		}
//		
//		// Iterate over all the entities (methods)
//		Iterator<Entry<String,EntityTCM>> entityIterator = this.entities.entrySet().iterator();
//		
//		while(entityIterator.hasNext()) {
//			Entry<String, EntityTCM> entityEntry = entityIterator.next();
//			// Entity ID
//			buffer.append(Utils.NEW_LINE + Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.COMMA + Utils.NEW_LINE);
//			buffer.append(Utils.METHOD_ID + Utils.COMMA + entityEntry.getKey());
//			buffer.append(Utils.NEW_LINE + Utils.COMMA + Utils.FAILING_TESTS + commas.toString() + Utils.PASSING_TESTS + Utils.NEW_LINE);
//			buffer.append(this.patternsDirectory.toUpperCase());
//			buffer.append(Utils.COMMA);
//			buffer.append(failingTests);
//			buffer.append(passingTests);
//			buffer.append(Utils.SPECTRUM);
//			buffer.append(faultLocatorHeader);
//			
//			EntityTCM entity = entityEntry.getValue();
//			Iterator<Entry<String,PatternTCM>> patternIterator = entity.getPatternsIterator();
//			
//			// Iterate over all the patterns
//			while(patternIterator.hasNext()) {
//				
//				Entry<String, PatternTCM> patternEntry = patternIterator.next();
//				// Pattern ID
//				buffer.append(Utils.NEW_LINE + patternEntry.getKey());
//				// Add the TCM of the pattern
//				buffer.append(Utils.COMMA+ patternEntry.getValue().getTCM(this.faultLocators));
//			}
//			if(!rankingsDir.exists())
//				rankingsDir.mkdirs();
//			Utils.writeContents(rankingsDir.getAbsolutePath() + File.separatorChar  + this.ID + "_" + this.patternsDirectory + Utils.TCM, buffer, true);
//			buffer.setLength(0);
//		}
//		
//	}
	
	/**
	 * This method reads all the patterns files and builds a TCM of all the entities.
	 * @param testStatus whether the tests are passing or failing
	 */
	private void parsePatterns(boolean testStatus) {
		
		// If testStatus is true we are dealing with passing tests
//		if(testStatus)
//			logger.debug("Parsing Passing patterns");
//		else
//			logger.debug("Parsing Failing patterns");
		
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
			addMethods(testDir);
			
			// Get the patterns folder of a test folder
			File patternsFolder = new File(testDir.getPath() + File.separatorChar + patternsDirectory);
			File entities[] = patternsFolder.listFiles();

//			logger.debug(testIndex + ". " + testDir.getName());
			
			// Read all the files in patterns folder
			if(entities!=null)
			for(File patterns:entities) {
				if(!patterns.getName().endsWith(Utils.TXT_EXTENSION)) continue;
				String eID = patterns.getName();
				// remove file extension from method ID
				eID = eID.substring(0, eID.indexOf(Utils.TXT_EXTENSION));
//				logger.debug(eID);
				try {
					BufferedReader reader = new BufferedReader(new FileReader(patterns.getPath()));
					String line = null;
					while((line = reader.readLine()) !=null) {
						// Split the line into patternID - frequency - method IDs
						String[] patternTriplet = line.split(";");
//						logger.debug(" [" + patternTriplet[0] + "]");
						addEntityPatterns(eID,patternTriplet[0], testIndex, testStatus);
					}
					reader.close();
				} catch (FileNotFoundException e) {
//					logger.error(e, e);
				} catch (Exception e) {
//					logger.error(e, e);
					System.exit(0);
				}
				
			} // inner for
		} // outer for
			
	}
	
	/**
	 * Used to add methods executed in a test as a total set of methods executed in particular combination of the failing and passing tests.
	 * @param testDir
	 */
	private void addMethods(File testDir) {
		File tracesFile = new File(testDir.getPath() + File.separatorChar + Utils.TRACE_ID_FILE);
		if(!tracesFile.exists()) return;
		
//		logger.debug("Reading IDs from " + tracesFile.getAbsolutePath());
		String mIDs[] = Utils.readMethodIDs(tracesFile);
		if(mIDs !=null)
			try {
				for(String id:mIDs)
					totalEntities.add(Integer.parseInt(id));
			} catch (Exception e) {
				System.err.println(this.getClass().getName() + "addMethods(File) " + e.getStackTrace());
//				logger.error(e);
				System.exit(0);
			}
	}

	/**
	 * Stores the patterns into the entities (methods)
	 * @param eID entity ID
	 * @param pID pattern ID
	 * @param testIndex index to the tests array indicated by testStatus
	 * @param testStatus whether the test is failing or passing
	 */
    private void addEntityPatterns(String eID, String pID, int testIndex, boolean testStatus) {
    	EntityTCM entity = this.entities.get(eID);
		if(entity == null) {
			entity = new EntityTCM(eID, failingTests.length, passingTests.length);
			this.entities.put(eID, entity);
		}
		entity.addPattern(pID, testIndex, testStatus);
		
	}

	public String getWastedEffort() {
		return this.wastedEffort;
    }
	
//	private void calculateWastedEffort() {
//		this.wastedEffort = Utils.getWastedEffort(this.entities, this.totalEntities, this.faultyMethodIDList, this.faultLocators);
//    }

	public void initialise() {
//		logger.debug("Starting " + this.getClass().getSimpleName());
		parsePatterns();
		saveRanking();
//		calculateWastedEffort();
		if(printCompleteLog) {
			saveTCM();
		}
	}

	public String getID() {
		return this.ID;
	}

	public Integer getTotalExecutedEntities() {
		return this.totalEntities.size();
	}

	public String getFaultyEntityIDListString() {
		return this.faultyMethodIDList.toString().replace(',',':');
	}
	
}
