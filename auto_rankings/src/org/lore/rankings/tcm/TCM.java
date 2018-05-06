package org.lore.rankings.tcm;

import org.lore.rankings.faultlocators.FaultLocator;

public interface TCM {
	public double getSuspiciousness(FaultLocator faultLocator);
}
