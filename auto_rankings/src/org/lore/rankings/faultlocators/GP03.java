package org.lore.rankings.faultlocators;

public class GP03 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		susp = Math.sqrt(Math.abs(ef * ef - Math.sqrt(ep)));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
