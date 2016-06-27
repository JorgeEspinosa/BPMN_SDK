package bpmnUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import bpmnElements.Diagram;
import bpmnElements.Event;
import bpmnElements.EventType;
import bpmnElements.Gateway;
import bpmnElements.GatewayClass;
import bpmnElements.GatewayType;
import bpmnElements.Node;
import bpmnElements.SubProcess;
import bpmnElements.Task;

public class BPMNNormalizer {	
	public static Diagram diagramNormalization(Diagram toNormalize) {
		Diagram normalized = toNormalize.clone();
		normalized = addIntermediateEvents(normalized);
		return normalized;
	}
	
	private static Diagram addIntermediateEvents(Diagram diagram) {
		diagram = gForkToTask(diagram);
		return gJoinToTask(diagram);
	}
	
	private static Diagram gForkToTask(Diagram diagram) {
		ArrayList<Event> addedEvents = new ArrayList<>();
		for(String key:diagram.getNodes().keySet()) {
			if(Gateway.isGClass(diagram.getNode(key),GatewayClass.FORK)) {
				Gateway gNode = (Gateway)diagram.getNode(key);
				for(String next:gNode.getOutgoingNodes()) {
					if(Task.isTask(diagram.getNode(next)) || SubProcess.isSubProcess(diagram.getNode(next))) {
						UUID newID = UUID.randomUUID();
						Event newEvent = new Event(newID.toString(), newID.toString(), EventType.INTERMEDIATE, 0);
						newEvent.setAfter(next);
						newEvent.setBefore(diagram.getNode(key).getBefore());
						addedEvents.add(newEvent);
					}
				}
			}
		}
		for(Event event:addedEvents) {
			UUID newAfter = UUID.randomUUID();
			Task tNode = (Task)diagram.getNode(event.getAfter());
			String oldAfter = event.getAfter();
			event.setAfter(newAfter.toString());
			tNode.setBefore(newAfter.toString());
			diagram.getNodes().put(oldAfter, event);
			diagram.addNode(tNode);
		}
		return diagram;
	}
	
	private static Diagram gJoinToTask(Diagram diagram) {
		ArrayList<Event> addedEvents = new ArrayList<>();
		for(String key:diagram.getNodes().keySet()) {
			if(Gateway.isGClass(diagram.getNode(key), GatewayClass.FORK)) {
				Gateway currentGNode = (Gateway)diagram.getNode(key);
				List<String> waysOut = currentGNode.getOutgoingNodes();
				for(String last:waysOut) {
					if(Gateway.isGClass(diagram.getNode(last),GatewayClass.JOIN)) {
						if(!Event.isEvent(diagram.getNode(key))) {
							UUID newID = UUID.randomUUID();
							Event newEvent = new Event(newID.toString(), newID.toString(), EventType.INTERMEDIATE, 0);
							newEvent.setAfter(last);
							newEvent.setBefore(last);
							addedEvents.add(newEvent);
						}
					}
				}
			}
			else if(!Event.isEType(diagram.getNode(key), EventType.END) && Gateway.isGClass(diagram.getNode(diagram.getNode(key).getAfter()),GatewayClass.JOIN)) {
				if(!Event.isEvent(diagram.getNode(key))) {
					UUID newID = UUID.randomUUID();
					Event newEvent = new Event(newID.toString(), newID.toString(), EventType.INTERMEDIATE, 0);
					newEvent.setAfter(diagram.getNode(key).getAfter());
					newEvent.setBefore(diagram.getNode(key).getAfter());
					addedEvents.add(newEvent);
				}
			}
		}
		for(Event event:addedEvents) {
			UUID newAfter = UUID.randomUUID();
			Gateway gNode = (Gateway)diagram.getNode(event.getAfter());
			event.setAfter(newAfter.toString());
			gNode.getIncomingNodes().remove(event.getBefore());
			gNode.addIncomingWay(event.getAfter());
			diagram.getNodes().put(event.getBefore(), event);
			diagram.getNodes().put(event.getAfter(), gNode);
		}
		return diagram;
	}
}
