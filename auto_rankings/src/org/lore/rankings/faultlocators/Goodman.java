package org.lore.rankings.faultlocators;

public class Goodman implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (2 * ef - nf - ep) / (2 * ef + nf + ep);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
