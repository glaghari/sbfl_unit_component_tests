package org.lore.rankings.faultlocators;

public class Ample2 implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		double percFail = ef / (ef + nf);
		double percPass = ep/ (ep + np);
		susp = percFail - percPass;
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
