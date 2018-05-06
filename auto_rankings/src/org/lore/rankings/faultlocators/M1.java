package org.lore.rankings.faultlocators;

public class M1 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (ef + np) / (nf + ep);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
