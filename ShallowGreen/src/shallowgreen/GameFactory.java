package shallowgreen;

import shallowgreen.game.BallGame;

public class GameFactory {

	public Game newGame() {
		// FIXME: select proper game here
//		return new DogGame();
//		return new PetGame();
		return new BallGame();
	}

}
