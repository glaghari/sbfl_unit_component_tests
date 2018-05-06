package org.lore.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.faultlocators.Naish;
import org.lore.rankings.tcm.TCM;

public class Utils {
	// Common constants
	
	public static final String RANKINGS_FILE = "_rankings.csv";
	public static final String SEQ = "seq.";
	public static final String COV = "cov.";
	public static final String NEW_LINE = "\n";
	public static final String COMMA = ",";
	public static final String METHOD_ID = "Method_ID";
	public static final String RANKING_ID = "Rankings ID";
	public static final String PASSING_TESTS = "Passing tests";
	public static final String FAILING_TESTS = "Failing tests";
	public static final String EXEC_UUT = "Executed UUT";
//	public static final String METHOD_TRACES = "traces";
	public static final String METHOD_TRACES = "patterned_coverage";
	public static final String PATTERNS_DIR = "patterns";
//	public static final String RANKINGS = "_Rankings.csv";
	public static final String RANKINGS = "_RankedList.csv";
	public static final String TCM = "_TCM.csv";
//	public static final String TRACE_ID_FILE = "traceIDs.csv";
	public static final String TRACE_ID_FILE = "raw_coverage.csv";
	public static final String SPECTRUM = "ef,ep,nf,np,";
	public static final String TXT_EXTENSION = ".txt";
	public static final String FAULTY_METHODS_FILE = "faultyMethods.txt";
	public static final String DB_FILE = "DB.csv";
	public static final String PASSING_TESTS_FILE = "passing_tests.txt";
	public static final String FAILING_TESTS_FILE = "failing_tests.txt";
	public static final String SEQUENCES = "sequences";
	public static final String PATTERNS = "patterns";
	

	
	
	static DecimalFormat df = null;
	static double lowestValue = (double) (-1*Math.pow(10.0,38.0)); // This is the smallest double value
	static {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
		symbols.setInfinity("inf");
		df = new DecimalFormat("0.00000000000000000000", symbols);
	}
	public static double getFaultyEntitySuspiciousness(Map<String, ? extends TCM> entities, FaultLocator faultLocator, List<String> faultyMethodIDList) {
		double faultyEntitySuspiciousness = getLowestValue(faultLocator);
		ArrayList<TCM> faultyEntities = new ArrayList<>();
		for(String faultyEntity:faultyMethodIDList)
			faultyEntities.add(entities.get(faultyEntity));
		
		for(TCM faultyEntity:faultyEntities) {
			if(faultyEntity !=null) {
				double entitySuspiciousness = faultyEntity.getSuspiciousness(faultLocator);
				if(entitySuspiciousness > faultyEntitySuspiciousness)
					faultyEntitySuspiciousness = entitySuspiciousness;
			}
		}
		return faultyEntitySuspiciousness;
	}
	
    /**
     * Calculates and returns the wasted effort of the ranking.
     * wasted effort = m + (n+1) / 2
     * where m is the rank of the immediately preceding UUT and n is the number of UUTs with equal estimate
     * OR ( both are same)
     * wasted effort = |{susp(x) > susp(x∗)}|+|{susp(x) = susp(x∗)}|/2 + 1/2
     * where x ∈ S is any candidate method, x∗ is the actual faulty method,
     * and | · | calculates the size of a set. The value of the wasted effort is from 0 to |S| (both inclusive)
     * 
     * BUT we use following
     * wasted effort = m + n / 2

     * @return wasted effort
     */
	public static double getWastedEffort(Map<String, ? extends TCM> entities, Set<Integer> totalEntities, FaultLocator faultLocator, List<String> faultyMethodIDList) {
    	double m = 0; // For number of entities with higher suspiciousness
		double n = 01; // For number of entities with same suspiciousness
		double wastedEffort = 0;
		double entitySuspiciousness;
		double lowestValue = getLowestValue(faultLocator);
		double faultyEntitySuspiciousness = getFaultyEntitySuspiciousness(entities, faultLocator, faultyMethodIDList);
		
		Iterator<Integer> totalEntitesIterator = totalEntities.iterator();
		// Iterate over all executed entities
		while(totalEntitesIterator.hasNext()) {
			String entityID = totalEntitesIterator.next().toString();
			TCM entity = entities.get(entityID);
			// Don't compare faulty entities against themselves
			if(faultyMethodIDList.contains(entity))
				continue;
			
			if(entity != null)
				entitySuspiciousness = entity.getSuspiciousness(faultLocator);
			else
				entitySuspiciousness = lowestValue;

			if(entitySuspiciousness > faultyEntitySuspiciousness)
				m++;
			else if(entitySuspiciousness == faultyEntitySuspiciousness)
				n++;
		}
		
		wastedEffort = m + n/2;
		return wastedEffort;
	}
	
	public static double getWastedEffort(Map<String, ? extends TCM> entities, Set<Integer> totalEntities, FaultLocator faultLocator, double faultyEntitySuspiciousness) {
    	// Since the faulty entity compares with itself so this number must be 1 less.
    	// Therefore initialised to -1 NOT 0
    	double m = 0; // For number of entities with higher suspiciousness
		double n = -1; // For number of entities with same suspiciousness
		double wastedEffort = 0;
//		double faultyEntitySuspiciousness = getFaultyEntitySuspiciousness(faultLocator);
		double entitySuspiciousness;
		double lowestValue = getLowestValue(faultLocator);
		
		Iterator<Integer> totalEntitesIterator = totalEntities.iterator();
		// Iterate over all executed entities
		while(totalEntitesIterator.hasNext()) {
			String entityID = totalEntitesIterator.next().toString();
			TCM entity = entities.get(entityID);
			if(entity != null)
				entitySuspiciousness = entity.getSuspiciousness(faultLocator);
			else
				entitySuspiciousness = lowestValue;

			if(entitySuspiciousness > faultyEntitySuspiciousness)
				m++;
			else if(entitySuspiciousness == faultyEntitySuspiciousness)
				n++;
		}
		
		wastedEffort = m + n/2;
		return wastedEffort;
	}
	
	
	public static String getWastedEffort(Map<String, ? extends TCM> entities, Set<Integer> totalEntities, List<String> faultyMethodIDList, List<FaultLocator> faultLocators) {
		StringBuilder wastedEffortBuffer = new StringBuilder();
		Iterator<FaultLocator> fauIteratorIterator = faultLocators.iterator();
		while(fauIteratorIterator.hasNext()) {
			FaultLocator faultLocator = fauIteratorIterator.next();
//			double faultyEntitySuspiciousness = getFaultyEntitySuspiciousness(entities, faultLocator, faultyMethodIDList);
//			double wastedEffort = getWastedEffort(entities, totalEntities, faultLocator, faultyEntitySuspiciousness);
			double wastedEffort = getWastedEffort(entities, totalEntities, faultLocator, faultyMethodIDList);
			wastedEffortBuffer.append(wastedEffort);
			if(fauIteratorIterator.hasNext())
				wastedEffortBuffer.append(COMMA);
		}
		return wastedEffortBuffer.toString();
	}


	public static double getLowestValue(FaultLocator faultLocator) {
    	if(Naish.class.isAssignableFrom(faultLocator.getClass()))
			return lowestValue;
		return 0.0d;
	}
	
	/**
     * Save the bufer contents into a file
     * @param fileName the name of the file to be written
     * @param contents the contents in the file
     */
    
    public static void writeContents(String fileName, StringBuilder contents, boolean append) {
    	BufferedOutputStream stream = null;
		try {
			stream = new BufferedOutputStream(new FileOutputStream(fileName, append));
			byte[] traceBytes = contents.toString().getBytes();
			stream.write(traceBytes, 0, traceBytes.length);
		} catch (FileNotFoundException e) {
//			logger.error(e,e);
			System.err.println(getStackStrace(e));
		} catch (IOException e) {
//			logger.error(e,e);
			System.err.println(getStackStrace(e));
		}
		finally {try { stream.flush(); stream.close();} catch (IOException e) {}}
	}
    
    public static void remove_DS_Store(File projectFolder) {
		File ds_store = new File(projectFolder.getAbsolutePath() + File.separatorChar + ".DS_Store");
		if(ds_store.exists())
			ds_store.delete();
	}

	public static String getFaultLocatorHeader(List<FaultLocator> faultLocators) {
		return getFaultLocatorHeader(faultLocators, "");
	}
	
	public static String getFaultLocatorHeader(List<FaultLocator> faultLocators, String prefix) {
		StringBuilder faultLocatorHeader = new StringBuilder();
		Iterator<FaultLocator> faultLocatorsIterator = faultLocators.iterator();
		while(faultLocatorsIterator.hasNext()) {
			faultLocatorHeader.append(prefix + faultLocatorsIterator.next().getName());
			if(faultLocatorsIterator.hasNext())
				faultLocatorHeader.append(COMMA);
		}
		return faultLocatorHeader.toString();
	}

	public static String getZeroValues(List<FaultLocator> faultLocators) {
		StringBuilder zeroValues = new StringBuilder();
		String zero = getDecimalFormat(0d);
		for(FaultLocator faultLocator:faultLocators) {
			if(Naish.class.isAssignableFrom(faultLocator.getClass()))
				zeroValues.append(getDecimalFormat(getLowestValue(faultLocator)));
			else
				zeroValues.append(zero);
			zeroValues.append(COMMA);
		}
		int length = zeroValues.length();
		return zeroValues.toString().substring(0, length - 1);
	}
	
	public static String getDecimalFormat(double value) {
		return df.format(value);
	}

	public static String getSuspiciousness(TCM tcm, List<FaultLocator> faultLocators) {
		StringBuilder suspiciousness = new StringBuilder();
		Iterator<FaultLocator> faultLocatorsIterator = faultLocators.iterator();
		while(faultLocatorsIterator.hasNext()) {
			suspiciousness.append(getDecimalFormat(tcm.getSuspiciousness(faultLocatorsIterator.next())));
			if(faultLocatorsIterator.hasNext())
				suspiciousness.append(COMMA);
		}
		return suspiciousness.toString();	}

	public static String[] readMethodIDs(File coverage) {
		BufferedReader reader = null;
		String mIDs[] = null;
		try {
			reader = new BufferedReader(new FileReader(coverage.getPath()));
			String line = reader.readLine();
			if(line != null && line.length() > 0)
				mIDs = line.split(COMMA);
		} catch (Exception e) {
			System.err.println(coverage.getPath());
			System.err.println(getStackStrace(e));
			System.exit(0);
		}
		
		finally {
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					System.err.println(getStackStrace(e));
				}
		}
		
		return mIDs;
	}

	public static String getStackStrace(Exception exception) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		exception.printStackTrace(ps);
		String stackTrace = new String(baos.toByteArray());
		return stackTrace;
	}

}