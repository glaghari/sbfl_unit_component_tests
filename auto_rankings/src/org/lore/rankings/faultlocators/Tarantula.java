package org.lore.rankings.faultlocators;

public class Tarantula implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 0;
		double percFail = ef / (ef + nf);
		double percPass = ep / (ep + np);
		susp = percFail / (percFail + percPass);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
