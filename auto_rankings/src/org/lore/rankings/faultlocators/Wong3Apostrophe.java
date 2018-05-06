package org.lore.rankings.faultlocators;

public class Wong3Apostrophe implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = (ep + ef) == 0 ? -1000d : new Wong3().getSuspiciousness(ef, ep, nf, np);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
