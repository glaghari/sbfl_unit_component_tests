package org.lore.rankings.faultlocators;

public class Zoltar implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = ef / ( (ef + nf + ep) + (10000 * nf * ep / ef));
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
