
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
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
        MySQLAccess dbManager = new MySQLAccess();
        dbManager.countQuery(query);
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<String> queryWords = queryProcessor.startProcessing();
        personDetector personDetectorObj = new personDetector(queryWords,CountryDomain);
        OverAllRank ranker = new OverAllRank();
        Link[] links = ranker.startRank(queryWords, CountryDomain);
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
        MySQLAccess dbManager = new MySQLAccess();
        dbManager.countQuery(query);
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<String> queryWords = queryProcessor.startProcessing();
        personDetector personDetectorObj = new personDetector(queryWords,CountryDomain);
        ImageRank ranker = new ImageRank();
        Img[] imgs = ranker.startRank(queryWords);
        return imgs;
    }

    /*************************************end point 3*******************************************************/
    @GetMapping("/complete")
    public String[] getSuggestions(
            @RequestParam(value = "part", defaultValue = " ") String part) throws SQLException {

        int maxNum = 10;
        String[] suggestions = new String[maxNum];

        MySQLAccess dbManager = new MySQLAccess();
        ResultSet queryResult = dbManager.getAutoComplete(part);
        int i =0;
        while(queryResult.next() && i<maxNum)
        {
            suggestions[i]=queryResult.getString(1);
            i++;
        }
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
        String query = "SELECT person_name,count(person_name) as person_occurrence from trends_table WHERE " +
                "country = \""+CountryDomain+"\""+
            "GROUP by person_name order by person_occurrence DESC";
        try {
            trendsData = dbManager.readDataBase(query);

            for (int row = 1; row <= trendsCount; row++) {
                if (trendsData.absolute(row)) {
                    trends[row - 1] = new Trend(trendsData.getString(1), trendsData.getInt(2));
                } else
                    trends[row - 1] = new Trend("N/A", 0);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return trends;
    }

    /*************************************end point 5*******************************************************/
    @PutMapping("/personalized")
    public void updatePersonalized(@RequestParam(value = "link", defaultValue = " ") String link) throws SQLException {
        //TODO :: add link to data base or increase its count
        MySQLAccess dbManager = new MySQLAccess();
        dbManager.countLink(link);
        System.out.println("personalized request received");
    }
}
            