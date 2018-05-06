package org.lore.rankings.tcm.coverage;


import java.util.List;

import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.tcm.TCM;
import org.lore.util.Utils;

/**
 * This class holds a test coverage matrix (TCM) for an entity
 * @author Gulsher Laghari
 * @year 2015
 * @version 1.0
 *
 */
public class EntityTCM implements TCM {
	
	/**
	 * This field holds the failed test coverage matrix (FCM) for the entity
	 */
	private int FCM[];
	
	/**
	 * This field holds the passed test coverage matrix (PCM) for the entity
	 */
	private int PCM[];
	
	/**
	 * This field holds the id of the entity
	 */
	private String ID;
	
	/**
	 * This field indicates the number failing tests involving this entity
	 */
	private double ef=0;
	
	/**
	 * This field indicates the number passing tests involving this entity
	 */
	private double ep=0;
	
		
	/**
	 * Constructor to initialize the entity tcm
	 * @param ID the ID of the entity
	 * @param sizeFCM the number of failing tests
	 * @param sizePCM the number of passing tests
	 */
	public EntityTCM(String ID, int sizeFCM, int sizePCM) {
		this.ID = ID;
		this.FCM = new int[sizeFCM];
		this.PCM = new int[sizePCM];
		for(int i=0;i<sizeFCM;i++)
			this.FCM[i] = 0;
		for(int i=0;i<sizePCM;i++)
			this.PCM[i] = 0;
	}

	/**
	 * Marks the corresponding test in PCM as 1. It indicates that the entity was found in the test
	 * @param testIndex
	 */
	public void addPCM(int testIndex) {
		this.PCM[testIndex] = 1;
		ep++;
	}

	/**
	 * Marks the corresponding test in FCM as 1. It indicates that the entity was found in the test
	 * @param testIndex
	 */
	public void addFCM(int testIndex) {
		this.FCM[testIndex] = 1;
		ef++;
	}
	
	/**
	 * Returns the TCM of this entity, first FCM followed by PCM
	 */

	public String getTCM(List<FaultLocator> faultLocators) {
		StringBuilder buffer = new StringBuilder();
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
	
	public String getID() {
		return this.ID;
	}

	@Override
	public double getSuspiciousness(FaultLocator faultLocator) {
		double nf = this.FCM.length - ef;
		double np = this.PCM.length - ep;
		return faultLocator.getSuspiciousness(this.ef, this.ep, nf, np);
	}

}