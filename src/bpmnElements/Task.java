package bpmnElements;

public class Task extends Node {
	protected double cost;
	
	public Task(String label, String id, double cost) {
		super(label, id);
		this.cost = cost;
	}
	
	public Task(String label, String id) {
		super(label, id);
	}
	
	public Task() {
		
	}
	
	public double getCost() {
		return this.cost;
	}
	
	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public static boolean isTask(Node node) {
		return node.getClass() == Task.class;
	}
	
	@Override
	public Task clone() {
		Task task = new Task();
		task.after = this.after;
		task.before = this.before;
		task.id = this.id;
		task.label = this.label;
		task.cost = this.cost;
		return task;
	}
}