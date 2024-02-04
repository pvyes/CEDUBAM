package domain;

import java.util.ArrayList;
import java.util.List;

public class Strategies {
	private static List<Strategy> strategies = new ArrayList<Strategy>();
	
	private static void createStrategies(Diagnoser diagnoser) {
		strategies.add(new StrategyMeu(diagnoser));
		strategies.add(new StrategyCheapest(diagnoser));
		strategies.add(new StrategyRandom(diagnoser));
		strategies.add(new StrategyFixed(diagnoser));
		strategies.add(new StrategyGivenScenario(diagnoser));
	}

	public static List<Strategy> getStrategies(Diagnoser diagnoser) {
		strategies.clear();
		createStrategies(diagnoser);
		return strategies;
	}

	public static void addStrategy(Strategy strategy) {
		strategies.add(strategy);
	}
}
