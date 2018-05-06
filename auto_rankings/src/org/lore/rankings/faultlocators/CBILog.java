package org.lore.rankings.faultlocators;

import org.lore.util.Utils;

public class CBILog implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 2 / ( (1 / new CBIInc().getSuspiciousness(ef, ep, nf, np)) + (Math.log10(ef + nf) / Math.log10(ef)));
		if (Double.isNaN(susp))
			susp = Utils.getLowestValue(this);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}