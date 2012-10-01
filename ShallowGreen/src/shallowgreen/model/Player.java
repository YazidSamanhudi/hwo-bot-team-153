package shallowgreen.model;

public class Player {

	private double y;
	private String playerName;

	public Player() {
	}

	public Player(double y, String playerName) {
		this.y=y;
		this.playerName=playerName;
	}

	public double getY() {
		return y;
	}

	public String getPlayerName() {
		return playerName;
	}

}
