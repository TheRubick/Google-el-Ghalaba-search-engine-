import java.awt.desktop.SystemSleepEvent;
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

import static java.lang.Integer.max;
import static java.lang.Integer.min;


public class crawler {
		
	public static int numOfPages = 14000;
	final String imageLinksDelimiter = "@@::;;@@;";
	public static List<String> seedSet = new ArrayList<String>();
	public static List<String> imagesOfSeedSet = new ArrayList<String>();
	public static int threadNumbers = 15;
	int pageIter = 0, visitorPointer = 0;
	public static int operationType = 0;
	public static File takenLinksFile;
	public static FileWriter takenLinksWriter;
	public static void main(String[] args) throws Exception {

		crawler c = new crawler();
		final Object lockObj = new Object();
		long startTime = System.nanoTime();
		MySQLAccess db = new MySQLAccess();
		HashMap<String,Integer> listOfVisitedSites = new HashMap<String ,Integer>();
		int threadNumber = 0;

		takenLinksFile = new File("takenLinks.txt");
		if(operationType == 0)
		{
			takenLinksFile.createNewFile();
			initializeSeed();
			//initialize the takenLinks with the seedSet
			takenLinksWriter = new FileWriter("takenLinks.txt");
			for(String link : seedSet)
				takenLinksWriter.append(link+"\n");

		}
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
			for(String link : takenLinks)
			{
				if(uniqueLink(databaseLinks,link))
				{
					seedSet.add(link);
				}
			}
			numOfPages -= databaseLinks.size();
			//to delete the previous links after being fetched from the file
			takenLinksFile.createNewFile();
			takenLinksWriter = new FileWriter("takenLinks.txt");
			for(String link : seedSet)
				takenLinksWriter.append(link+"\n");
			linksFileReader.close();
		}

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
			while (pageIter < numOfPages || visitorPointer < numOfPages) {
				int seedSetSize = seedSet.size();
				while (this.threadNumber >= seedSetSize)
				{
					System.out.println(seedSetSize);
					seedSetSize = seedSet.size();
				}
				String currentWebPageURL = "";

				synchronized (lock)
				{
					try{
						System.out.println("current seed = " + seedSet.get(visitorPointer));
						currentWebPageURL = normalizeSiteURL(seedSet.get(visitorPointer));
						visitorPointer++;
						if(listOfVisitedSites.containsKey(currentWebPageURL))
						{
							continue;
						}
						listOfVisitedSites.put(currentWebPageURL,1);
					}catch(IllegalArgumentException e){
						continue;
					}
				}

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
					if (isValidWebPage) {
						Document doc = Jsoup.connect(currentWebPageURL).get();
						System.out.println("this is the web page URL "+currentWebPageURL);
						Elements links = doc.body().getElementsByTag("a");
						Elements images = doc.body().getElementsByTag("img");
						String webPageTitle = doc.head().getElementsByTag("title").text();
						Elements headingElements = doc.body().getElementsByTag("h1");
						Elements paragraphElements = doc.body().getElementsByTag("p");

						String URLLocation = getURLLocation(currentHost);
						String imageSources = "";
						String headingText = "";
						String paragraphText = "";
						String referLinks = "";
						List<String> webPageLinks = new ArrayList<String>();

						int webPageLinksThreshold = 50;

						//giving more weight for wikipedia links
						if(currentWebPageURL.indexOf("wikipedia.org") != -1)
							webPageLinksThreshold = 150;
						int referNumLinks = 0;
						for (Element link : links) {
							if (webPageLinks.size() >= webPageLinksThreshold)
								break;
							String href = link.attr("href");
							if(!href.endsWith(".pdf"))
							{
								if (href.startsWith("http") || href.startsWith("https"))
								{
									webPageLinks.add(href);
									//if(referNumLinks < 2)
									//{
										referLinks += href + " ";
										referNumLinks++;
									//}
								}
								else if(href.startsWith("//")) // in case of missing the protocol
								{
									href = "https:" + href;
									if (uniqueLink(seedSet, href) && uniqueLink(webPageLinks, href))
									{
										webPageLinks.add(href);
										//if(referNumLinks < 2)
										//{
											referLinks += href + " ";
											referNumLinks++;
										//}
									}
								}
								else if (href.startsWith("/")) // in case of relative URL
								{
									href = currentHost + href;
									if (uniqueLink(seedSet, href) && uniqueLink(webPageLinks, href))
									{
										webPageLinks.add(href);
										//if(referNumLinks < 2)
										//{
											referLinks += href + " ";
											referNumLinks++;
										//}
									}
								}
							}

						}

						int imgSize = Math.min(25,images.size());
						for (int i = 0;i < imgSize;i++) {
							String imgSrc = images.get(i).attr("src");
							String caption = images.get(i).attr("alt");
							if (caption.isEmpty())
								caption = " ";

							if(imgSrc.startsWith("https"))
							{
								imageSources += imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
							}
							else if(imgSrc.startsWith("//"))
							{
								imgSrc = "https:" + imgSrc;
								imageSources += imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
							}
							else if(imgSrc.startsWith("/"))
							{
								imageSources += currentWebPageURL + imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
							}

						}

						for (Element hElement : headingElements) {
							headingText += hElement.text() + " ";
						}
						if (headingText.isEmpty())
							headingText = " ";
						headingText += "@@::;;@@;h1@@::;;@@;";
						for (Element pElement : paragraphElements) {
							paragraphText += pElement.text() + " ";
						}
						if (paragraphText.isEmpty())
							paragraphText = " ";

						paragraphText = paragraphText.substring(0,min(paragraphText.length(),50000));
						paragraphText += "@@::;;@@;p@@::;;@@;";

						if(webPageTitle.isEmpty())
							webPageTitle = " ";
						webPageTitle += "@@::;;@@;title@@::;;@@;";


						if(currentWebPageURL.indexOf("wikipedia.org") == -1)
							Thread.sleep(100);

						synchronized (lock)
						{
							for (int k = 0; k < webPageLinks.size() && pageIter < numOfPages; k++) {
								pageIter++;
								takenLinksWriter.append(webPageLinks.get(k)+"\n");
								seedSet.add(webPageLinks.get(k));
							}
							imagesOfSeedSet.add(imageSources);
							System.out.println("current seedSet size = " + pageIter);
							System.out.println("site host = " + currentWebPageURL);
							System.out.println(""); // publish date
							String refererLink = referLinks;
							try {
								String webPageTagsText = webPageTitle + headingText + paragraphText;
								db.writeResultSet(currentWebPageURL, webPageTagsText, imageSources,
										webPageTitle, refererLink, URLLocation);
								System.out.println("visitor = "+visitorPointer);

							} catch (SQLException e) {
								System.out.println((headingText+paragraphText).length());
								System.out.println("problem occured on writing in the database");
								numOfPages++;
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

