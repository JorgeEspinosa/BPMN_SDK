package bpmnUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import bpmnElements.Diagram;
import bpmnElements.Event;
import bpmnElements.EventType;
import bpmnElements.SequenceFlow;
import bpmnElements.Gateway;
import bpmnElements.GatewayClass;
import bpmnElements.GatewayType;
import bpmnElements.Node;
import bpmnElements.SubProcess;
import bpmnElements.Task;

public class BPMNFactory {
	public static Namespace baseNamespace;
	public static Element definitionsXML;
	public static Element diagramXML;
	
	public static Diagram createBPMNDiagram(String bpmnFileName) throws JDOMException, IOException {
		Document jdomDocument = createJDOMDocument(bpmnFileName);
		if(!rootIsDefinitions(jdomDocument))
			return null;
		definitionsXML = jdomDocument.getRootElement();
		diagramXML = getDiagramProcess(definitionsXML);
		if(diagramXML == null)
			return null;
		baseNamespace = definitionsXML.getNamespace(Tag.BASE);
		Diagram diagram = createGraph(diagramXML);
        return diagram;
	}
	
	private static Document createJDOMDocument(String bpmnFileName) throws JDOMException, IOException {
		File bpmnFile = new File(bpmnFileName);
		SAXBuilder jdomBuilder = new SAXBuilder();
		Document jdomDocument = jdomBuilder.build(bpmnFile);
		return jdomDocument;
	}
	
	private static boolean rootIsDefinitions(Document jdomDocument) {
		Element definitions = jdomDocument.getRootElement();
		return definitions.getName().equals(Tag.DEFINITIONS);
	}
	
	private static Element getDiagramProcess(Element root) {
		List<Element> children = root.getChildren();
        List<Element> process = new ArrayList<Element>();
        for (int i = 0; i < children.size(); i++) {
            Element child = children.get(i);
            if(child.getName().equals(Tag.PROCESS))
            	process.add(child);
        }
        Element diagram = null;
        for(Element element:process) {
        	if(!element.getChildren().isEmpty()) {
        		diagram = element;
        	}
        }
        return diagram;
	}
	
	private static Diagram createGraph(Element diagramElement) {
		Diagram diagram = new Diagram();
		
		List<Element> elementsSet = diagramElement.getChildren();
		
		for(int i = 0 ; i < elementsSet.size() ; i++) {
			Element element = elementsSet.get(i);
			if(element.getName().equals(Tag.START_EVENT)) {
				Event startEvent = createEvent(element, EventType.START);
				diagram.setStartEvent(startEvent);
			} else if(element.getName().equals(Tag.INTERMEDIATE_EVENT)) {
				Event intermediateEvent = createEvent(element, EventType.INTERMEDIATE);
				diagram.addNode(intermediateEvent);
			} else if(element.getName().equals(Tag.END_EVENT)) {
				Event endEvent = createEvent(element, EventType.END);
				diagram.addNode(endEvent);
			} else if(element.getName().equals(Tag.TASK)) {
				Task task = createTask(element);
				diagram.addNode(task);
			} else if(element.getName().equals(Tag.SUBPROCESS)) {
				SubProcess subProcess = createSubProcess(element);
				Diagram internGraph = createGraph(element);
				subProcess.setGraph(internGraph);
				diagram.addNode(subProcess);
			} else if(element.getName().equals(Tag.AND)) {
				Gateway andGateway = createGateway(element, GatewayType.AND);
				diagram.addNode(andGateway);
			} else if(element.getName().equals(Tag.XOR)) {
				Gateway xorGateway = createGateway(element, GatewayType.XOR);
				diagram.addNode(xorGateway);
			} else if(element.getName().equals(Tag.OR)) {
				Gateway orGateway = createGateway(element, GatewayType.OR);
				diagram.addNode(orGateway);
			} else if(element.getName().equals(Tag.SEQUENCE_FLOW)) {
				SequenceFlow sequenceFlow = createSequenceFlow(element);
				diagram.addFlow(sequenceFlow);
			}
		}
		diagram = recreateGatewayFlows(diagram);
		return diagram;
	}

	private static Event createEvent(Element element, EventType eType) {
		String label = element.getAttributeValue(Tag.NAME);
		String id = element.getAttributeValue(Tag.ID);
		double cost = getCostDocumentation(element);
		Event event = new Event(label, id, eType, cost);
		if(eType == EventType.END || eType == EventType.INTERMEDIATE) {
			String incoming = element.getChild(Tag.INCOMING, definitionsXML.getNamespace(Tag.BASE)).getValue();
			event.setBefore(incoming);
		}
		if(eType == EventType.START || eType == EventType.INTERMEDIATE) {
			String outgoing = element.getChild(Tag.OUTGOING, definitionsXML.getNamespace(Tag.BASE)).getValue();
			event.setAfter(outgoing);
		}
		return event;
	}
	
	private static Task createTask(Element element) {
		String label = element.getAttributeValue(Tag.NAME);
		String id = element.getAttributeValue(Tag.ID);
		double cost = getCostDocumentation(element);
		Task task = new Task(label, id, cost);
		String incoming = element.getChild(Tag.INCOMING, definitionsXML.getNamespace(Tag.BASE)).getValue();
		String outgoing = element.getChild(Tag.OUTGOING, definitionsXML.getNamespace(Tag.BASE)).getValue();
		task.setBefore(incoming);
		task.setAfter(outgoing);
		return task;
	}
	
	private static SubProcess createSubProcess(Element element) {
		String label = element.getAttributeValue(Tag.NAME);
		String id = element.getAttributeValue(Tag.ID);
		double cost = getCostDocumentation(element);
		SubProcess subProcess = new SubProcess(label, id, cost);
		String incoming = element.getChild(Tag.INCOMING, definitionsXML.getNamespace(Tag.BASE)).getValue();
		String outgoing = element.getChild(Tag.OUTGOING, definitionsXML.getNamespace(Tag.BASE)).getValue();
		subProcess.setBefore(incoming);
		subProcess.setAfter(outgoing);
		return subProcess;
	}
	
	private static Gateway createGateway(Element element, GatewayType gType) {
		String label = element.getAttributeValue(Tag.NAME);
		String id = element.getAttributeValue(Tag.ID);
		GatewayClass gClass = element.getAttributeValue(Tag.DIRECTION).equals(Tag.FORK)?GatewayClass.FORK:GatewayClass.JOIN;
		Gateway gateway = new Gateway(label, id, gType, gClass);
		if(gClass == GatewayClass.FORK) { 
			if(gType != GatewayType.AND) {
				Map<String, Double> pbbs = getPbbDocumentation(element);
				gateway.setOutgoingPbb(pbbs);
			}
			String incoming = element.getChild(Tag.INCOMING, definitionsXML.getNamespace(Tag.BASE)).getValue();
			gateway.setBefore(incoming);
			List<Element> flows = element.getChildren();
			for(Element el: flows) {
				if(el.getName().equals(Tag.OUTGOING)) {
					gateway.addOutgoingWay(el.getValue());
				}
			}
		} else {
			String outgoing = element.getChild(Tag.OUTGOING, definitionsXML.getNamespace(Tag.BASE)).getValue();
			gateway.setAfter(outgoing);
			List<Element> flows = element.getChildren();
			for(Element el: flows) {
				if(el.getName().equals(Tag.INCOMING)) {
					gateway.addIncomingWay(el.getValue());
				}
			}
		}		
		return gateway;
	}
	
	private static SequenceFlow createSequenceFlow(Element element) {
		String label = element.getAttributeValue(Tag.NAME);
		String id = element.getAttributeValue(Tag.ID);
		SequenceFlow sequenceFlow = new SequenceFlow(label, id);
		return sequenceFlow;
	}
	
	private static String filterDocumentation(Element node, String key) {
		Element element = node.getChild(Tag.DOCUMENTATION, definitionsXML.getNamespace(Tag.BASE));
		if(element == null) {
			return null;
		}
		String value = element.getValue();
		if(value == null) 
			return null;
		int first = value.indexOf(key);
		first += (key).length();
		int last = value.indexOf(Tag.END_DOCUMENTATION, first);
		if(first < 0 || last < 0) 
			return null;
		return value.substring(first, last).trim();
	}
	
	private static double getCostDocumentation(Element node) {
		String value = filterDocumentation(node, Tag.COST);
		if(value == null)
			return 0;
		try {
			return Double.parseDouble(value);
		} catch(NumberFormatException e) {
			return 0;
		}
	}
	
	private static Map<String, Double> getPbbDocumentation(Element node) {
		String value = filterDocumentation(node, Tag.PBB);
		Map<String, Double> pbbs = new HashMap<>();
		if(value == null) {
			return null;
		}
		String[] arrayPbb = value.trim().split(Tag.SEMICOLON);
		for(String current: arrayPbb) {
			String[] keyValue = current.trim().split(Tag.EQUALS);
			try {
				pbbs.put(keyValue[0], Double.parseDouble(keyValue[1]));
			} catch(NumberFormatException e) {
				return null;
			}
		}
		return pbbs;
	}
	
	private static Diagram recreateGatewayFlows(Diagram diagram) {
		if(diagram == null)
			return null;
		Set<String> keys = diagram.getNodes().keySet();
		Map<String, SequenceFlow> flows = diagram.getFlows();
		for(String key: keys) {
			Node node = diagram.getNode(key);
			if(Gateway.isGClass(node, GatewayClass.FORK)) {
				if(Gateway.isGType(node, GatewayType.XOR)) {
					Gateway gNode = (Gateway) node;
					Map<String, Double> pbbs = gNode.getOutgoingPbbs();
					for(String currentId: gNode.getOutgoingNodes()) {
						String label = flows.get(currentId).getLabel();
						double pbb = pbbs.get(label);
						pbbs.remove(label, pbb);
						pbbs.put(currentId, pbb);
					}
					gNode.setOutgoingPbb(pbbs);
					diagram.getNodes().replace(gNode.getId(), gNode);
				} else if(Gateway.isGType(node, GatewayType.OR)) {
					Gateway gNode = (Gateway) node;
					Map<String, Double> pbbs = gNode.getOutgoingPbbs();
					Set<String> pbbsKeys = new HashSet<>();
					for(String current: pbbs.keySet()) {
						pbbsKeys.add(current);
					}
					for(String currentId: pbbsKeys) {
						String list = ""+currentId;
						for(String id: gNode.getOutgoingNodes()) {
							list = list.replace(flows.get(id).getLabel(), id);
						}
						double pbb = pbbs.get(currentId);
						pbbs.remove(currentId, pbb);
						pbbs.put(list, pbb);
					}
					gNode.setOutgoingPbb(pbbs);
					diagram.getNodes().replace(gNode.getId(), gNode);
				}
			}
		}
		return diagram;
	}
}
