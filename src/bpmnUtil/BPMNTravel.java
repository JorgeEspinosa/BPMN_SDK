package bpmnUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bpmnElements.Diagram;
import bpmnElements.Event;
import bpmnElements.EventType;
import bpmnElements.Gateway;
import bpmnElements.GatewayClass;
import bpmnElements.GatewayType;
import bpmnElements.Node;
import bpmnElements.SubProcess;
import bpmnElements.Task;

public class BPMNTravel {
	private List<List<String>> routes;
	private List<List<String>> routesID;
	private List<Double> routesPBB;
	private List<Double> routesCost;
	
	public void findRoutes(Diagram diagram) {
		List<Node> andKeys = new ArrayList<>();
		List<Node> orKeys = new ArrayList<>();
		
		routes = new ArrayList<>();
		routesID = new ArrayList<>();
		routesPBB = new ArrayList<>();
		routesCost = new ArrayList<>();
		
		Event startEvent = (Event)diagram.getStartEvent();
		Map<String, Node> nodes = diagram.getNodes();
		Node current = startEvent;
		List<Node> nodeList = new ArrayList<>();
		nodeList.add(startEvent);
		
		List<Integer> currentWay = new ArrayList<>();
		List<Integer> currentAndWay = new ArrayList<>();
		List<Integer> currentOrWay = new ArrayList<>();
		List<Integer> currentInternOrWay = new ArrayList<>();
		List<Double> pbbs = new ArrayList<>();
		List<Double> pbbsOr = new ArrayList<>();
		List<String> currentList = new ArrayList<>();
		List<String> currentListID = new ArrayList<>();
		List<List<String>> orKeyRoutes = new ArrayList<>();
		double cost = ((Event)startEvent).getCost();
		
		while(!nodeList.isEmpty()) {
			//System.out.println(current.getLabel());
			if(Event.isEType(current, EventType.END)) {
				for(Node node:nodeList) {
					currentList.add(node.getLabel());
					currentListID.add(node.getId());
				}
				cost += ((Event)current).getCost();
				routes.add(currentList);
				routesID.add(currentListID);
				currentList = new ArrayList<>();
				currentListID = new ArrayList<>();
				double valuePbb = 1;
				for(Double pbb:pbbs) {
					valuePbb *= pbb;
				}
				for(Double pbb:pbbsOr) {
					valuePbb *= pbb;
				}
				routesPBB.add(valuePbb);
				routesCost.add(cost);
				while(true) {
					if(nodeList.isEmpty())
						break;
					Node node = nodeList.get(nodeList.size()-1);
					if(Gateway.isGClass(node, GatewayClass.FORK) && Gateway.isGType(node, GatewayType.OR)) {
						Gateway gNode = (Gateway)node;
						if(orKeyRoutes.get(orKeyRoutes.size()-1).size() != currentOrWay.get(currentOrWay.size()-1)) {
							current = nodes.get(orKeyRoutes.get(orKeyRoutes.size()-1).get(currentOrWay.get(currentOrWay.size()-1)).split(Tag.LINE)[0]);
							currentInternOrWay.add(1);
							nodeList.add(current);
							pbbsOr.set(pbbsOr.size()-1, gNode.getOutgoingPbb(orKeyRoutes.get(orKeyRoutes.size()-1).get(currentOrWay.get(currentOrWay.size()-1))));
							//currentOrWay.set(currentOrWay.size()-1, currentOrWay.get(currentOrWay.size()-1)+1);
							break;
						} else {
							nodeList.remove(nodeList.size()-1);
							pbbsOr.remove(pbbsOr.size()-1);
							currentOrWay.remove(currentOrWay.size()-1);
							continue;
						}
					} else if(Gateway.isGClass(node, GatewayClass.FORK) && Gateway.isGType(node, GatewayType.XOR)) {
						Gateway gNode = (Gateway)node;
						if(gNode.getOutgoingNodes().size() != currentWay.get(currentWay.size()-1)) {
							current = nodes.get(gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1)));
							nodeList.add(current);
							pbbs.set(pbbs.size()-1, gNode.getOutgoingPbb(gNode.getOutgoingNodes().get(currentWay.get(currentWay.size()-1))));
							currentWay.set(currentWay.size()-1, currentWay.get(currentWay.size()-1)+1);
							break;
						} else {
							nodeList.remove(nodeList.size()-1);
							pbbs.remove(pbbs.size()-1);
							currentWay.remove(currentWay.size()-1);
							continue;
						}
					} else if(Event.isEvent(node)) {
						nodeList.remove(nodeList.size()-1);
						cost -= ((Event)node).getCost();
						continue;
					} else if(SubProcess.isSubProcess(node)) {
						nodeList.remove(nodeList.size()-1);
						cost -= ((SubProcess)node).getCost();
						continue;
					} else if(Task.isTask(node)) {
						nodeList.remove(nodeList.size()-1);
						cost -= ((Task)node).getCost();
						continue;
					} else {
						nodeList.remove(nodeList.size()-1);
						continue;
					}
				}
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK) && Gateway.isGType(current, GatewayType.XOR)) {
				Gateway gNode = (Gateway)current;
				pbbs.add(gNode.getOutgoingPbb(gNode.getOutgoingNodes().get(0)));
				currentWay.add(1);
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.AND)) {
				Gateway gNode = (Gateway)current;
				andKeys.add(current);
				currentAndWay.add(1);
				current = nodes.get(gNode.getOutgoingNodes().get(0));
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.FORK)  && Gateway.isGType(current, GatewayType.OR)) {
				Gateway gNode = (Gateway)current;
				orKeys.add(current);
				Set<String> list = gNode.getOutgoingPbbs().keySet();
				List<String> keyList = new ArrayList<>();
				for(String key:list) {
					keyList.add(key);
				}
				orKeyRoutes.add(keyList);
				pbbsOr.add(gNode.getOutgoingPbb(orKeyRoutes.get(orKeyRoutes.size()-1).get(0)));
				currentOrWay.add(0);
				currentInternOrWay.add(1);
				int value = orKeyRoutes.get(orKeyRoutes.size()-1).get(0).indexOf(Tag.LINE);
				if(value != -1) {
					current = nodes.get(orKeyRoutes.get(orKeyRoutes.size()-1).get(0).split(Tag.LINE)[0]);
				} else {
					current = nodes.get(orKeyRoutes.get(orKeyRoutes.size()-1).get(0));
				}
				nodeList.add(current);
				continue;
			} else if(Event.isEType(current, EventType.INTERMEDIATE)){
				Event eNode = (Event)current;
				cost += eNode.getCost();
				current = nodes.get(eNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(SubProcess.isSubProcess(current)){
				SubProcess sNode = (SubProcess)current;
				cost += sNode.getCost();
				current = nodes.get(sNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(Task.isTask(current)){
				Task tNode = (Task)current;
				cost += tNode.getCost();
				current = nodes.get(tNode.getAfter());
				nodeList.add(current);
				continue;
			} else if(Gateway.isGClass(current, GatewayClass.JOIN)) {
				if(Gateway.isGType(current, GatewayType.OR)) {
					if(orKeyRoutes.get(orKeyRoutes.size()-1).size() != currentOrWay.get(currentOrWay.size()-1)) {
						if(orKeyRoutes.get(orKeyRoutes.size()-1).get(currentOrWay.get(currentOrWay.size()-1)).split(Tag.LINE).length != currentInternOrWay.get(currentInternOrWay.size()-1)) {
							nodeList.remove(nodeList.size()-1);
							current = nodes.get(orKeyRoutes.get(orKeyRoutes.size()-1).get(currentOrWay.get(currentOrWay.size()-1)).split(Tag.LINE)[currentInternOrWay.get(currentInternOrWay.size()-1)]);
							nodeList.add(current);
							currentInternOrWay.set(currentInternOrWay.size()-1, currentInternOrWay.get(currentInternOrWay.size()-1)+1);
						} else {
							currentOrWay.set(currentOrWay.size()-1, currentOrWay.get(currentOrWay.size()-1)+1);
							currentInternOrWay.remove(currentInternOrWay.size()-1);
							current = nodes.get(current.getAfter());
							nodeList.add(current);
						}
					} else {
						currentInternOrWay.remove(currentInternOrWay.size()-1);
						current = nodes.get(current.getAfter());
						nodeList.add(current);
					}
					continue;
				} else if(Gateway.isGType(current, GatewayType.AND)) {
					Node currentPro = andKeys.get(andKeys.size()-1);
					Gateway gNode = (Gateway)currentPro;
					if(gNode.getOutgoingNodes().size() != currentAndWay.get(currentAndWay.size()-1)) {
						nodeList.remove(nodeList.size()-1);
						current = nodes.get(gNode.getOutgoingNodes().get(currentAndWay.get(currentAndWay.size()-1)));
						nodeList.add(current);
						currentAndWay.set(currentAndWay.size()-1, currentAndWay.get(currentAndWay.size()-1)+1);
					} else {
						currentAndWay.remove(currentAndWay.size()-1);
						current = nodes.get(current.getAfter());
						nodeList.add(current);
					}
					continue;
				} else if(Gateway.isGType(current, GatewayType.XOR)) {
					current = nodes.get(current.getAfter());
					nodeList.add(current);
				}
				continue;
			} else if(Event.isEType(current, EventType.START)) {
				Event eNode = (Event)current;
				cost += eNode.getCost();
				current = nodes.get(current.getAfter());
				nodeList.add(current);
				continue;
			}
		}
	}

	public List<List<String>> getRoutes() {
		return routes;
	}

	public List<List<String>> getRoutesID() {
		return routesID;
	}
	
	public List<Double> getRoutesPbbs() {
		return routesPBB;
	}
	
	public List<Double> getRoutesCost() {
		return routesCost;
	}
	
	public double getTotalCost() {
		double cost = 0;
		for(int i = 0; i < routes.size() ; i++) {
			System.out.println("cost: " + routesCost.get(i));
			System.out.println("pbb: " + routesPBB.get(i));
			cost += routesCost.get(i)*routesPBB.get(i);
		}
		return cost;
	}
}
