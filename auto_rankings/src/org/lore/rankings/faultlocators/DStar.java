package org.lore.rankings.faultlocators;

public class DStar implements FaultLocator {

	/***
	 * Definition adopted from Rue Abrue and Rene Just
	 * if passed + totalfailed - failed == 0:
	 * 		assert passed==0 and failed==totalfailed
	 * 		return totalfailed**2 + 1 
	 * return failed**2 / (passed + totalfailed - failed)
	 */
	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double susp = 0;
		if( (ep + nf) == 0 ) {
			assert ep == 0 && ef == (ef + nf) ;
			susp = ((ef + nf) * (ef + nf) + 1);
		}
		else
			susp = (ef * ef) / (ep + nf);

		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
