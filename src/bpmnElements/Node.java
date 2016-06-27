package bpmnElements;

public class Node {
	protected String label;
	protected String id;
	protected String before;
	protected String after;
	
	public Node(String label, String id) {
		this.label = label;
		this.id = id;
		this.before = null;
		this.after = null;
	}
	
	public Node(){
		this.before = null;
		this.after = null;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getBefore() {
		return this.before;
	}
	
	public String getAfter() {
		return this.after;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setBefore(String before) {
		this.before = before;
	}
	
	public void setAfter(String after) {
		this.after = after;
	}
	
	@Override
	public Node clone() {
		Node node = new Node();
		node.after = this.after;
		node.before = this.before;
		node.id = this.id;
		node.label = this.label;
		return node;
	}
}
