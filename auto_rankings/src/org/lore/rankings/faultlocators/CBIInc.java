package org.lore.rankings.faultlocators;

public class CBIInc implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ((ef / (ef + ep) ) - ((ef + nf) / (ef + nf + np + ep)));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
