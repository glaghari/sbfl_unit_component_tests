package org.lore.rankings.faultlocators;

public class Binary implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = nf > 0 ? 0 : 1;
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
