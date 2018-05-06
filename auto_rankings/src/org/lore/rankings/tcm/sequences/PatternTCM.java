package org.lore.rankings.tcm.sequences;
import java.util.List;

import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.tcm.TCM;
import org.lore.util.Utils;

/**
 * This class holds a test coverage matrix (TCM) for a pattern of an entity
 * @author Gulsher Laghari
 * @year 2015
 * @version 1.0
 *
 */
public class PatternTCM implements TCM{
	
	/**
	 * This field holds the failed test coverage matrix (FCM) for a pattern
	 */
	private int FCM[];
	
	/**
	 * This field holds the passed test coverage matrix (PCM) for a pattern
	 */
	private int PCM[];
	
	/**
	 * This field holds the id of a pattern
	 */
	private String ID;
	
	/**
	 * This field indicates the number failing tests involving this pattern
	 */
	private double ef=0;
	
	/**
	 * This field indicates the number passing tests involving this pattern
	 */
	private double ep=0;
		
	/**
	 * Constructor to initialize the pattern tcm
	 * @param ID the ID of the pattern
	 * @param sizeFCM the number of failing tests
	 * @param sizePCM the number of passing tests
	 */
	public PatternTCM(String ID, int sizeFCM, int sizePCM) {
		this.ID = ID;
		this.FCM = new int[sizeFCM];
		this.PCM = new int[sizePCM];
		for(int i=0;i<sizeFCM;i++)
			this.FCM[i] = 0;
		for(int i=0;i<sizePCM;i++)
			this.PCM[i] = 0;
	}

	/**
	 * Marks the corresponding test in PCM as 1. It indicates that the pattern was found in the test
	 * @param testIndex
	 */
	public void addPCM(int testIndex) {
		this.PCM[testIndex] = 1;
		ep++;
	}

	/**
	 * Marks the corresponding test in FCM as 1. It indicates that the pattern was found in the test
	 * @param testIndex
	 */
	public void addFCM(int testIndex) {
		this.FCM[testIndex] = 1;
		ef++;
	}
	
	/**
	 * Returns the TCM of this pattern first FCM followed by PCM
	 */
	public String getTCM(List<FaultLocator> faultLocators) {
		StringBuffer buffer = new StringBuffer();
		for(int i:this.FCM)
			buffer.append(i + Utils.COMMA);
		
		for(int i:this.PCM)
			buffer.append(i + Utils.COMMA);
		buffer.append((int) ef + Utils.COMMA + (int) ep + Utils.COMMA);
		double nf = this.FCM.length - ef;
		double np = this.PCM.length - ep;
		buffer.append((int) nf + Utils.COMMA + (int) np + Utils.COMMA);
		buffer.append(Utils.getSuspiciousness(this, faultLocators));
		return buffer.toString();
	}
	
	/**
	 * This method returns the suspiciousness of this pattern calculated with fault locator function passed 
	 * @return pattern suspiciousness
	 */
	public double getSuspiciousness(FaultLocator faultLocator) {
		double nf = this.FCM.length - ef;
		double np = this.PCM.length - ep;
		return faultLocator.getSuspiciousness(this.ef, this.ep, nf, np);
	}

}
