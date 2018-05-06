package org.lore.rankings.faultlocators;

public class Wong2 implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef - ep;
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
