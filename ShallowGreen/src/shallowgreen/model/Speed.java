package shallowgreen.model;

public class Speed {

	private double dx;
	private double dy;

	public Speed() {
	}

	public Speed(double dx, double dy) {
		this.dx=dx;
		this.dy=dy;
	}

	public Speed(double x1, double y1, double x2, double y2) {
		dx=x2-x1;
		dy=y2-y1;
	}

	public double getDx() {
		return dx;
	}

	public double getDy() {
		return dy;
	}

}
