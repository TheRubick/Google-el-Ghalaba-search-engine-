package com.example.demo;

import java.lang.Math;
import java.sql.ResultSet;
import java.util.*;


public class Relevance {

//	private ArrayList<String> queryProcessed;
	private double[][] tfIdf;  //  dimension : (terms,doc)
	private int numTerms;
	private int numDocs;
	private int totalDocs;
	// used in queries;
	private String words;
	private MySQLAccess db;

	private HashMap<String, Integer> hashTableURLs;
	private HashMap<String, Integer> hashTableWords;
	private double RelevanceScore[];

	public Relevance(String Words,int num) throws Exception {
		numTerms = num;
		words = Words;
//		queryProcessed = qp;
//		numTerms = queryProcessed.size();
		db = new MySQLAccess();
//		words = "(";
//		for (int i = 0; i < queryProcessed.size() - 1; i++) {
//			words += "'" + queryProcessed.get(i) + "',";
//		}
//		words += "'" + queryProcessed.get(queryProcessed.size() - 1) + "')";
		setTotalDocs();
		setNumDocs();

	}

	private void setTotalDocs() throws Exception {
		String query = "SELECT COUNT(DISTINCT(url)) FROM `words_url`";
		ResultSet resultSet = db.readDataBase(query);
		resultSet.next();

		totalDocs = resultSet.getInt(1);
	}

	private void setNumDocs() throws Exception {
		String query = "SELECT COUNT(DISTINCT(url)) FROM `words_url` where word IN " + words;
		ResultSet resultSet = db.readDataBase(query);
		resultSet.next();
		numDocs = resultSet.getInt(1);
	}

	private ResultSet queryTermFreq() throws Exception {
		String query = "SELECT URL,word,score FROM `words_url` where word IN " + words;
		return db.readDataBase(query);
	}

	private ResultSet queryDocFreq() throws Exception {
		String query = "SELECT word,COUNT(DISTINCT(url)) FROM `words_url` where word IN " + words + " GROUP by word";
		return db.readDataBase(query);
	}

	// calculates tf_idf and return a hashmap of links withs its score
	public HashMap<String,Double> startRelevance() throws Exception {
		tfIdf = new double[numTerms][numDocs];
		hashTableURLs = new HashMap<String, Integer>();
		hashTableWords = new HashMap<String, Integer>();
		ResultSet tf = queryTermFreq();
		ResultSet df = queryDocFreq();

		int indURL = 0;
		int indWord = 0;
		// fill the matrix with TF	
		while (tf.next()) {
			// check if url is in the hashTable to give it a new index if not.
			String url = tf.getString(1);
			if (!hashTableURLs.containsKey(url)) {
				hashTableURLs.put(url, indURL);
				indURL++;
			}
			String word = tf.getString(2);
			if (!hashTableWords.containsKey(word)) {
				hashTableWords.put(word, indWord);
				indWord++;
			}
			tfIdf[hashTableWords.get(word)][hashTableURLs.get(url)] = tf.getDouble(3);
		}

		// fill the matrix with IDF	
		while (df.next()) {
			String word = df.getString(1);
			for (int i = 0; i < numDocs; i++) {
				tfIdf[hashTableWords.get(word)][i] *= (1 + Math.log10((double) (totalDocs) / df.getInt(2)));
			}
		}

		RelevanceScore = new double[numDocs];
		hashTableURLs.forEach((key, value) -> {
			RelevanceScore[value] = 0;
			for (int j = 0; j < numTerms; j++) {
				RelevanceScore[value] += tfIdf[j][value];
			}
		});

		db = new MySQLAccess();
		HashMap<String,Double> retRelevance = new HashMap<String,Double>();
		hashTableURLs.forEach((key, value) -> {
			retRelevance.put(key,RelevanceScore[value]);
		});
		return retRelevance;
	}
}
