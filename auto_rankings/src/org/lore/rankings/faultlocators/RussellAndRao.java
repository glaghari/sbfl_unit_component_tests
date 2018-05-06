package org.lore.rankings.faultlocators;

public class RussellAndRao implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef / (ef + nf + ep + np);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
