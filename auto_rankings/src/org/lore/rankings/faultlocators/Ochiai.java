package org.lore.rankings.faultlocators;

public class Ochiai implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef / Math.sqrt( (ef + nf) * (ef + ep) );
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
