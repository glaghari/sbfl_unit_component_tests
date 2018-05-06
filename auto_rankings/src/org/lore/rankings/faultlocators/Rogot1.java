package org.lore.rankings.faultlocators;

public class Rogot1 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 0.5 * ( (ef / (2 * ef + nf + ep)) + (np / (2 * np + nf + ep)) );
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
