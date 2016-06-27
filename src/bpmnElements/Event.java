package bpmnElements;

public class Event extends Node {
	private EventType eType;
	private double cost;
	
	public Event(String label, String id, EventType eType, double cost) {
		super(label, id);
		this.eType = eType;
		this.cost = cost;
	}
	
	public Event(String label, String id) {
		super(label, id);
	}
	
	public Event() {
		
	}
	
	public EventType getEType() {
		return this.eType;
	}
	
	public double getCost() {
		return this.cost;
	}

	@Override
	public String getBefore() {
		if(this.eType == EventType.START)
			return null;
		return before;
	}
	
	@Override
	public String getAfter() {
		if(this.eType == EventType.END)
			return null;
		return after;
	}
	
	public void setEType(EventType eType) {
		this.eType = eType;
	}
	
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public static boolean isEvent(Node node) {
		return node.getClass() == Event.class;
	}
	
	public static boolean isEType(Node node, EventType eType) {
		if(isEvent(node) && eType == ((Event)node).eType)
			return true;
		return false;
	}
	
	@Override
	public Event clone() {
		Event event = new Event();
		event.after = this.after;
		event.before = this.before;
		event.id = this.id;
		event.label = this.label;
		event.eType = this.eType;
		event.cost = this.cost;
		return event;
	}
}