package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;

import bpmnElements.Diagram;
import bpmnElements.Node;
import bpmnElements.SubProcess;
import bpmnUtil.BPMNFactory;
import bpmnUtil.BPMNSubProcessValues;
import bpmnUtil.BPMNTravel;
import view.MainView; 

public class MainController {
	
	public static void main(String args[]) {
		MainView application = new MainView(new MainController());
	}
	
	public String calculate(String path) {
		String returnValue = "";
		try {
			Diagram diagram = BPMNFactory.createBPMNDiagram(path);
			BPMNSubProcessValues.calculateSubProcessValue(diagram);
			BPMNTravel travel = new BPMNTravel();
			travel.findRoutes(diagram);
			
			List<SubProcess> subProcess = getSubProcess(diagram);
			
			if(subProcess.size() > 0) {
				returnValue += "\nSubProcess evaluation\n\n";
				returnValue += "-----------------------------------------\n";
				for(SubProcess currentNode : subProcess) {
					returnValue += "Node: " + currentNode.getLabel() + "\n";
					returnValue += "Value: " + currentNode.getCost() + "\n";
					if(currentNode.getRepetitions() != 0) {
						returnValue += "Repetitions: " + currentNode.getRepetitions() + "\n";	
					}
					returnValue += "-----------------------------------------\n";
				}
			}
		
			returnValue += "\n\nRoutes evaluation\n";
			for(int i = 0 ; i < travel.getRoutes().size() ; i++) {
				returnValue += "-----------------------------------------\n";
				List<String> route = travel.getRoutes().get(i);
				for(String value: route) {
					returnValue += value + "\n";
				}
				returnValue += "\nRoute Evaluation = " + travel.getRoutesCost().get(i) + "\n";
				returnValue += "Pbb = " + travel.getRoutesPbbs().get(i) + "\n";
			}
			returnValue += "-----------------------------------------\n";
			returnValue += "\nDiagram value = " + travel.getTotalCost();
			return returnValue;
		} catch (JDOMException e) {
			return "MALFORMED DIAGRAM";
		} catch (IOException e) {
			return "MALFORMED DIAGRAM";
		}
	}
	
	private List<SubProcess> getSubProcess(Diagram diagram) {
		List<SubProcess> subProcess = new ArrayList<>();
		Map<String, Node> nodes = diagram.getNodes();
		for(String key: nodes.keySet()) {
			Node node = nodes.get(key);
			if(SubProcess.isSubProcess(node)) {
				subProcess.add((SubProcess)node);
				subProcess.addAll(getSubProcess((SubProcess)node));
			}
		}
		return subProcess;
	}
	
	private List<SubProcess> getSubProcess(SubProcess currentNode) {
		List<SubProcess> subProcess = new ArrayList<>();
		Map<String, Node> nodes = currentNode.getGraph().getNodes();
		for(String key: nodes.keySet()) {
			Node node = nodes.get(key);
			if(SubProcess.isSubProcess(node) && !currentNode.getLabel().equalsIgnoreCase(node.getLabel())) {
				subProcess.add((SubProcess)node);
				subProcess.addAll(getSubProcess((SubProcess)node));
			}
		}
		return subProcess;		
	}
}
