package org.lore.rankings.faultlocators;

public class Barinel implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 0;
		susp = 1 - ep / (ep + ef);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
