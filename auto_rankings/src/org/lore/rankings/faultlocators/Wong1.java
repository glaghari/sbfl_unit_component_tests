package org.lore.rankings.faultlocators;

public class Wong1 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef;
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
