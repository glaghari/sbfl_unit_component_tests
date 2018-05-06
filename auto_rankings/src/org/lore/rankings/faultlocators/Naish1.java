package org.lore.rankings.faultlocators;

public class Naish1 implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		return ef > 0 ? -1 : np;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
