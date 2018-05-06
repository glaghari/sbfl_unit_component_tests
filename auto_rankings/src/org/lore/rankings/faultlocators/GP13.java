package org.lore.rankings.faultlocators;

public class GP13 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		susp = ef * (1 + 1 / (2 * ep + ef));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
