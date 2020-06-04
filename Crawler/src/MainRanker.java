import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class MainRanker {

	public static void main(String[] args) throws Exception {

		MySQLAccess db = new MySQLAccess();
		String query = "SELECT LINK,REFERER FROM `crawler_table`";
		ResultSet resultSet = db.readDataBase(query);

		HashMap<String,Boolean> concernedLink = new HashMap<String,Boolean>();
		while(resultSet.next())
		{
			concernedLink.put(resultSet.getString(1),true);
		}

		ArrayList<Node> graph = new ArrayList<Node>();
		resultSet.beforeFirst();
		while(resultSet.next()) {
			Node node = new Node();
			node.setId(resultSet.getString(1).replaceAll("[\']", ""));
			ArrayList<String> sites = new ArrayList<String>();
			String referLink = resultSet.getString(2);
			for (String w : referLink.split("\\s", -1)) {
				if (w.length() > 3 && concernedLink.containsKey(w))
					sites.add(w.replaceAll("[\']", ""));
				node.setOutDegree(sites);
				graph.add(node);
			}
		}
			PageRanker pgrk = new PageRanker(graph);
			pgrk.divergeScoreVector(5);
			pgrk.saveResult();
//		 pgrk.printScores();

		}
	}

