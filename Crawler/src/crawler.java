import java.awt.Image;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;






public class crawler {
	
	
	
	
	final int numOfPages = 12;
	public static List<String> seedSet = new ArrayList<String>();
	public static List<String> imagesOfSeedSet = new ArrayList<String>();
	int pageIter = 0, visitorPointer = 0,webPageLinksThreshold = 4;
	
	

	public static void main(String[] args) {
		initializeSeed();
		crawler c = new crawler();
		final Object lockObj = new Object();
		Thread th1 = new Thread (c.new crawlerThread(lockObj));
		Thread th2 = new Thread (c.new crawlerThread(lockObj));
		Thread th3 = new Thread (c.new crawlerThread(lockObj));
		th1.start();
		th2.start();
		th3.start();
		try {
			th1.join();
			th2.join();
			th3.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//initialize the seedSet
	private static void initializeSeed()
	{
		seedSet.add("https://en.wikipedia.org/wiki/Mohamed_Salah");
		seedSet.add("https://en.wikipedia.org/wiki/Lionel_Messi");
		seedSet.add("https://edition.cnn.com/world/live-news/coronavirus-pandemic-05-13-20-intl/index.html");
		//seedSet.add("https://wikipedia.org");
		//seedSet.add("https://goal.com");
	}
	
	
	class crawlerThread implements Runnable {
		private Object lock = new Object();
		public crawlerThread(Object lock)
		{
			this.lock = lock;
		}
		public void run () {
			while(pageIter < numOfPages || visitorPointer < numOfPages)
			{
				String currentWebPageURL = "";
				try {
					
					synchronized(lock)
					{
						System.out.println("current seed = "+seedSet.get(visitorPointer));
						currentWebPageURL = normalizeSiteURL(seedSet.get(visitorPointer));
						visitorPointer++;
					}
					
					String currentPath =  URI.create(currentWebPageURL).getPath();
					String currentHost = URI.create(currentWebPageURL).getScheme()+"://"+
								URI.create(currentWebPageURL).getHost();
					//System.out.println(currentPath);
					//System.out.println(currentHost);
					boolean isValidWebPage = checkRobots(currentHost,currentPath);
					if(isValidWebPage)
					{
						Document doc = Jsoup.connect(currentWebPageURL).get();
						Elements links = doc.body().getElementsByTag("a");
						Elements images = doc.body().getElementsByTag("img");
						
						Elements headingElements = doc.body().getElementsByTag("h1");
						Elements paragraphElements = doc.body().getElementsByTag("p");
						//Elements headOneText = doc.body().getElementsByTag("h1");
						//Elements headTwoText = doc.body().getElementsByTag("h2");
						
						
						String imageSources = "";
						String headingText = "";
						String paragraphText = "";
						List<String> webPageLinks = new ArrayList<String>();
						for(Element link : links)
						{
							if(webPageLinks.size() >= webPageLinksThreshold)
								break;
							String href = link.attr("href");
							if(href.startsWith("/"))
							{
								if(href.indexOf("http") != 0 || href.indexOf("https") != 0)
									href = currentHost + href;
								if(uniqueLink(seedSet, href) && uniqueLink(webPageLinks, href))
								{
									//check if the web page is found or not
									boolean checkExistance = urlExist(href);
									if(checkExistance)
									{
										//check if this webpage exists or not
										//seedSet.add(href);
										webPageLinks.add(href);
										System.out.println("href = "+href);
									}
									
								}
							}
						
						}
						for(Element image : images)
						{
							String imgSrc = image.attr("src");
							System.out.println("ImgSrc = "+imgSrc);
							if(imageExist(imgSrc))
							{
								imageSources += imgSrc + " ";
							}
							else if(imageExist("https:"+imgSrc))
							{
								imageSources += "https:" + imgSrc + " ";
							}
							else if(imageExist(currentHost+imgSrc))
							{
								imageSources += currentHost + imgSrc + " ";
							}
						}
						for(Element hElement : headingElements)
						{
							headingText += hElement.text() + "^h1^";
						}
						for(Element pElement : paragraphElements)
						{
							paragraphText += pElement.text() + "^p^";
						}
						synchronized(lock)
						{
							for(int k = 0;k < webPageLinks.size() && pageIter < numOfPages;k++)
							{
								pageIter++;
								seedSet.add(webPageLinks.get(k));
							}
							imagesOfSeedSet.add(imageSources);
							System.out.println("seedSet after i = "+pageIter);
							System.out.println("site host = "+currentWebPageURL);
							System.out.println(imageSources);
							System.out.println(headingText+paragraphText);
							System.out.println(""); // publish date
							MySQLAccess db = new MySQLAccess();
							try {
								db.writeResultSet(currentWebPageURL, headingText+paragraphText, imageSources, "");
							} catch (SQLException e) {
								System.out.println("problem occured on writing in the database");
							}
							
						}
					
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	
	
	private static boolean imageExist(String href) {
		boolean exists = true;
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
}

