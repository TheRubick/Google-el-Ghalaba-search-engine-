
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.sql.ResultSet;
@SpringBootApplication
@RestController
public class ServerAPI {


    public static void main(String[] args) {
        SpringApplication.run(ServerAPI.class, args);
    }

    /*********************************dummy endpoint*****************************************************/
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    public static class Greeting {

        private final long id;
        private final String content;

        public Greeting(long id, String content) {
            this.id = id;
            this.content = content;
        }

        public long getId() {
            return id;
        }

        public String getContent() {
            return content;
        }
    }

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        //return String.format("Hello %s!", name);
        return new Greeting(counter.incrementAndGet(), String.format(template, name));

    }

    /*********************************end point 1*********************************************************/
    public static class Link {
        String title;
        String link;
        String snippet;

        public Link(String title, String link, String snippet) {
            this.title = title;
            this.link = link;
            this.snippet = snippet;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public String getSnippet() {
            return snippet;
        }
    }

    @GetMapping("/searchLinks")
    public Link[] getLinks(
            @RequestParam(value = "query", defaultValue = "World") String query,
            @RequestParam(value = "CountryDomain", defaultValue = "EG") String CountryDomain) throws Exception {
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<String> queryWords = queryProcessor.startProcessing();
        OverAllRank ranker = new OverAllRank();
        ranker.startRank(queryWords,CountryDomain);
        //Link[] links= rel.getLinksOrdered();
        //return links;

        final int dataMaxSize = 100;
        Link[] links = new Link[dataMaxSize];

        links[0] = new Link("wikipedia", "https://wikipedia.com",
                "Wikipedia is hosted by the Wikimedia Foundation, a non-profit organization that also hosts a range of other projects.");
        links[1] = new Link("google", "https://google.com", "Google LLC is an American multinational technology company that specializes in Internet-related services and products, which include online advertising technologies, a search engine, cloud computing, software, and hardware. It is considered one of the Big Four technology companies alongside Amazon, Apple, and Facebook");

        for (int i = 2; i < dataMaxSize; i++)
            links[i] = new Link(query + (i + 1), "https://facebook.com", "Facebook is an American online social media and social networking service based in Menlo Park,Facebook is an American online social media and social networking service based in Menlo Park,Facebook is an American online social media and social networking service based in Menlo Park California and a flagship service of the namesake company Facebook, Inc.");

        return links;

    }

    /****************************************end point 2*********************************************************/
    public static class Img {
        String title;
        String link;

        public Img(String title, String link) {
            this.title = title;
            this.link = link;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }
    }

    @GetMapping("/searchImages")
    public Img[] getImages(
            @RequestParam(value = "query", defaultValue = "World") String query,
            @RequestParam(value = "CountryDomain", defaultValue = "EG") String CountryDomain) throws Exception {
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<String> queryWords = queryProcessor.startProcessing();
        OverAllRank ranker = new OverAllRank();
        ranker.startRank(queryWords,CountryDomain);
        //Img[] imgs= rel.getImgsOrdered();
        //return imgs;
        final int dataMaxSize = 500;
        Img[] imgs = new Img[dataMaxSize];
        imgs[0] = new Img("img1", "https://i.imgur.com/tGbaZCY.jpg");
        imgs[1] = new Img("img2", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Mohamed_Salah_2018.jpg/200px-Mohamed_Salah_2018.jpg");
        imgs[2] = new Img("img3", "https://i.imgur.com/k0aIIHx.png");
        imgs[3] = new Img("img4", "https://i.imgur.com/F9dYGWA.png");

        for (int i = 4; i < dataMaxSize; i++)
            imgs[i] = new Img(query + (i + 1), "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4a/Mohamed_Salah_2018.jpg/200px-Mohamed_Salah_2018.jpg");

        return imgs;
    }

    /*************************************end point 3*******************************************************/
    @GetMapping("/complete")
    public String[] getSuggestions(
            @RequestParam(value = "part", defaultValue = " ") String part) {
        int randomNum = 50;
        String[] suggestions = new String[randomNum];
        for (int i = 0; i < randomNum; i++)
            suggestions[i] = part + "suggestion " + i;

        return suggestions;
    }

    /*************************************end point 4*******************************************************/
    public static class Trend {
        String name;
        int count;

        public Trend(String name, int count) {
            this.name = name;
            this.count = count;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
    }

    @GetMapping("/trends")
    public Trend[] getTrends(
            @RequestParam(value = "CountryDomain", defaultValue = "EG") String CountryDomain) {
        final int trendsCount = 10;
        Trend[] trends = new Trend[trendsCount];
        MySQLAccess dbManager = new MySQLAccess();
        ResultSet trendsData = null;
        try {
			trendsData = dbManager.readDataBase(
					"SELECT person_name,count(person_name) as person_occurrence from trends_table WHERE country = \"EG\" "
					+ "GROUP by person_name order by person_name DESC"
					);
			for (int row = 1; row <= trendsCount; row++)
	        {
	        	if(trendsData.absolute(row))
	        	{
	        		int personCount = trendsData.getInt(2);
	        		trends[row-1] = new Trend(trendsData.getString(1), personCount);
	        	}
	        	else
	        		trends[row-1] = new Trend("N/A", 0);
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return trends;
    }
}
            