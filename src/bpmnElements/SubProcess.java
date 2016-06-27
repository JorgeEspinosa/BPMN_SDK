package bpmnElements;

public class SubProcess extends Task {
	private Diagram graph;
	private double repetitions;

	public SubProcess(String label, String id, double cost) {
		super(label, id, cost);
		this.graph = new Diagram();
	}

	public SubProcess(String label, String id) {
		super(label, id);
		this.graph = new Diagram();
	}
	
	public SubProcess() {
		this.graph = new Diagram();
	}
	
	public Diagram getGraph() {
		return this.graph;
	}
	
	public double getRepetitions() {
		return this.repetitions;
	}
	
	public void setGraph(Diagram graph) {
		this.graph = graph;
	}
	
	public void setRepetitions(double repetitions) {
		this.repetitions = repetitions;
	}
	
	public static boolean isSubProcess(Node node) {
		return node.getClass() == SubProcess.class;
	}
	
	@Override
	public SubProcess clone() {
		SubProcess subProcess = new SubProcess();
		subProcess.after = this.after;
		subProcess.before = this.before;
		subProcess.id = this.id;
		subProcess.label = this.label;
		subProcess.cost = this.cost;
		subProcess.repetitions = this.repetitions;
		if(graph.getStartEvent() == null) {
			subProcess.graph = null;
		} else {
			subProcess.graph = this.graph.clone();
		}
		return subProcess;
	}
}
