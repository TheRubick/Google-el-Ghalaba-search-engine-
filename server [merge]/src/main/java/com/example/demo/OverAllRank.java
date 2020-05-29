package com.example.demo;

import java.util.*;
import java.sql.ResultSet;

public class OverAllRank{

    private String words;
    private MySQLAccess db;
    private HashMap<String, Double> finalScore;
    private double relvWeight;
    private double popWeight;
    private double LocWeight;
    private double persWeight;
    private int numLinks;

    // function to sort hashmap by values
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list =
                new LinkedList<Map.Entry<String, Double> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public ServerAPI.Link[] startRank(ArrayList<String> queryProcessed ,String Loc) throws Exception
    {
        relvWeight = 1.0;
        popWeight = 0.5;
        LocWeight = 0.8;
        persWeight = 0.2;
        int num = queryProcessed.size();
        words = "(";
        for (int i = 0; i < queryProcessed.size() - 1; i++) {
            words += "'" + queryProcessed.get(i) + "',";
        }
        words += "'" + queryProcessed.get(queryProcessed.size() - 1) + "')";
        ////////////////////////////////////////////////////////////////
        // We get relevance rank
        Relevance relv = new Relevance(words,num);
        HashMap<String,Double>relvRank = relv.startRelevance();

        // add relevance to over all rank
        // get names of concerning links
        StringBuilder concernedLink = new StringBuilder();
        concernedLink.append("(");
        relvRank.forEach((key, value) -> {
            finalScore.put(key,relvWeight *relvRank.get(key));
            concernedLink.append("'"+key+"',");
        });
        // used in querying database
        String concernedLinks = concernedLink.toString();
        if(concernedLinks.equals("("))
            concernedLinks = "''";
        else
            concernedLinks = concernedLinks.substring(0, concernedLinks.length() - 1);
        concernedLinks += ")";

        numLinks = relvRank.size();

        ////////////////////////////////////////////////////////////////
        // We get popularity rank
        String query = "SELECT LINK,POPULARITY_SCORE FROM `POPULARITY_RANK` where LINK IN " + concernedLinks;
        ResultSet  queryResult = db.readDataBase(query);

        // add popularity to over all rank
        while (queryResult.next()) {
            String link = queryResult.getString(1);
            double score = queryResult.getDouble(2);
            finalScore.put(link, finalScore.get(link) + popWeight*score);
        }
        ////////////////////////////////////////////////////////////////
        // We get personalized rank
        query = "SELECT LINK, COUNT_CLICKS/ (SELECT SUM(COUNT_CLICKS) FROM SITES_CLICKS) " +
                "FROM SITES_CLICKS  WHERE LINK IN "+ concernedLinks;
        queryResult = db.readDataBase(query);

        // add personalized rank to over all rank
        while (queryResult.next()) {
            String link = queryResult.getString(1);
            double score = queryResult.getDouble(2);
            finalScore.put(link, finalScore.get(link) +persWeight *score);
        }
        ////////////////////////////////////////////////////////////////
        // We get geographic rank
        query = "SELECT LINK FROM `crawler_table` where Country = " + Loc;
        queryResult = db.readDataBase(query);

        //add LocWeight to over all rank
        while (queryResult.next()) {
            String link = queryResult.getString(1);
            double score = queryResult.getDouble(2);
            finalScore.put(link, finalScore.get(link)+ LocWeight *score);
        }
        ////////////////////////////////////////////////////////////////
        // sort the final score
        finalScore = sortByValue(finalScore);

        StringBuilder sortedLinks = new StringBuilder();

        // build string to query links,title and snippets of the ranked links
        sortedLinks.append("(");
        relvRank.forEach((key, value) -> {
            sortedLinks.append("'"+key+"',");
            finalScore.get(key);
        });
        String dString = sortedLinks.toString();
        if(dString.equals("("))
            dString = "'')";
        else
            dString = dString.substring(0, dString.length() - 1);
        dString += ")";


        query = "SELECT TITLE, LINK, SNIPPETS FROM `crawler_table` WHERE LINK IN " +dString;
        queryResult = db.readDataBase(query);

        // build Link class to send it to the frontend
        ServerAPI.Link[] toFrontEnd = new ServerAPI.Link[numLinks];
        int i=0;
        while (queryResult.next()) {
            String title = queryResult.getString(1);
            String link= queryResult.getString(2);
            String snippet = queryResult.getString(3);
            toFrontEnd[i].link = link;
            toFrontEnd[i].title = title;
            toFrontEnd[i].snippet =snippet;
            i++;
        }
        return toFrontEnd;
    }
}
