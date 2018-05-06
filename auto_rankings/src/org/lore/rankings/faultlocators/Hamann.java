package org.lore.rankings.faultlocators;

public class Hamann implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (ef + np - nf - ep) / (ef + nf + ep + np);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
