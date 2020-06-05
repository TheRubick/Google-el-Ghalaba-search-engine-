package com.example.demo;

//import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.io.*;


public class QueryProcessor {
    String query;
    ArrayList<String> parts;

    public QueryProcessor(String query) {
        this.query = query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<String> startProcessing() throws IOException {
        parts = new ArrayList<>(Arrays.asList(query.split(" ")));
        printQueryWordsCount();
        removeWhiteSpaces();
        lowerCaseWords();
        removeStopWords();
        System.out.println("after removal");
        printQueryWordsCount();
//        stemQuery();
        return parts;
    }

    public void printOutputToConsole() {
        for (String part : parts) {
            System.out.println("received : " + part + "     " + part.length());
        }
    }

    private void removeWhiteSpaces() {
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, parts.get(i).replaceAll("\\s+", ""));
        }
    }

    private void lowerCaseWords() {
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, parts.get(i).toLowerCase());
        }
    }

    private void printQueryWordsCount() {
        System.out.println(parts.size());
    }

    private void removeStopWords() throws IOException {
        //String[] stopWords = {"a", "and", "or", "is"}; //to be continued
        ArrayList<String> stopWords = new ArrayList<>();
        File file = new File(".\\stopping_words.txt");

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;
        while ((st = br.readLine()) != null) {
            //System.out.println(st);
            stopWords.add(st);
        }
        for (Iterator<String> iterator = parts.iterator(); iterator.hasNext(); ) {
            String part = iterator.next();
            //System.out.println("word:" + part);
            for (String stopWord : stopWords) {
                if (part.equalsIgnoreCase(stopWord)) {
                    //System.out.println("removed:" + part);
                    iterator.remove();
                    break;
                }
            }
        }
    }

//    private void stemQuery() {
//        for (int i = 0; i < parts.size(); i++) {
//            PorterStemmer stemmer = new PorterStemmer();
//            stemmer.setCurrent(parts.get(i)); //set string you need to stem
//            stemmer.stem();  //stem the word
//            parts.set(i, stemmer.getCurrent());
//        }
//    }

}
