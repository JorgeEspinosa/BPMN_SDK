package bpmnUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jdom2.JDOMException;

import bpmnElements.Diagram;

public class Test {
	public static void main(String[] args) {
		try {
			Diagram diagram = BPMNFactory.createBPMNDiagram("Diagram 1.bpmn");
			//diagram = BPMNNormalizer.diagramNormalization(diagram);
			//Diagram diagram = BPMNFactory.createBPMNDiagram("New Process.bpmn");
			BPMNSubProcessValues.calculateSubProcessValue(diagram);
			BPMNTravel travel = new BPMNTravel();
			System.out.println("---PRE---");
			travel.findRoutes(diagram);
			System.out.println("--START--");
			for(List<String> route:travel.getRoutes()) {
				System.out.println("-current-");
				for(String value: route) {
					System.out.println(value);
				}
				System.out.println("--next--");
			}
			System.out.println("---END---");
			System.out.println(travel.getTotalCost());
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
