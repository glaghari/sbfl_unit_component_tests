package org.lore.rankings.faultlocators;

public class Sokal implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (2 * (ef + np)) / (2 * (ef + np) + nf + ep);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
