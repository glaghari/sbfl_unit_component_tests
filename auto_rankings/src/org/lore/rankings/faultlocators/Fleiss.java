package org.lore.rankings.faultlocators;

public class Fleiss implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (4 * ef * np - 4 * nf * ep - ((nf - ep) * (nf - ep))) / ((2 * ef + nf + ep) + (2 * np + nf + ep));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
