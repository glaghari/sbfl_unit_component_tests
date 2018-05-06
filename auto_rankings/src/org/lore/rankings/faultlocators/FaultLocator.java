package org.lore.rankings.faultlocators;

public interface FaultLocator {
	public double getSuspiciousness(double ef, double ep, double nf, double np);
	public String getName();
}
