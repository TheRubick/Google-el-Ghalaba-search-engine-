import java.awt.Image;
import java.io.*;
import java.net.*;
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
	
	public static List<String> seedSet = new ArrayList<String>();
	static int numOfPages = 10;
	
	//initialize the seedSet
	private static void initializeSeed()
	{
		seedSet.add("https://en.wikipedia.org/wiki/Mohamed_Salah");
		//seedSet.add("https://cnn.com");
		//seedSet.add("https://bbc.com");
		//seedSet.add("https://wikipedia.org");
		//seedSet.add("https://goal.com");
	}
	
	public static void main(String[] args) {
		
		initializeSeed();
		for(int i = 0;i < numOfPages;)
		{
			try {
				
				//URL url = new URL("https://www.google.com");
				
				//normalizeSiteURL("http://example.com/%7Efoo");
				System.out.println("current seed = "+seedSet.get(i));
				String currentWebPageURL = normalizeSiteURL(seedSet.get(i));
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
					int checkDannyrose = doc.body().html().indexOf("https://ichef.bbc.co.uk/wwhp/624/cpsprodpb/10BF1/production/_112239586_dannyrose.jpg");
					
					String imageSources = "";
					for(Element link : links)
					{
						String href = link.attr("href");
						if(href.startsWith("/"))
						{
							if(href.indexOf("http") != 0 || href.indexOf("https") != 0)
								href = currentHost + href;
							if(uniqueLink(seedSet, href))
							{
								if(i < numOfPages)
								{
									//check if the web page is found or not
									boolean checkExistance = urlExist(href);
									if(checkExistance)
									{
										//check if this webpage exists or not
										i++;
										seedSet.add(href);
										System.out.println("href = "+href);
									}
									
								}
								
							}
						}
						
						
						
						//System.out.println(href);
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
					System.out.println("seedSet after i = "+i);
					System.out.println(imageSources);
					//for(int j = 0;j < seedSet.size();j++)
						//System.out.println(seedSet.get(j));
				
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

			
	}
	
	private static boolean urlExist(String href) {
		boolean exists = true;
		try {
			Jsoup.connect(href).get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Page isn't found");
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
			System.out.println("Image isn't found");
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
		
		if(path.indexOf("index") != -1 || path.indexOf("default") != -1 || path.equals(""))
			path = "/";
		String normalizedURL = protocol+"://"+host+path+Query;
		System.out.println(normalizedURL);
		return normalizedURL;
	}
	
	private static boolean uniqueLink(List<String>seedSet,String link)
	{
		boolean isUnique = true;
		//System.out.println("link = "+link);
		if(seedSet.size() == 1)
		{
			System.out.println("Yes = "+seedSet.get(0));
		}
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
			Document doc = Jsoup.connect(currentHost+"/robots.txt").get();
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

