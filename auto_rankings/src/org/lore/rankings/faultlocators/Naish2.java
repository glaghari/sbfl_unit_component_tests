package org.lore.rankings.faultlocators;

public class Naish2 implements Naish { // Also Op2

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		susp = ef - ep / (ep + np + 1);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
