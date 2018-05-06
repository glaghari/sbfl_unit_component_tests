package org.lore.rankings.tcm.coverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.lore.rankings.faultlocators.FaultLocator;
import org.lore.rankings.tcm.coverage.EntityTCM;
import org.lore.util.Utils;

/**
 * This class holds a test coverage matrix (TCM) for an entity. An entity can be a Class or a Method
 * @author Gulsher Laghari
 * @year 2015
 * @version 1.0
 *
 */
public class ProgramTCM {
	
	/**
	 * This map holds all the sequences (patterns) of the entity. The String holds the IDs of patterns
	 */
	private Map<String, EntityTCM> entities;
	
	/**
	 * This is the ID of the entity
	 */
	private String ID;
	
	/**
	 * This indicates the ID of a faulty entity that needs to be localised.
	 */
	private List<String> faultyMethodIDList = null;
	
	/**
	 * This is the size of the FCM of the entities
	 */
	int sizeFCM;
	
	/**
	 * This is the size of the PCM of the entities
	 */
	int sizePCM;
	
	/**
	 * This is the set of all the entities executed. The String holds the IDs of entities.
	 */
	private Set<Integer> totalEntities = null;
	
	/**
	 * Fault locator functions to calculate the suspiciousness with
	 */
	private List<FaultLocator> faultLocators = null;
	
	/**
	 * The constructor to initialise the entity
	 * @param ID the ID of the entity
	 * @param size FCM this is the size of the FCM of the entities
	 * @param sizePCM this is the size of the PCM of the entities
	 * @param faultyEntityID2 
	 */
	public ProgramTCM(String ID, int sizeFCM, int sizePCM, List<String> faultyMethodIDList, List<FaultLocator> faultLocators) {
		this.ID = ID;
		this.sizeFCM = sizeFCM;
		this.sizePCM = sizePCM;
		this.entities = new HashMap<String, EntityTCM>();
		this.totalEntities = new HashSet<Integer>();
		this.faultyMethodIDList = faultyMethodIDList;
		this.faultLocators = faultLocators;
	}

	/**
	 * Store the entities and their respective FCM or PCM
	 * @param eID entity ID
	 * @param testIndex FCM / PCM index
	 * @param testStatus FCM / PCM selector
	 */
	public void addEntity(String eID, int testIndex, boolean testStatus) {
		totalEntities.add(Integer.parseInt(eID));
		EntityTCM entity = entities.get(eID);
		if(entity == null) {
			entity = new EntityTCM(eID, sizeFCM, sizePCM);
			entities.put(eID, entity);
		}
		
		// if testStatus is true we mark FCM of the entity for the test at index testIndex as 1
		if(testStatus)
			entity.addPCM(testIndex);
		// otherwise we mark FCM of the entity for the test at index testIndex as 1
		else
			entity.addFCM(testIndex);	
	}
	
	public String getTCM() {
		StringBuilder buffer = new StringBuilder();
		
		// Iterate over all the entities (methods)
		Iterator<Entry<String,EntityTCM>> entityIterator = this.entities.entrySet().iterator();
		while(entityIterator.hasNext()) {
			Entry<String, EntityTCM> entityEntry = entityIterator.next();
			// Entity ID
			buffer.append(Utils.NEW_LINE + entityEntry.getKey());
			// Add the TCM of the entity
			buffer.append(Utils.COMMA + entityEntry.getValue().getTCM(this.faultLocators));
		}
		return buffer.toString();
	}
	
	public String getRankings() {
		StringBuilder buffer = new StringBuilder();
		
		// Iterate over all the entities (methods)
		Iterator<Entry<String,EntityTCM>> entityIterator = this.entities.entrySet().iterator();
		while(entityIterator.hasNext()) {
			Entry<String, EntityTCM> entityEntry = entityIterator.next();
			EntityTCM entity = entityEntry.getValue();
			buffer.append(entity.getID() + Utils.COMMA + Utils.getSuspiciousness(entity, this.faultLocators));
			if(entityIterator.hasNext())
				buffer.append(Utils.NEW_LINE);
		}
		
		return buffer.toString();
	}
	
    
	public String getWastedEffort() {
		return Utils.getWastedEffort(this.entities, this.totalEntities, this.faultyMethodIDList, this.faultLocators);
    }
	
	public String getFaultyEntityIDListString() {
		return this.faultyMethodIDList.toString().replace(',', ':');
	}

	public int getTotalExecutedEntities() {
		return this.totalEntities.size();
	}


}

