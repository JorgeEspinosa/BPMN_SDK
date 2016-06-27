package bpmnUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bpmnElements.Diagram;
import bpmnElements.Event;
import bpmnElements.EventType;
import bpmnElements.Gateway;
import bpmnElements.GatewayClass;
import bpmnElements.GatewayType;
import bpmnElements.Node;
import bpmnElements.SubProcess;
import bpmnElements.Task;

public class BPMNSubProcessValues {
	public static void calculateSubProcessValue(Diagram diagram) {
		calculateRepetitions(diagram);
		calculateValues(diagram);
	}
	
	private static void calculateRepetitions(Diagram diagram) {
		Map<String, Node> nodes = diagram.getNodes();
		for(String key:nodes.keySet()) {
			Node node = nodes.get(key);
			if(SubProcess.isSubProcess(node)) {
				calculateRepetitionValue((SubProcess)node);
			}
		}
	}
	
	private static void calculateRepetitionValue(SubProcess subProcess) {
		boolean isNotRecursive = !isRecursive(subProcess);
		if(isNotRecursive) {
			subProcess.setRepetitions(0);
		}
		String label = subProcess.getLabel();
		Map<String, Node> nodes = subProcess.getGraph().getNodes();
		for(String key:nodes.keySet()) {
			Node node = nodes.get(key);
			if(SubProcess.isSubProcess(node) && !label.equals(node.getLabel())) {
				calculateRepetitionValue((SubProcess)node);
			}
		}
		double repetitions = 0;
		int limit = 10000000;
		for(int i = 0 ; i < limit ; i++) {
			Node current = subProcess.getGraph().getStartEvent();
			while(!Event.isEType(current, EventType.END)) {
				if(SubProcess.isSubProcess(current) && label.equals(current.getLabel())) {
					repetitions++;
					current = subProcess.getGraph().getStartEvent();
				} else if(Gateway.isGClass(current, GatewayClass.FORK)) {
					Gateway gNode = (Gateway)current;
					if(Gateway.isGType(current, GatewayType.XOR)) {
						double value = Math.random();
						double last = 0;
						for(String key:gNode.getOutgoingPbbs().keySet()) {
							double actual = gNode.getOutgoingPbb(key);
							if(value >= last && value < last+actual) {
								current = nodes.get(key);
								break;
							}
							last +=actual;
						}
					} else if(Gateway.isGType(current, GatewayType.AND)) {
						current = nodes.get(gNode.getOutgoingNodes().get(0));
						break;
					} else if(Gateway.isGType(current, GatewayType.OR)) {
						current = nodes.get(gNode.getOutgoingNodes().get(0));
						break;
					}
				} else {
					current = nodes.get(current.getAfter());
				}
			}
		}
		subProcess.setRepetitions(repetitions/limit);
		System.out.println(repetitions/limit);
	}
	
	private static boolean isRecursive(SubProcess subProcess) {
		String label = subProcess.getLabel();
		Diagram graph = subProcess.getGraph();
		Map<String, Node> nodes = graph.getNodes();
		for(String key: nodes.keySet()) {
			if(nodes.get(key).getLabel().equals(label) && SubProcess.isSubProcess(nodes.get(key))) {
				return true;
			}
		}
		return false;
	}
	
	private static void calculateValues(Diagram diagram) {
		Map<String, Node> nodes = diagram.getNodes();
		for(String key:nodes.keySet()) {
			Node node = nodes.get(key);
			if(SubProcess.isSubProcess(node)) {
				((SubProcess)node).setCost(calculateValue((SubProcess)node));
			}
		}
	}
	
	private static double calculateValue(SubProcess subProcess) {
		if(subProcess.getGraph().getStartEvent() == null) {
			subProcess.setCost(0);
			return 0.0;
		}
		String label = subProcess.getLabel();
		Map<String, Node> nodes = subProcess.getGraph().getNodes();
		for(String key:nodes.keySet()) {
			Node node = nodes.get(key);
			if(SubProcess.isSubProcess(node) && !label.equalsIgnoreCase(node.getLabel())) {
				((SubProcess)node).setCost(calculateValue((SubProcess)node));
			}
		}
		SubProcess subProcessNoRecursive = subProcess.clone();
		Diagram diagram = subProcessNoRecursive.getGraph();
		nodes = diagram.getNodes();
		for(String key:nodes.keySet()) {
			if(Event.isEType(nodes.get(key), EventType.START)) {
				((Event)nodes.get(key)).setCost(0);
			} else if(Event.isEType(nodes.get(key), EventType.END)) {
				((Event)nodes.get(key)).setCost(0);
			}
		}
		if(subProcess.getRepetitions() == 0) {
			BPMNTravel travel = new BPMNTravel();
			travel.findRoutes(diagram);
			subProcess.setCost(travel.getTotalCost());
			return subProcess.getCost();
		} else {
			modifyNoRecursiveSide(subProcessNoRecursive);
			
			diagram = subProcessNoRecursive.getGraph();
			nodes = diagram.getNodes();
			for(String key:nodes.keySet()) {
				if(Event.isEType(nodes.get(key), EventType.START)) {
					((Event)nodes.get(key)).setCost(0);
				} else if(Event.isEType(nodes.get(key), EventType.END)) {
					((Event)nodes.get(key)).setCost(0);
				}
			}

			BPMNTravel travel = new BPMNTravel();
			travel.findRoutes(diagram);
			double noRecursiveCost = travel.getTotalCost();
			
			SubProcess subProcessRecursive = subProcess.clone();
			
			modifyRecursiveSide(subProcessRecursive);
			
			Diagram diagramRecursive = subProcessRecursive.getGraph();
			Map<String, Node> nodesRecursive = diagramRecursive.getNodes();
			for(String key:nodesRecursive.keySet()) {
				if(Event.isEType(nodesRecursive.get(key), EventType.START)) {
					((Event)nodesRecursive.get(key)).setCost(0);
				} else if(Event.isEType(nodesRecursive.get(key), EventType.END)) {
					((Event)nodesRecursive.get(key)).setCost(0);
				}
			}

			travel = new BPMNTravel();
			travel.findRoutes(diagramRecursive);			
			double recursiveCost = travel.getTotalCost();

			System.out.println("No recursivo " + noRecursiveCost);
			System.out.println("Recursivo " + recursiveCost);
			System.out.println("Repeticiones " + subProcess.getRepetitions());
			System.out.println("Total " + (noRecursiveCost + recursiveCost*subProcess.getRepetitions())); 
			subProcess.setCost(noRecursiveCost + recursiveCost*subProcess.getRepetitions());
			return subProcess.getCost();
		}
	}
	
	private static void modifyNoRecursiveSide(SubProcess subProcess) {		
		Diagram diagram = subProcess.getGraph();
		
		Event startEvent = (Event)diagram.getStartEvent();
		Map<String, Node> nodes = diagram.getNodes();
		Node current = startEvent;
		List<Node> nodeList = new ArrayList<>();
		nodeList.add(startEvent);
		
		List<Integer> currentWay = new ArrayList<>();
		
		while(!nodeList.isEmpty()) {
			if(Event.isEType(current, EventType.END)) {
				while(true) {
					if(nodeList.isEmpty())
						break;
					Node node = nodeList.get(nodeList.size()-1);
					if(Gateway.isGClass(node, GatewayClass.FORK) && Gateway.isGType(node, GatewayType.XOR)) {
						Gateway gNode = (Gateway)node;
						if(gNode.getOutgoingNodes().size() != currentWay.get(currentWay.size()-1)) {
							current = nodes.get(gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)));
							nodeList.add(current);
							currentWay.set(currentWay.size()-1, currentWay.get(currentWay.size()-1)+1);
							break;
						} else {
							nodeList.remove(nodeList.size()-1);
							currentWay.remove(currentWay.size()-1);
							continue;
						}
					} else if(Event.isEvent(node)) {
						nodeList.remove(nodeList.size()-1);
						continue;
					} else if(SubProcess.isSubProcess(node)) {
						nodeList.remove(nodeList.size()-1);
						continue;
					} else if(Task.isTask(node)) {
						nodeList.remove(nodeList.size()-1);
						continue;
					} else {
						nodeList.remove(nodeList.size()-1);
						continue;
					}
				}
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.XOR)) {
				Gateway gNode = (Gateway)current;
				currentWay.add(1);
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.AND)) {
				Gateway gNode = (Gateway)current;
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.OR)) {
				Gateway gNode = (Gateway)current;
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Event.isEType(current, EventType.INTERMEDIATE)){
				Event eNode = (Event)current;
				current = nodes.get(eNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(SubProcess.isSubProcess(current)){
				boolean deleted = false;
				if(current.getLabel().equals(subProcess.getLabel())) {
					while(true) {
						if(nodeList.isEmpty())
							break;
						Node node = nodeList.get(nodeList.size()-1);
						if(Gateway.isGClass(node, GatewayClass.FORK) && Gateway.isGType(node, GatewayType.XOR)) {
							Gateway gNode = (Gateway)node;
							String currentKey = gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)-1);
							double pbb = gNode.getOutgoingPbb(currentKey);
							for(String key: gNode.getOutgoingPbbs().keySet()) {
								if(!deleted){
									if(key.equals(currentKey)) {
										gNode.getOutgoingPbbs().put(key, 0.0);
									} else {
										gNode.getOutgoingPbbs().put(key, gNode.getOutgoingPbb(key)/(1-pbb));
									}
								}
							}
							deleted = true;
							if(gNode.getOutgoingNodes().size() != currentWay.get(currentWay.size()-1)) {
								current = nodes.get(gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)));
								nodeList.add(current);
								currentWay.set(currentWay.size()-1, currentWay.get(currentWay.size()-1)+1);
								break;
							} else {
								nodeList.remove(nodeList.size()-1);
								currentWay.remove(currentWay.size()-1);
								continue;
							}
						} else if(Event.isEvent(node)) {
							nodeList.remove(nodeList.size()-1);
							continue;
						} else if(SubProcess.isSubProcess(node)) {
							nodeList.remove(nodeList.size()-1);
							continue;
						} else if(Task.isTask(node)) {
							nodeList.remove(nodeList.size()-1);
							continue;
						} else {
							nodeList.remove(nodeList.size()-1);
							continue;
						}
					}
					continue;
				} else {
					SubProcess sNode = (SubProcess)current;
					current = nodes.get(sNode.getAfter());
					nodeList.add(current);
					continue;
				}
			} else if(Task.isTask(current)){
				Task tNode = (Task)current;
				current = nodes.get(tNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.JOIN)) {
				current = nodes.get(current.getAfter());
				nodeList.add(current);
				continue;
			} else if(Event.isEType(current, EventType.START)) {
				current = nodes.get(current.getAfter());
				nodeList.add(current);
				continue;
			}
		}
	}
	
	private static void modifyRecursiveSide(SubProcess subProcess) {		
		Diagram diagram = subProcess.getGraph();
		
		Event startEvent = (Event)diagram.getStartEvent();
		Map<String, Node> nodes = diagram.getNodes();
		Node current = startEvent;
		List<Node> nodeList = new ArrayList<>();
		nodeList.add(startEvent);
		
		List<Integer> currentWay = new ArrayList<>();
		
		while(!nodeList.isEmpty()) {
			if(Event.isEType(current, EventType.END)) {
				boolean changed = false;
				while(true) {
					if(nodeList.isEmpty())
						break;
					Node node = nodeList.get(nodeList.size()-1);
					if(Gateway.isGClass(node, GatewayClass.FORK) && Gateway.isGType(node, GatewayType.XOR)) {
						Gateway gNode = (Gateway)node;
						String currentKey = gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)-1);
						double pbb = gNode.getOutgoingPbb(currentKey);
						if(!changed) {
							for(String key: gNode.getOutgoingPbbs().keySet()) {
								if(key.equals(currentKey)) {
									gNode.getOutgoingPbbs().put(key, 0.0);
								} else {
									changed = true;
									gNode.getOutgoingPbbs().put(key, gNode.getOutgoingPbb(key)/(1-pbb));
								}
							}
						}
						if(gNode.getOutgoingNodes().size() != currentWay.get(currentWay.size()-1)) {
							current = nodes.get(gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)));
							nodeList.add(current);
							currentWay.set(currentWay.size()-1, currentWay.get(currentWay.size()-1)+1);
							break;
						} else {
							nodeList.remove(nodeList.size()-1);
							currentWay.remove(currentWay.size()-1);
							continue;
						}
					} else if(Event.isEvent(node)) {
						nodeList.remove(nodeList.size()-1);
						continue;
					} else if(SubProcess.isSubProcess(node)) {
						nodeList.remove(nodeList.size()-1);
						continue;
					} else if(Task.isTask(node)) {
						nodeList.remove(nodeList.size()-1);
						continue;
					} else {
						nodeList.remove(nodeList.size()-1);
						continue;
					}
				}
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.XOR)) {
				Gateway gNode = (Gateway)current;
				currentWay.add(1);
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.AND)) {
				Gateway gNode = (Gateway)current;
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.OR)) {
				Gateway gNode = (Gateway)current;
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Event.isEType(current, EventType.INTERMEDIATE)){
				Event eNode = (Event)current;
				current = nodes.get(eNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(SubProcess.isSubProcess(current)){
				if(current.getLabel().equals(subProcess.getLabel())) {
					while(true) {
						if(nodeList.isEmpty())
							break;
						Node node = nodeList.get(nodeList.size()-1);
						if(Gateway.isGClass(node, GatewayClass.FORK) && Gateway.isGType(node, GatewayType.XOR)) {
							Gateway gNode = (Gateway)node;
							if(gNode.getOutgoingNodes().size() != currentWay.get(currentWay.size()-1)) {
								current = nodes.get(gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)));
								nodeList.add(current);
								currentWay.set(currentWay.size()-1, currentWay.get(currentWay.size()-1)+1);
								break;
							} else {
								nodeList.remove(nodeList.size()-1);
								currentWay.remove(currentWay.size()-1);
								continue;
							}
						} else if(Event.isEvent(node)) {
							nodeList.remove(nodeList.size()-1);
							continue;
						} else if(SubProcess.isSubProcess(node)) {
							nodeList.remove(nodeList.size()-1);
							continue;
						} else if(Task.isTask(node)) {
							nodeList.remove(nodeList.size()-1);
							continue;
						} else {
							nodeList.remove(nodeList.size()-1);
							continue;
						}
					}
					continue;
				} else {
					SubProcess sNode = (SubProcess)current;
					current = nodes.get(sNode.getAfter());
					nodeList.add(current);
					continue;
				}
			} else if(Task.isTask(current)){
				Task tNode = (Task)current;
				current = nodes.get(tNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.JOIN)) {
				current = nodes.get(current.getAfter());
				nodeList.add(current);
				continue;
			} else if(Event.isEType(current, EventType.START)) {
				current = nodes.get(current.getAfter());
				nodeList.add(current);
				continue;
			}
		}
		String recursive = "";
		for(String key: nodes.keySet()) {
			if(nodes.get(key).getLabel().equals(subProcess.getLabel())) {
				recursive = key;
			}
		}
		Event newEvent = new Event();
		SubProcess recursiveCall = (SubProcess)nodes.get(recursive);
		newEvent.setEType(EventType.END);
		newEvent.setCost(0);
		newEvent.setBefore(recursiveCall.getBefore());
		newEvent.setLabel(recursiveCall.getLabel());
		newEvent.setId(recursiveCall.getId());
		nodes.put(recursive, newEvent);
	}
}
