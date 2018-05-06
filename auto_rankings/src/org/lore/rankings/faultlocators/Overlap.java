package org.lore.rankings.faultlocators;

import org.lore.util.Utils;

public class Overlap implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef / Math.min(Math.min(ef, nf), ep);
		if (Double.isNaN(susp))
			susp = Utils.getLowestValue(this);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
