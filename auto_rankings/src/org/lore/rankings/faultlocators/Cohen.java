package org.lore.rankings.faultlocators;

public class Cohen implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (2 * ef * np - 2 * nf * ep) / ((ef + ep) * (np + ep) + (ef + nf) * (nf + np));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
