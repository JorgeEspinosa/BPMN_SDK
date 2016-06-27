package bpmnElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gateway extends Node{
	private GatewayType gType;
	private GatewayClass gClass;
	private Map<String, Double> outgoingPbbs;
	private List<String> outgoingNodes; 
	private List<String> incomingNodes; 
	
	public Gateway(String label, String id, GatewayType gType, GatewayClass gClass) {
		super(label, id);
		this.gType = gType;
		this.gClass = gClass;
		createLists();
	}
	
	public Gateway(String label, String id) {
		super(label, id);
		createLists();
	}
	
	public Gateway(){
		createLists();
	}
	
	public GatewayType getGType() {
		return this.gType;
	}
	
	public GatewayClass getGClass() {
		return this.gClass;
	}

	public List<String> getIncomingNodes() {
		return this.incomingNodes;
	}
	
	public List<String> getOutgoingNodes() {
		return this.outgoingNodes;
	}

	public Map<String, Double> getOutgoingPbbs() {
		return this.outgoingPbbs;
	}
	
	public double getOutgoingPbb(String key) {
		return this.outgoingPbbs.get(key);
	}
	
	@Override
	public String getBefore() {
		if(gClass == GatewayClass.FORK) 
			return this.before;
		else
			return null;
	}
	
	@Override
	public String getAfter() {
		if(gClass == GatewayClass.JOIN) 
			return this.after;
		else
			return null;
	}
	
	public void setGType(GatewayType gType) {
		this.gType = gType;
	}
	
	public void setGClass(GatewayClass gClass) {
		this.gClass = gClass;
	}
	
	public void setOutgoingPbb(Map<String, Double> outgoingPbbs) {
		this.outgoingPbbs = outgoingPbbs;
	}
	
	public boolean addOutgoingWay(String destiny) {
		if(gClass != GatewayClass.FORK)
			return false;
		this.outgoingNodes.add(destiny);
		return true;
	}

	public boolean addIncomingWay(String from) {
		if(gClass != GatewayClass.JOIN)
			return false;
		this.incomingNodes.add(from);
		return true;
	}

	public static boolean isGateway(Node node) {
		return node.getClass() == Gateway.class;
	}
	
	public static boolean isGType(Node node, GatewayType gType) {
		if(isGateway(node) && gType == ((Gateway)node).gType)
			return true;
		return false;
	}
	
	public static boolean isGClass(Node node, GatewayClass gClass) {
		if(isGateway(node) && gClass == ((Gateway)node).gClass)
			return true;
		return false;
	}
	
	@Override
	public Gateway clone() {
		Gateway gateway = new Gateway();
		gateway.after = this.after;
		gateway.before = this.before;
		gateway.id = this.id;
		gateway.label = this.label;
		gateway.gClass = this.gClass;
		gateway.gType = this.gType;
		gateway.incomingNodes = new ArrayList<>();
		gateway.incomingNodes.addAll(this.getIncomingNodes());
		gateway.outgoingNodes = new ArrayList<>();
		gateway.outgoingNodes.addAll(this.getOutgoingNodes());
		gateway.outgoingPbbs = new HashMap<>();
		for(String key:this.getOutgoingPbbs().keySet()) {
			gateway.outgoingPbbs.put(key, this.getOutgoingPbb(key));
		}
		return gateway;
	}
	
	private void createLists() {
		incomingNodes = new ArrayList<>();
		outgoingNodes = new ArrayList<>();
		outgoingPbbs = new HashMap<>();
	}
}
