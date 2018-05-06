package org.lore.rankings.faultlocators;

public class Wong3 implements Naish {

	@Override
	public double getSuspiciousness(double ef, double ep, double nf, double np) {
		double h = 0;
		if(ep <= 2)
			h = ep;
		else if(2 < ep && ep <= 10)
			h = 2 + 0.1 * (ep - 2);
		else if(ep > 10)
			h = 2.8 + 0.001 * (ep - 10);
		
		double susp = ef - h;
		return susp;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

}
