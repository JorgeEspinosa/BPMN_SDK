package bpmnElements;

public class SequenceFlow {
	private String label;
	private String id;
	
	public SequenceFlow(String label, String id) {
		this.label = label;
		this.id = id;
	}
	
	public SequenceFlow(){
		
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public SequenceFlow clone() {
		SequenceFlow sequenceFlow = new SequenceFlow();
		sequenceFlow.id = this.id;
		sequenceFlow.label = this.label;
		return sequenceFlow;
	}
}
