
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
	

	
	public static void main(String[] args) throws Exception {
		Indexer id  = new Indexer();
	    MySQLAccess dao = new MySQLAccess();
	    String dateString = "2020-05-25 20:34:41";
	    Timestamp tdate = java.sql.Timestamp.valueOf(dateString);
		long startTime = System.nanoTime();
		indexerThread idth = id.new indexerThread(dao);
		idth.loadStoppingWords();
	    idth.newCrawled = dao.getbylastupdate(tdate);

	    dao.delbyUrl(idth.newCrawled[0]);
	    
	    ArrayList<Thread> indexerThreads = new ArrayList<Thread>();
	    int threadsNumber = 8;
		for(int i = 0; i < threadsNumber;i++)
		{
			indexerThreads.add(new Thread (idth));
			indexerThreads.get(i).setName(Integer.toString(i));
			indexerThreads.get(i).start();
		}
		try {
			for(int i = 0; i < threadsNumber;i++)
			{
				indexerThreads.get(i).join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    
	    dao.close();
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("duration = "+duration*Math.pow(10,-9)+" seconds");
	}

	class indexerThread implements Runnable {
		
		ArrayList<String> [] newCrawled;
		MySQLAccess dao;
		int threadsNumber = 8;
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

			      e.printStackTrace();
			    }
		}
		
		indexerThread(MySQLAccess d){
			dao = d;
		}
		public void run() {

			int name = Integer.parseInt(Thread.currentThread().getName());
			int start = (threadsNumber+newCrawled[0].size()-1)/threadsNumber*name;
			int end = (threadsNumber+newCrawled[0].size()-1)/threadsNumber*(name+1);
			if(end > newCrawled[0].size()) end = newCrawled[0].size();
		    
		    for(int i = start; i < end; ++i) {
				HashMap<String, Integer > imgs_map = new HashMap<String, Integer >();
		    	HashMap<String, Integer[] > mp = new HashMap<String, Integer[] >();
		    	HashMap<String, Double > mpscore = new HashMap<String, Double >();

		    	Double totScore = 0.0, sum = 0.0;
		    	Double [] scores = {100.0, 30.0};
		    	String txts = newCrawled[1].get(i), url = newCrawled[0].get(i), imgs = newCrawled[2].get(i);

		    	String[] arrOfStr = txts.split("@@::;;@@;", -2);
		    	String[] arr2OfStr = imgs.split("@@::;;@@;", -2);
		    	for(int j = 0; j < arr2OfStr.length-1; j+=2) {
		    		String[] arrStr = arr2OfStr[j+1].split(" ", -2);
		    		for(String r: arrStr) {
		    			String s = r.replaceAll("[^a-zA-Z]","");
		    			s = s.toLowerCase();
		    			if(s.isEmpty() || stoppingWords.containsKey(s)) continue;
		    			s = stemWord(s);
		    			String str = s + "@@::;;@@;" + arr2OfStr[j];
		    			imgs_map.put(str, 1);
		    		}
		    	}
		    	for(int j = 0; j < arrOfStr.length-2; j += 2) {
		    		String[] arrStr = arrOfStr[j].split(" ", -2);
		    		for(String r: arrStr) {
		    			String s = r.replaceAll("[^a-zA-Z]","");
		    			s = s.toLowerCase();
		    			if(s.isEmpty() || stoppingWords.containsKey(s)) continue;
		    			s = stemWord(s);
		    			Integer[] it = {0,0,0};
		    			if(mp.containsKey(s)) 
		    				it = mp.get(s);
	    				it[j/2]++;
	    				totScore += scores[j/2];
	    				mp.put(s, it);
		    		}
		    	}
		    	for (HashMap.Entry<String, Integer[]> entry : mp.entrySet()) {
		    		Integer [] it = entry.getValue();
		    		Double score = (it[0]*scores[0] +it[1]*scores[1])/totScore;
				    //System.out.println(entry.getKey() + " = " + it[0] + " " + it[1] + " " + it[2] + " " + score);
				    sum += score;
				    mpscore.put(entry.getKey(), score);
				}
		    	url = url.replaceAll("[\']","");
		    	if(!mpscore.isEmpty())
			    	dao.insertWordUrl(url, mpscore);

		    	if(!(imgs_map.isEmpty()))
					dao.insertImages(imgs_map);
		    }

		}
		
	}
}