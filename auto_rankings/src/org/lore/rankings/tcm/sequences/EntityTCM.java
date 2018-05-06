package org.lore.rankings.tcm.sequences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.tcm.TCM;
import org.lore.util.Utils;

/**
 * This class holds a test coverage matrix (TCM) for an entity. An entity can be a Class or a Method
 * @author Gulsher Laghari
 * @year 2015
 * @version 1.0
 *
 */
public class EntityTCM implements TCM {
	
	/**
	 * This map holds all the sequences (patterns) of the entity. The String holds the IDs of patterns
	 */
	private Map<String, PatternTCM> patterns;
	
	/**
	 * This is the ID of the entity
	 */
	private String ID;
	
	/**
	 * This is the size of the FCM of the patterns in the entity
	 */
	int sizeFCM;
	
	/**
	 * This is the size of the PCM of the patterns in the entity
	 */
	int sizePCM;

	/**
	 * The constructor to initialise the entity
	 * @param ID the ID of the entity
	 * @param size FCM this is the size of the FCM of the patterns in the entity
	 * @param sizePCM this is the size of the PCM of the patterns in the entity
	 */
	public EntityTCM(String ID, int sizeFCM, int sizePCM) {
		this.ID = ID;
		this.sizeFCM = sizeFCM;
		this.sizePCM = sizePCM;
		this.patterns = new HashMap<String, PatternTCM>();
	}

	/**
	 * Store the patterns and their respective FCM or PCM
	 * @param pID pattern ID
	 * @param testIndex FCM / PCM index
	 * @param testStatus FCM / PCM selector
	 */
	public void addPattern(String pID, int testIndex, boolean testStatus) {
		PatternTCM pattern = patterns.get(pID);
		if(pattern == null) {
			pattern = new PatternTCM(pID, sizeFCM, sizePCM);
			patterns.put(pID, pattern);
		}
		
		// if testStatus is true we mark FCM of the pattern for the test at index testIndex as 1
		if(testStatus)
			pattern.addPCM(testIndex);
		// otherwise we mark FCM of the pattern for the test at index testIndex as 1
		else
			pattern.addFCM(testIndex);	
	}
	
	/**
	 * Returns the iterator over the map for all patterns of this entity
	 * @return the iterator over map of pattern TCM
	 */
	public Iterator<Entry<String, PatternTCM>> getPatternsIterator() {
		return patterns.entrySet().iterator();
	}
	
	/**
	 * Returns the number of sequences in this entity (method)
	 * @return the number of patterns in this entity
	 */
	public int getNumberOfPatterns() {
		return patterns.size();
	}
	
	/**
	 * This method calculates the suspiciousness of the entity.
	 * The suspiciousness of the entity is the suspiciousness of its pattern with highest suspiciousness. 
	 * @param faultLocator The fault locator function to calculate the suspiciousness
	 * @return The suspiciousness of the entity
	 */
	public double getSuspiciousness(FaultLocator faultLocator) {
		double suspiciousnessMax = Utils.getLowestValue(faultLocator);
		Iterator<Entry<String,PatternTCM>> patternIterator = patterns.entrySet().iterator();
		// Iterate over all the patterns
		while(patternIterator.hasNext()) {
			Entry<String, PatternTCM> patternEntry = patternIterator.next();
			double patternSuspiciousness = patternEntry.getValue().getSuspiciousness(faultLocator);
			if(patternSuspiciousness > suspiciousnessMax) {
				suspiciousnessMax = patternSuspiciousness;
			}
		}		
		return suspiciousnessMax;
	}
	
	
//	public double getSuspiciousness(FaultLocator faultLocator) {
//		double suspiciousnessMax = Utils.getLowestValue(faultLocator);
//		SummaryStatistics stats = new SummaryStatistics();
//		Iterator<Entry<String,PatternTCM>> patternIterator = patterns.entrySet().iterator();
//		// Iterate over all the patterns
//		while(patternIterator.hasNext()) {
//			Entry<String, PatternTCM> patternEntry = patternIterator.next();
//			double patternSuspiciousness = patternEntry.getValue().getSuspiciousness(faultLocator);
//			stats.addValue(patternSuspiciousness);
//			if(patternSuspiciousness > suspiciousnessMax) {
//				suspiciousnessMax = patternSuspiciousness;
//			}
//		}
//		suspiciousnessMax = stats.getMax();
//		double std = stats.getStandardDeviation();
//		return suspiciousnessMax * (std + 1);
//	}
	
	
	public String getID() {
		return this.ID;
	}

}