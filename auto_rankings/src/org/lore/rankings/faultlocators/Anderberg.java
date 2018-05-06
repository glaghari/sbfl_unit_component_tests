package org.lore.rankings.faultlocators;

public class Anderberg implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef / (ef + 2 * (nf + ep));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
