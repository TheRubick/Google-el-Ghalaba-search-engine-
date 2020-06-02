import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;



public class crawler {
		
	final int numOfPages = 1000;
	final String imageLinksDelimiter = "@@::;;@@;";
	public static List<String> seedSet = new ArrayList<String>();
	public static List<String> refererSet = new ArrayList<String>();
	public static List<String> imagesOfSeedSet = new ArrayList<String>();
	int pageIter = 0, visitorPointer = 0;
	final int webPageLinksThreshold = 100;
	public static int operationType = 0;
	
	

	public static void main(String[] args) {
		if(operationType == 0)
		{
			initializeSeed();
		}
		else if(operationType == 1)
		{
			//fetch links from the database then initialize the seed with it
		}
		
		crawler c = new crawler();
		final Object lockObj = new Object();
		long startTime = System.nanoTime();
		MySQLAccess db = new MySQLAccess();
		Thread th1 = new Thread (c.new crawlerThread(lockObj,db));
		Thread th2 = new Thread (c.new crawlerThread(lockObj,db));
		Thread th3 = new Thread (c.new crawlerThread(lockObj,db));
		Thread th4 = new Thread (c.new crawlerThread(lockObj,db));
		th1.start();
		th2.start();
		th3.start();
		th4.start();
		try {
			th1.join();
			th2.join();
			th3.join();
			th4.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			db.close();
		}
		db.close();
		long endTime = System.nanoTime();
		long duration = endTime - startTime;
		System.out.println("duration of the crawler = "+duration*Math.pow(10,-9)+" seconds");
	}
	
	//initialize the seedSet
	private static void initializeSeed()
	{
		seedSet.add("https://en.wikipedia.org/wiki/Mohamed_Salah");
		refererSet.add("--");
		seedSet.add("https://en.wikipedia.org/wiki/Lionel_Messi");
		refererSet.add("--");
		seedSet.add("https://edition.cnn.com/world/live-news/coronavirus-pandemic-05-30-20-intl/index.html");
		refererSet.add("--");
		seedSet.add(("https://www.tutorialspoint.com/java/index.htm"));
		refererSet.add("--");
		//seedSet.add("https://wikipedia.org");
		//seedSet.add("https://goal.com");
	}
	
	
	class crawlerThread implements Runnable {
		private Object lock = new Object();
		private MySQLAccess db;
		public crawlerThread(Object lock,MySQLAccess db)
		{
			this.db = db;
			this.lock = lock;
		}
		public void run () {
			if(operationType != 2) //not recrawel
			{

				while (pageIter < numOfPages || visitorPointer < numOfPages) {
					String currentWebPageURL = "";
					int refererIndex = 0;


						synchronized (lock) {
							System.out.println("current seed = " + seedSet.get(visitorPointer));
							currentWebPageURL = normalizeSiteURL(seedSet.get(visitorPointer));
							refererIndex = visitorPointer;
							visitorPointer++;
						}

						String currentPath = URI.create(currentWebPageURL).getPath();
						String currentHost = URI.create(currentWebPageURL).getScheme() + "://" +
								URI.create(currentWebPageURL).getHost();
						//System.out.println(currentPath);
						//System.out.println(currentHost);
						boolean isValidWebPage = checkRobots(currentHost, currentPath);
						try{
							if (isValidWebPage) {
								Document doc = Jsoup.connect(currentWebPageURL).get();
								System.out.println("this is the web page URL "+currentWebPageURL);
								Elements links = doc.body().getElementsByTag("a");
								Elements images = doc.body().getElementsByTag("img");
								String webPageTitle = doc.head().getElementsByTag("title").text();
								Elements headingElements = doc.body().getElementsByTag("h1");
								Elements paragraphElements = doc.body().getElementsByTag("p");
								//Elements headOneText = doc.body().getElementsByTag("h1");
								//Elements headTwoText = doc.body().getElementsByTag("h2");

								String URLLocation = getURLLocation(currentHost);
								String imageSources = "";
								String headingText = "";
								String paragraphText = "";
								List<String> webPageLinks = new ArrayList<String>();
								for (Element link : links) {
									if (webPageLinks.size() >= webPageLinksThreshold)
										break;
									String href = link.attr("href");
									if (href.startsWith("/")) {
										if (href.indexOf("http") != 0 || href.indexOf("https") != 0)
											href = currentHost + href;
										if (uniqueLink(seedSet, href) && uniqueLink(webPageLinks, href)) {
											//check if the web page is found or not
											//boolean checkExistance = urlExist(href);
											//if (checkExistance) {
												//check if this webpage exists or not
												//seedSet.add(href);
												webPageLinks.add(href);
												//System.out.println("href = " + href);
											//}

										}
									}

								}
								int imgSize = Math.min(25,images.size());
								for (int i = 0;i < imgSize;i++) {
									String imgSrc = images.get(i).attr("src");
									//System.out.println("ImgSrc = " + imgSrc);
									String caption = images.get(i).attr("alt");
									if (caption.isEmpty())
										caption = " ";
									/*
									if (imageExist(imgSrc)) {
										System.out.println("first attempt");
										imageSources += imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
									} else if (imageExist("https:" + imgSrc)) {
										System.out.println("second attempt");
										imageSources += "https:" + imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
									} else if (imageExist(currentHost + imgSrc)) {
										System.out.println("third attempt");
										imageSources += currentHost + imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
									}

									 */
									imageSources += imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
									/*

									if(!images.get(i).attr("width").isEmpty() && !images.get(i).attr("height").isEmpty())
									{
										if(Integer.parseInt(images.get(i).attr("width")) > 50 && Integer.parseInt(images.get(i).attr("height")) > 50)
											imageSources += imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
									}
									else {
										imageSources += imgSrc + imageLinksDelimiter + caption + imageLinksDelimiter;
									}
									*/

								}
								for (Element hElement : headingElements) {
									headingText += hElement.text();
								}
								if (headingText.isEmpty())
									headingText = " ";
								headingText += "^h1^";
								for (Element pElement : paragraphElements) {
									paragraphText += pElement.text();
								}
								if (paragraphText.isEmpty())
									paragraphText = " ";
								if(paragraphText.length() > 50000)
									paragraphText = paragraphText.substring(0,50000);
								paragraphText += "^p^";
								synchronized (lock) {
									for (int k = 0; k < webPageLinks.size() && pageIter < numOfPages; k++) {
										pageIter++;
										seedSet.add(webPageLinks.get(k));
										refererSet.add(currentWebPageURL);
									}
									imagesOfSeedSet.add(imageSources);
									System.out.println("seedSet after i = " + pageIter);
									System.out.println("site host = " + currentWebPageURL);
									//System.out.println(imageSources);
									//System.out.println(headingText + paragraphText);
									System.out.println(""); // publish date
									String refererLink = refererSet.get(refererIndex);
									try {
										String webPageTagsText = headingText + paragraphText;
										db.writeResultSet(currentWebPageURL, webPageTagsText, imageSources,
												webPageTitle, refererLink, URLLocation);
									} catch (SQLException e) {
										System.out.println((headingText+paragraphText).length());
										System.out.println("problem occured on writing in the database");
										e.printStackTrace();
									}

								}

							}
						}catch (IOException e)
						{
							System.out.println("url doesn't exists");
							continue;
						}
				}
			}
		

		}
	}
	
	
	private static boolean urlExist(String href) {
		boolean exists = true;
		try {
			Jsoup.connect(href).timeout(2000).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("Page isn't found"); 
			exists = false;
		}
		return exists;
	}
	
	
	private static boolean imageExist(String href) throws IOException {
		boolean exists = true;
		/*
		try {
			Image img = ImageIO.read(new URL(href));
			if(img != null){
				// to get rid from the small , thin images
				if(img.getWidth(null) < 50 || img.getHeight(null) < 50)
				{
					exists = false;
				}
			}else{
				//if the url doesn't point to image
			    System.out.println("NOT IMAGE");
			    exists = false;
			}
		} catch (IOException e) {
			//e.printStackTrace();
			//System.out.println("Image isn't found");
			exists = false;
		}
		*/
		int contentLength;
		try
		{
			URL url = new URL(href);
			URLConnection conn = url.openConnection();
			contentLength = conn.getContentLength();
			System.out.println(contentLength);
			if(contentLength < 10000)
				exists = false;
		}catch ( IOException e)
		{
			contentLength = -1;
			exists = false;
		}


		// now you get the content length

		return exists;
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
		//System.out.println("link = "+link);
		//if(seedSet.size() == 1)
		//{
			//System.out.println("Yes = "+seedSet.get(0));
		//}
		for(int j = 0;j < seedSet.size();j++)
		{
			//System.out.println("seed link = "+seedSet.get(j));
			if(link.equals(seedSet.get(j)))
				isUnique = false;
		}
		return isUnique;
	}
	
	private static boolean checkRobots(String currentHost,String currentPath)
	{
		boolean isValid = false;
		try {
			
			currentPath = currentPath.toLowerCase();
			Document doc = Jsoup.connect(currentHost+"/robots.txt").timeout(2000).get();
			String docBody = doc.body().text().toLowerCase();
			int currentUserAgent = docBody.indexOf("user-agent: *");
			int nextUserAgent = docBody.indexOf("user-agent:",currentUserAgent +20);
			int checkURL = -1;
			
			
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
			//System.out.println("current = "+(currentUserAgent));
			//System.out.println("next = "+nextUserAgent);
			//System.out.println(docBody);
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
		for(int i = 0;i < locationsSize;i++) //el mafrod hena yb2a feh global array
		{
			//System.out.println(i);
			if(internetCountryDomain.Locations[i].equals((currentLocation)))
			{
				webPageLocation = internetCountryDomain.Locations[i];
				break;
			}
		}
		return webPageLocation;
	}
}

