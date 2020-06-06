import java.util.ArrayList;
import java.sql.SQLException;
import java.util.*;

public class PageRanker {
	private double[][] weightMatrix;
	private double[] scoreVector;
	private ArrayList<Node> graph;
	private HashMap<String,Integer> siteNodes;
	private int numSites;
	private double d;
	
	public PageRanker(ArrayList<Node> graph) {
		this.graph = graph;
		d = 0.15;
		initNodes();
		initScoreVector();
		initGoogleMatrix();
	}
	
	private void initNodes()  {
		Set<String> sites = new HashSet<String>();
		for(int i=0;i<graph.size();i++)
		{
			sites.add(graph.get(i).getId());
			int outDegreeSize = graph.get(i).getOutDegree().size();
			for(int j=0 ;j < outDegreeSize;j++)
			{
				sites.add(graph.get(i).getOutDegree().get(j));
			}
		}
		numSites = sites.size();
		int i=0;
		siteNodes = new HashMap<String,Integer>();

		for(String site : sites)
		{
			siteNodes.put(site, i);
			i++;
		}
	}
	
	private void initScoreVector()
	{
		// given the google matrix is a sparse matrix
		// so we can assume that init scores are 1 for better convergence
		double probability = 1.0;
//		double probability = 1.0 / numSites;
		scoreVector = new double[numSites];
		for(int i=0;i<numSites;i++)
		{
			scoreVector[i] = probability;
		}
		
	}
	// M = (1-d)*A + d*B
	private void initGoogleMatrix()
	{
		weightMatrix = new double[numSites][numSites];
		for(int i=0;i<graph.size();i++)
		{
			int outDegreeSize = graph.get(i).getOutDegree().size();
			String siteName =  graph.get(i).getId();
			double votePortion ;
			if(outDegreeSize>0)
				votePortion =  1.0 / outDegreeSize;
			else
				// dangling link
				votePortion = 0.0;
			for(int j=0;j<outDegreeSize;j++)
			{
				String referredSiteName = graph.get(i).getOutDegree().get(j);
				weightMatrix[siteNodes.get(referredSiteName)][siteNodes.get(siteName)] = votePortion * (1.0-d) + d*(1.0/numSites);
			}
		}
		
	}
	
	private void processScoreVector()
	{
		double[] newScoreVector = new double[numSites];
		for(int i=0;i<numSites;i++)
		{
			for(int j=0;j<numSites;j++)
			{
				if(j != 0)
					newScoreVector[i] += scoreVector[j]*weightMatrix[i][j];
				else {
					newScoreVector[i] = scoreVector[j]*weightMatrix[i][j];
				}
			}
		}
		for(int i =0 ;i<numSites;i++)
		{
			scoreVector[i] = newScoreVector[i] ; 
		}
	}
	
	public void divergeScoreVector(int iter)
	{
		for(int i=0;i<iter;i++)
			processScoreVector();
	}
	
	public double[] getScoreVector() {
		return scoreVector;
	}
	public void saveResult() throws SQLException
	{
		MySQLAccess db = new MySQLAccess();
		int i=0;
		String data = "";
		db.createPopRank();
		for(HashMap.Entry<String,Integer> entry:siteNodes.entrySet()) {
			if (i == 10000 )
			{
				System.out.println("we are saving");
				data = data.substring(0, data.length() - 1);
				db.saveRank(data);
				i=0;
				data = "";
			}
			data = data.concat("('"+entry.getKey()+"',"+scoreVector[entry.getValue()]+"),");
			i++;
		}
		if(i>0)
		{
			data = data.substring(0, data.length() - 1);
			db.saveRank(data);
		}

//		siteNodes.forEach((key, value) -> {
//
//			});
//			String dString = data.toString();
//			dString = dString.substring(0, dString.length() - 1);
//			db.saveRank(dString);

	}
	public void printScores() {

		for(HashMap.Entry<String,Integer> entry:siteNodes.entrySet())
		{
			System.out.print("key: "+ entry.getKey());
			System.out.println(", Value: "+ entry.getValue());
		}
		System.out.println("____________________________________________");
		for(int i =0 ;i<numSites;i++)
		{
			System.out.println(scoreVector[i]);
		}
		
	}
	
}
