package shallowgreen.visualizer;

import shallowgreen.Game;
import shallowgreen.GameFactory;
import shallowgreen.visualizer.game.ServerUpdateVisualizer;

public class VisualizerGameFactory extends GameFactory {

	private GameFactory wrappedGameFactory;

	@SuppressWarnings("unused")
	private VisualizerGameFactory() { }

	public VisualizerGameFactory(GameFactory wrappedGameFactory) {
		this.wrappedGameFactory=wrappedGameFactory;
	}

	@Override
	public Game newGame() {
		return new ServerUpdateVisualizer(wrappedGameFactory.newGame());
	}

}
