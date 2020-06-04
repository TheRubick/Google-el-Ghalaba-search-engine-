package com.example.demo;


import java.sql.ResultSet;
import java.util.ArrayList;

public class ImageRank  {
    private String words;
    private MySQLAccess db;
    private int numImgs;
    ServerAPI.Img[] startRank(ArrayList<String> queryProcessed) throws Exception
    {
        db = new MySQLAccess();
        words = "(";
        for (int i = 0; i < queryProcessed.size() - 1; i++) {
            words += "'" + queryProcessed.get(i) + "',";
        }
        words += "'" + queryProcessed.get(queryProcessed.size() - 1) + "')";
        String query = "SELECT DISTINCT(`img_url`), `word` FROM `img_word` WHERE WORD IN " + words;

        ResultSet queryResult = db.readDataBase(query);

        queryResult.last();
        numImgs =  queryResult.getRow();
        query = "SELECT DISTINCT(`img_url`), `word` FROM `img_word` WHERE ";
        for(int i=0;i<queryProcessed.size()-1;i++)
        {
            query += "(word LIKE '%" + queryProcessed.get(i) +"%' AND WORD <>  '"
                    +queryProcessed.get(i)+ "') OR ";
        }
        query += "(word LIKE '%" + queryProcessed.get(queryProcessed.size() - 1) +"%' AND WORD <>  '"
                +queryProcessed.get(queryProcessed.size() - 1)+"');";
        ResultSet queryResult2 = db.readDataBase(query);
        queryResult2.last();
        numImgs += queryResult2.getRow();

        ServerAPI.Img[] toFrontEnd = new ServerAPI.Img[numImgs];
        queryResult.beforeFirst();
        int i =0;
        while (queryResult.next()) {
            String link = queryResult.getString(1);
            String title= queryResult.getString(2);
            ServerAPI.Img element = new ServerAPI.Img(title,link);
            toFrontEnd[i] = element ;
            i++;
        }
        queryResult2.beforeFirst();
        while (queryResult2.next()) {
            String link = queryResult2.getString(1);
            String title= queryResult2.getString(2);
            ServerAPI.Img element = new ServerAPI.Img(title,link);
            toFrontEnd[i] = element ;
            i++;
        }
        return toFrontEnd;
    }
}

