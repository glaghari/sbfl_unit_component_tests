package org.lore.rankings.faultlocators;

public class GP02 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		susp = 2 * (ef + Math.sqrt(ep + np)) + Math.sqrt(ep);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
