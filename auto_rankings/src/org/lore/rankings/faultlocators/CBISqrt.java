package org.lore.rankings.faultlocators;

public class CBISqrt implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 2 / ( (1 / new CBIInc().getSuspiciousness(ef, ep, nf, np)) + (Math.sqrt(ef + nf) / Math.sqrt(ef)));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
