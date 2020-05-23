import java.util.ArrayList;

public class Node {
	
	private String id;
	private ArrayList<String> outDegree;

	public Node(String id, ArrayList<String> outDegree) {
		this.id = id;
		this.outDegree = outDegree;
	}
	
	public Node() {
		this.id = null;
		this.outDegree = null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<String> getOutDegree() {
		return outDegree;
	}

	public void setOutDegree(ArrayList<String> outDegree) {
		this.outDegree = outDegree;
	}
	
	
}
