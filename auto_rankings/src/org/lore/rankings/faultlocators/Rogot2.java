package org.lore.rankings.faultlocators;

import org.lore.util.Utils;

public class Rogot2 implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 0.25 * ( (ef / (ef + ep)) + (ef / (ef + nf)) + (np / (np + ep)) + (np / (np + nf)) );
		if (Double.isNaN(susp))
			susp = Utils.getLowestValue(this);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
