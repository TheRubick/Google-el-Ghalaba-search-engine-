import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import static java.lang.Integer.min;


public class crawler {

	/*
	num of pages to be crawler, it should be greater than the demanded web pages to be crawled.
	due many cases like duplicate URL,invalid URL,noise in the internet,etc..
	*/
	public static int numOfPages = 14000;
	//tags delimiter to be used on concatenating different tags together
	final String tagsDelimiter = "@@::;;@@;";
	//seedSet that would hold the URLs craweled.d
	public static List<String> seedSet = new ArrayList<String>();
	//number of the threads to be used in the crawling process
	public static int threadNumbers = 15;
	/*
	pageIter "Iteration" is the number of pages that have been fetched
	visitorPointer to indicate the current web page being crawled
	*/
	int pageIter = 0, visitorPointer = 0;
	//operation type to indicate the process : 0 for new crawl ,1 to crawl after being interrupted
	public static int operationType = 0;
	//File to store the links in the seedSet
	public static File takenLinksFile;
	//File writer to be used on writing the links of the seedSet in the file mentioned above
	public static FileWriter takenLinksWriter;
	public static void main(String[] args) throws Exception {

		//initialize new crawler object
		crawler c = new crawler();
		//initialize lock to be used by the threads
		final Object lockObj = new Object();
		//variable to store the beginning of the crawler
		long startTime = System.nanoTime();
		//database object to access the database
		MySQLAccess db = new MySQLAccess();
		//HashMap to hole the visited sites to make sure for preventing the duplication of sites
		HashMap<String,Integer> listOfVisitedSites = new HashMap<String ,Integer>();
		//threadNumber is assigned to each thread
		int threadNumber = 0;

		//create takenLinks file
		takenLinksFile = new File("takenLinks.txt");
		//new crawl operation
		if(operationType == 0)
		{
			//create new file
			takenLinksFile.createNewFile();
			//initialize the seed set
			initializeSeed();
			//initialize the takenLinks with the seedSet
			takenLinksWriter = new FileWriter("takenLinks.txt");
			//write the initialized seed set in the file
			for(String link : seedSet)
				takenLinksWriter.append(link+"\n");

		}
		//recrawel after being interrupted
		else if(operationType == 1)
		{
			//fetch links from the database and the takenLinks.txt then compare between them
			ArrayList<String> databaseLinks = new ArrayList<String>();
			ArrayList<String> takenLinks = new ArrayList<String>();
			//open takenLinks.txt and read the links from it
			Scanner linksFileReader = new Scanner(new File("takenLinks.txt"));
			while (linksFileReader.hasNextLine()) {
				String data = linksFileReader.nextLine();
				if(!data.isEmpty())
					takenLinks.add(data);
			}
			//read the links that has been written to the database
			ResultSet databaseFetchedLinks = db.readDataBase("SELECT Link FROM `crawler_table`");
			while (databaseFetchedLinks.next())
			{
				databaseLinks.add(databaseFetchedLinks.getString(1));
			}
			//add the different links to the seed set
			for(String link : takenLinks)
			{
				if(uniqueLink(databaseLinks,link))
				{
					seedSet.add(link);
				}
			}
			//numOfPages should be decreased by the number of links have been fetched from the database
			numOfPages -= databaseLinks.size();
			//to delete the previous links after being fetched from the file
			takenLinksFile.createNewFile();
			takenLinksWriter = new FileWriter("takenLinks.txt");
			for(String link : seedSet)
				takenLinksWriter.append(link+"\n");
			linksFileReader.close();
		}

		//create the crawler threads and initiate them
		ArrayList<Thread> crawlerThreads = new ArrayList<Thread>();
		for(int i = 0; i < threadNumbers;i++)
		{
			crawlerThreads.add(new Thread (c.new crawlerThread(lockObj,db,threadNumber,listOfVisitedSites)));
			crawlerThreads.get(i).start();
			threadNumber++;
		}
		try {
			for(int i = 0; i < threadNumbers;i++)
			{
				crawlerThreads.get(i).join();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			db.close();
		}
		System.out.println("deleting the takenLinksFile and closing the takenLinksWriter");
		takenLinksWriter.close();
		if(takenLinksFile.delete())
		{
			System.out.println("file has been deleted successfully");
		}
		else{
			System.out.println("error on deleting the takenLinksFile");
		}
		db.close();
		long endTime = System.nanoTime();
		//calculating the duration taken by the crawler
		long duration = endTime - startTime;
		System.out.println("duration of the crawler = "+duration*Math.pow(10,-9)+" seconds");
	}
	
	//initialize the seedSet
	private static void initializeSeed()
	{
		seedSet.add("https://wikipedia.org/wiki/UEFA_Men%27s_Player_of_the_Year_Award");
		seedSet.add("https://wikipedia.org/wiki/Will_Smith");
		seedSet.add("https://edition.cnn.com/");
		seedSet.add("https://www.foxnews.com/");
		seedSet.add("https://www.tutorialspoint.com/java/index.htm");
		seedSet.add("https://stackoverflow.com/");
		seedSet.add("https://www.w3schools.com/");
		seedSet.add("https://www.independent.co.uk/");
		seedSet.add("https://www.quora.com/What-are-the-best-Java-tutorials/");
	}
	
	
	class crawlerThread implements Runnable {
		/*
		here each thread would hold
			the db object from the crawler
			number
			listOfVisitedSites
			lock for synchronization
		*/
		private Object lock;
		private MySQLAccess db;
		private int threadNumber;
		private HashMap<String,Integer> listOfVisitedSites;
		public crawlerThread(Object lock,MySQLAccess db,int threadNumbers, HashMap<String,Integer> listOfVisitedSites)
		{
			this.db = db;
			this.lock = lock;
			this.threadNumber = threadNumbers;
			this.listOfVisitedSites = listOfVisitedSites;
		}
		public void run () {
			System.out.println("thread = "+(this.threadNumber));
			/*
				if pageIter has became >= the required number of pages and the visitorPointer has visited all
				the web pages then stop crawling
			*/
			while (pageIter < numOfPages || visitorPointer < numOfPages) {

				/*
					this while loop to make the thread sleep if the seedSetSize is small and the other threads
					could be satisfied for the crawling process
				*/
				int seedSetSize = seedSet.size();
				while (this.threadNumber >= seedSetSize)
				{
					System.out.println(seedSetSize);
					seedSetSize = seedSet.size();
				}

				/*
					in this part we normalize the currentWebPageURL
				*/
				String currentWebPageURL = "";
				synchronized (lock)
				{
					try{
						System.out.println("current seed = " + seedSet.get(visitorPointer));
						currentWebPageURL = normalizeSiteURL(seedSet.get(visitorPointer));
						//increment the visitorPointer to make other threads fetch the following URLs
						visitorPointer++;
						//if the current URL is duplicate then continue the while loop
						if(listOfVisitedSites.containsKey(currentWebPageURL))
						{
							continue;
						}
						listOfVisitedSites.put(currentWebPageURL,1);
					}catch(IllegalArgumentException e){
						continue;
					}
				}

				//in this part we make check on the robots.txt so we need to get the current host and path
				String currentPath = "";
				String currentHost = "";
				boolean isValidWebPage;
				try
				{
					currentPath = URI.create(currentWebPageURL).getPath();
					currentHost = URI.create(currentWebPageURL).getScheme() + "://" +
							URI.create(currentWebPageURL).getHost();
					isValidWebPage = checkRobots(currentHost, currentPath);
				}
				catch (IllegalArgumentException e)
				{
					System.out.println("illegal parameters in the URL");
					continue;
				}
				try{
					//if it is valid from the robots.txt
					if (isValidWebPage) {
						Document doc = Jsoup.connect(currentWebPageURL).get();
						System.out.println("this is the web page URL "+currentWebPageURL);
						//fetch the "a","title","h1","p" tags and the image source attributes
						Elements links = doc.body().getElementsByTag("a");
						Elements images = doc.body().getElementsByTag("img");
						String webPageTitle = doc.head().getElementsByTag("title").text();
						Elements headingElements = doc.body().getElementsByTag("h1");
						Elements paragraphElements = doc.body().getElementsByTag("p");

						//get the location of the current web page
						String URLLocation = getURLLocation(currentHost);
						//the objects are fetched from crawling through this web page
						String imageSources = "";
						String headingText = "";
						String paragraphText = "";
						String referLinks = "";
						//this list would be hold all the links fetched from the web page
						List<String> webPageLinks = new ArrayList<String>();

						//threshold for the crawling depth
						int webPageLinksThreshold = 50;

						//giving more weight for wikipedia links
						if(currentWebPageURL.indexOf("wikipedia.org") != -1)
							webPageLinksThreshold = 150;

						//loop on all the links
						for (Element link : links) {
							//if it reached the threshold therefore it should stop iterating
							if (webPageLinks.size() >= webPageLinksThreshold)
								break;
							String href = link.attr("href");
							//check if this href "link" isn't PDF
							if(!href.endsWith(".pdf"))
							{
								if (href.startsWith("http") || href.startsWith("https"))
								{
									webPageLinks.add(href);
									referLinks += href + " ";
								}
								else if(href.startsWith("//")) // in case of missing the protocol
								{
									href = "https:" + href;
									if (uniqueLink(seedSet, href) && uniqueLink(webPageLinks, href))
									{
										webPageLinks.add(href);
										referLinks += href + " ";
									}
								}
								else if (href.startsWith("/")) // in case of relative URL
								{
									href = currentHost + href;
									if (uniqueLink(seedSet, href) && uniqueLink(webPageLinks, href))
									{
										webPageLinks.add(href);
										referLinks += href + " ";
									}
								}
							}

						}

						int imgSize = Math.min(25,images.size()); //getting max 25 images from the web page
						for (int i = 0;i < imgSize;i++) {
							//get the src of the image
							String imgSrc = images.get(i).attr("src");
							//get the caption of this image
							String caption = images.get(i).attr("alt");
							if (caption.isEmpty())
								caption = " ";

							if(imgSrc.startsWith("https"))
							{
								imageSources += imgSrc + tagsDelimiter + caption + tagsDelimiter;
							}
							else if(imgSrc.startsWith("//")) // in case of missing the protocol
							{
								imgSrc = "https:" + imgSrc;
								imageSources += imgSrc + tagsDelimiter + caption + tagsDelimiter;
							}
							else if(imgSrc.startsWith("/")) // in case of relative URL
							{
								imageSources += currentWebPageURL + imgSrc + tagsDelimiter + caption + tagsDelimiter;
							}

						}

						//getting the all the heading tags' text
						for (Element hElement : headingElements) {
							headingText += hElement.text() + " ";
						}
						if (headingText.isEmpty())
							headingText = " ";
						//adding the delimiter at the end
						headingText += "@@::;;@@;h1@@::;;@@;";
						for (Element pElement : paragraphElements) {
							paragraphText += pElement.text() + " ";
						}
						if (paragraphText.isEmpty())
							paragraphText = " ";

						//thresholding the paragraph text
						paragraphText = paragraphText.substring(0,min(paragraphText.length(),50000));
						//adding the delimiter at the end
						paragraphText += "@@::;;@@;p@@::;;@@;";

						if(webPageTitle.isEmpty())
							webPageTitle = " ";
						//adding the delimiter at the end
						webPageTitle += "@@::;;@@;title@@::;;@@;";

						//this is also to prioritizing the wikipedia
						if(currentWebPageURL.indexOf("wikipedia.org") == -1)
							Thread.sleep(100);

						synchronized (lock)
						{
							for (int k = 0; k < webPageLinks.size() && pageIter < numOfPages; k++) {
								pageIter++;
								takenLinksWriter.append(webPageLinks.get(k) + "\n");
								seedSet.add(webPageLinks.get(k));
							}
							System.out.println("current seedSet size = " + pageIter);
							System.out.println("site host = " + currentWebPageURL);
							try {
								//elements of the web Page is the title then the heading then the paragraph
								String webPageTagsText = webPageTitle + headingText + paragraphText;
								db.writeResultSet(currentWebPageURL, webPageTagsText, imageSources,
										webPageTitle, referLinks, URLLocation);
								System.out.println("visitor = "+visitorPointer);

							} catch (SQLException e) {
								System.out.println((headingText+paragraphText).length());
								System.out.println("problem occured on writing in the database");
								e.printStackTrace();
							}

						}

					}
				}
				catch (IOException | InterruptedException e)
				{
					System.out.println("url doesn't exists");
					continue;
				}
			}
		

		}
	}
	

	private static String normalizeSiteURL(String siteURL)
	{
		String protocol = URI.create(siteURL).normalize().getScheme().toLowerCase();
		String path = URI.create(siteURL).normalize().getPath();
		String host = URI.create(siteURL).normalize().getHost().toLowerCase();
		String Query = "";
		if(URI.create(siteURL).getQuery() != null)
		{
			Query = URI.create(siteURL).normalize().getQuery().toLowerCase();
			Query = "?"+Query;
		}
		
		if(path.startsWith("/index") || path.startsWith("/default") || path.equals(""))
			path = "/";
		String normalizedURL = protocol+"://"+host+path+Query;
		System.out.println(normalizedURL);
		return normalizedURL;
	}
	
	private static boolean uniqueLink(List<String>seedSet,String link)
	{
		boolean isUnique = true;
		for(int j = 0;j < seedSet.size();j++)
		{
			if(link.equals(seedSet.get(j)) || (link+"/").equals(seedSet.get(j)))
				isUnique = false;
		}
		return isUnique;
	}
	
	private static boolean checkRobots(String currentHost,String currentPath)
	{
		boolean isValid = false;
		try {
			
			currentPath = currentPath.toLowerCase();
			Document doc = Jsoup.connect(currentHost+"/robots.txt").get();
			String docBody = doc.body().text().toLowerCase();
			int currentUserAgent = docBody.indexOf("user-agent: *");
			int nextUserAgent = docBody.indexOf("user-agent:",currentUserAgent +20);
			int checkURL = -1;
			
			if(currentUserAgent != -1)
			{
				if(currentUserAgent != nextUserAgent && nextUserAgent != -1)
				{
					//check if the user agent isn't the last one or the user agent isn't the only one
					docBody = docBody.substring(currentUserAgent, nextUserAgent);
				}
				else {
					docBody = docBody.substring(currentUserAgent);
				}

				checkURL = docBody.indexOf("disallow: "+currentPath +" ");
				if(checkURL == -1)
					isValid = true;
				else {
					System.out.println(docBody.substring(checkURL));
				}
			}
			else
				isValid = true;
		} catch (IOException e) {
			System.out.println(currentHost);
			System.out.println("web page doesn't have robots.txt file");
			isValid = true;
		}
		return isValid;
	}

	private String getURLLocation(String currentHost)
	{
		String webPageLocation = "--";
		String currentLocation = currentHost.substring(currentHost.length()-2);
		int locationsSize = internetCountryDomain.Locations.length;
		for(int i = 0;i < locationsSize;i++)
		{
			if(internetCountryDomain.Locations[i].equals((currentLocation)))
			{
				webPageLocation = internetCountryDomain.Locations[i];
				break;
			}
		}
		return webPageLocation;
	}
}

