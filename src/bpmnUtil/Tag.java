package bpmnUtil;

public class Tag {
	//Tags to work
	public final static String DEFINITIONS = "definitions";
	public final static String PROCESS = "process";
	//Tags of node
	public final static String START_EVENT = "startEvent";
	public final static String INTERMEDIATE_EVENT = "intermediateThrowEvent";
	public final static String END_EVENT = "endEvent";
	public final static String TASK = "task";
	public final static String SUBPROCESS = "subProcess";
	public final static String XOR = "exclusiveGateway";
	public final static String OR = "inclusiveGateway";
	public final static String AND = "parallelGateway";
	//Tags of flow
	public final static String INCOMING = "incoming";
	public final static String OUTGOING = "outgoing";
	public final static String SEQUENCE_FLOW = "sequenceFlow";
	public final static String FORK = "Diverging";
	public final static String JOIN = "Converging";
	//Tags of attributes
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String VALUE = "value";
	public final static String DIRECTION = "gatewayDirection";
	public final static String DOCUMENTATION = "documentation";
	public final static String COST = "(cost:";
	public final static String PBB = "(pbb:";
	public final static String END_DOCUMENTATION = ")";
	//Tags of namespace
	public final static String BASE = "";
	//Tags of split
	public final static String SEMICOLON = ";";
	public final static String EQUALS = "=";
	public final static String LINE = "#";
}
