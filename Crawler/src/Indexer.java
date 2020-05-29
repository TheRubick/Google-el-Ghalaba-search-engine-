import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.tartarus.snowball.ext.PorterStemmer;

public class Indexer {
	
	private HashMap<String, Integer> stoppingWords = new HashMap<String, Integer>();
	
	private String stemWord(String s) {
		PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(s);
        stemmer.stem();
		String ret = stemmer.getCurrent();
		return ret;
	}
	
	public void loadStoppingWords() {
		
		try {
		      File myObj = new File(config.stoppingWordsPath);
		      Scanner myReader = new Scanner(myObj);
		      while (myReader.hasNextLine()) {
		        String data = myReader.nextLine();
		        data = data.toLowerCase();
		        stoppingWords.put(data, 1);
		      }
		      myReader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
	}
	
	public static void main(String[] args) throws Exception {
		Indexer id  = new Indexer();
	    MySQLAccess dao = new MySQLAccess();
	    id.loadStoppingWords();
	    String dateString = "2020-05-25 20:34:41";
	    Timestamp tdate = java.sql.Timestamp.valueOf(dateString);
	    System.out.println(tdate);
	    ArrayList<String> [] arr = dao.getbylastupdate(tdate);

	    
	    
	    for(int i = 0; i < arr[0].size(); ++i) {
	    	HashMap<String, Integer[] > mp = new HashMap<String, Integer[] >();
	    	HashMap<String, Double > mpscore = new HashMap<String, Double >();
	    	Double totScore = 0.0, sum = 0.0;
	    	int [] freq = {0,0};
	    	String txts = arr[1].get(i), url = arr[0].get(i), imgs = arr[2].get(i);
	    	
	    	String[] arrOfStr = txts.split("\\^", -2);
	    	for(int j = 0; j < 3; j += 2) {
	    		String[] arrStr = arrOfStr[j].split(" ", -2);
	    		for(String r: arrStr) {
	    			String s = r.replaceAll("[^a-zA-Z]","");
	    			s = s.toLowerCase();
	    			if(s.isEmpty() || id.stoppingWords.containsKey(s)) continue;
	    			s = id.stemWord(s);
	    			Integer[] it = {0,0,0};
	    			if(mp.containsKey(s)) 
	    				it = mp.get(s);
    				it[j]++;
    				it[1]++;
    				freq[j/2]++;
    				mp.put(s, it);
	    		}
	    	}
	    	totScore = freq[1]*2.0;
	    	dao.delbyUrl(url);
	    	for (HashMap.Entry<String, Integer[]> entry : mp.entrySet()) {
	    		Integer [] it = entry.getValue();
	    		Double score = (it[0]*freq[1])/(freq[0]*totScore) + it[2]/totScore;
	    		if(freq[0] == 0) score = it[2]*1.0/freq[1];
	    		if(freq[1] == 0) score = it[0]*1.0/freq[0];
			    //System.out.println(entry.getKey() + " = " + it[0] + " " + it[1] + " " + it[2] + " " + score);
			    sum += score;
			    mpscore.put(entry.getKey(), score);
			}
		    dao.insertWordUrl(url, mpscore);
	    	System.out.println(sum);
	    	System.out.println("-------------------------------------");
	    }
	    dao.close();
	  }
}
