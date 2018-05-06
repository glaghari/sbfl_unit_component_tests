package org.lore.rankings.faultlocators;

public class SimpleMatching implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (ef + np) / (ef + nf + ep + np);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
