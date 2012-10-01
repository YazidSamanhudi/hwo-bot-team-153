package shallowgreen.model;

public class MissileLaunch {

	private long receiveTimeMillis=System.currentTimeMillis();
	private String code;
	private Position pos;
	private Speed speed;
	private long launchTime;

	public long getReceiveTimeMillis() {
		return receiveTimeMillis;
	}

	public String getCode() {
		return code;
	}

	public Position getPos() {
		return pos;
	}

	public Speed getSpeed() {
		return speed;
	}

	public long getLaunchTime() {
		return launchTime;
	}

}

/*
{
	"msgType":"missileLaunched",
	"data":{
		"code":"iceman_133",
		"pos":{
			"x":10.0,
			"y":374.87842794516223
		},
		"speed":{
			"x":8.0,
			"y":0.0
		},
		"launchTime":1348853015606
	}
}
*/
