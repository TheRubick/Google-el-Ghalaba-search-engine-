package com.example.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class personDetector {
    private List<String> text;
    public static void main(String [] args) throws IOException, SQLException {
        System.out.println("hello");
        ArrayList<String>dummy = new ArrayList<String>();
        dummy.add("smith");
        //dummy.add("Ahmed");
        dummy.add("smith");
        dummy.add("+");
        dummy.add("Mike");

        personDetector obj = new personDetector(dummy,"EG");
    }
    public personDetector(ArrayList<String> inputText,String countryName) throws IOException, SQLException {

        String sentence = "";
        for(String word : inputText)
        {
            //to normalize the word
            word = word.toLowerCase();
            //make the first letter of the word uppercase capital
            word = word.substring(0,1).toUpperCase() + word.substring(1);
            sentence += word + " ";
        }

        //tokenizing phase
        InputStream inputStreamTokenizer = new FileInputStream("D:\\College\\3rd Year\\2nd Term\\APT\\Project" +
                "\\Search_Engine\\openNLP bin\\en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);
        TokenizerME tokenizer = new TokenizerME(tokenModel);

        //finding person name phase
        InputStream inputStream = new
                FileInputStream("D:\\College\\3rd Year\\2nd Term\\APT\\Project\\Search_Engine" +
                "\\openNLP bin\\en-ner-person.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStream);

        //Instantiating the NameFinder class
        NameFinderME nameFinder = new NameFinderME(model);
        String [] tokens = tokenizer.tokenize(sentence);
        Span nameSpans[] = nameFinder.find(tokens);


        //create database object to write in the trends table
        MySQLAccess dbManager = new MySQLAccess();
        int iter = 0;
        //Printing the spans of the names in the sentence
        for(Span s: nameSpans)
        {
            System.out.println(s.toString()+"  "+tokens[s.getEnd()-1]);
            System.out.println(tokens[s.getStart()]);
            int nameLen = s.length();
            String personName = "";
            for(int i = 0;i < nameLen;i++)
                personName += tokens[s.getStart()+i] + " ";

            System.out.println("person name is "+personName);
            //writing in the database
            dbManager.writePersonName(countryName,personName);
        }


    }
}
