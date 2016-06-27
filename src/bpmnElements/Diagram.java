package bpmnElements;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bpmnUtil.BPMNFactory;

public class Diagram {
	private Event startEvent;
	private Map<String, Node> nodes;
	private Map<String, SequenceFlow> flows;
	
	public Diagram() {
		nodes = new HashMap<String, Node>();
		flows = new HashMap<String, SequenceFlow>();
	}
	
	public Node getStartEvent() {
		return this.startEvent;
	}
	
	public Map<String, Node> getNodes() {
		return this.nodes;
	}
	
	public Map<String, SequenceFlow> getFlows() {
		return this.flows;
	}
	
	public Node getNode(String id) {
		return this.nodes.get(id);
	}
	
	public SequenceFlow getFlow(String id) {
		return this.flows.get(id);
	}
	
	public void setStartEvent(Event startEvent) {
		if(startEvent != null) {
			nodes.remove(startEvent.getId(), startEvent);
		}
		this.startEvent = startEvent;
		nodes.put("", startEvent);
	}
	
	public void setNodes(Map<String, Node> nodes) {
		this.nodes = nodes;
	}
	
	public boolean addNode(Node node) {
		if(node == null) 
			return false;
		if(Event.isEType(node, EventType.START)) {
			return false;
		}
		if(Gateway.isGClass(node, GatewayClass.JOIN)) {
			for(String id:((Gateway)node).getIncomingNodes()){
				nodes.put(id, node);
			}
			return true;
		}
		nodes.put(node.getBefore(), node);
		return true;
	}
	
	public boolean addFlow(SequenceFlow flow) {
		if(flow == null) 
			return false;
		flows.put(flow.getId(), flow);
		return true;
	}
	
	@Override
	public Diagram clone() {
		Diagram diagram = new Diagram();
		diagram.startEvent = this.startEvent.clone();
		diagram.nodes = new HashMap<>();
		for(String key:this.getNodes().keySet()) {
			diagram.nodes.put(key, this.getNode(key).clone());
		}
		diagram.flows = new HashMap<>();
		for(String key:this.getFlows().keySet()) {
			diagram.flows.put(key, this.getFlow(key).clone());
		}
		return diagram;
	}
}
