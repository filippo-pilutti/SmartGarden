package esiot.garden_service;

class DataPoint {
	private String name;
	private int value;
	private int range;
	
	public DataPoint(String name, int value, int range) {
		this.name = name;
		this.value = value;
		this.range = range;
	}
	
	public int getValue() {
		return value;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRange() {
		return range;
	}
}
