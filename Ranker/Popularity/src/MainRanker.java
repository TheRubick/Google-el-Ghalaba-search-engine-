import java.sql.ResultSet;
import java.util.ArrayList;
public class MainRanker {

	 public static void main(String[] args) throws Exception {
		 
		 MySQLAccess db = new MySQLAccess();
		 String query = "SELECT LINK,REFERER FROM `crawler_table`";
		 ResultSet resultSet = db.readDataBase(query);
		 
		 ArrayList<Node> graph = new ArrayList<Node>(); 
		 
	     while(resultSet.next())
	     {
	    	 Node node = new Node();
	    	 node.setId(resultSet.getString(1));
	    	 ArrayList<String> sites = new ArrayList<String>();
	    	 String referLink = resultSet.getString(2);
	    	 for(String w:referLink.split("\\s",0)){  
	    		 sites.add(w);
	    		 } 
	    	 node.setOutDegree(sites);
	    	 graph.add(node);
	     }
		 PageRanker pgrk = new PageRanker(graph);
		 pgrk.divergeScoreVector(50);
		 pgrk.saveResult();
//		 pgrk.printScores();
	 }
	 

}