import java.util.ArrayList;

public class MainRelevance {

	 public static void main(String[] args) throws Exception {
		 ArrayList<String> arr = new ArrayList<String>();
		 arr.add("hey");
		 arr.add("cow");
		 Relevance rel = new Relevance(arr);
		 rel.calcTfIdf();
	 }
	
}
