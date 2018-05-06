package org.lore.rankings.faultlocators;

public class SorensenDice implements FaultLocator {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp;
		double twoTimesEF = 2 * ef;
		susp  = twoTimesEF / (twoTimesEF + nf + ep);
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
//		return "Sørensen-Dice";
	}

}
