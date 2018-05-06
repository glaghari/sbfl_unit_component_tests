package org.lore.rankings.faultlocators;

public class GP19 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		susp = ef * Math.sqrt(Math.abs(ep - ef + (ef + nf) - (ep + np)));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
