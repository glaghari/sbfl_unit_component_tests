package org.lore.rankings.faultlocators;

public class Kulczynski2 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 0.5 * ( (ef / (ef + nf)) + (ef / (ef + ep)) );
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
