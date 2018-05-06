package extract;

import org.antlr.v4.runtime.ParserRuleContext;

public class LineNumber {
	
	public static int getStart(ParserRuleContext ctx) {
		return ctx.start.getLine();
	}
	
	public static int getStop(ParserRuleContext ctx) {
		return ctx.stop.getLine();
	}
}