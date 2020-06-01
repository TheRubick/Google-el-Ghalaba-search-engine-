package com.example.demo;

import java.util.ArrayList;

public class imageMain {
    public static void main(String[] args) throws Exception {
        OverAllRank rank = new OverAllRank();
        ArrayList<String> queryProcessed = new ArrayList<String>();
        queryProcessed.add("123");
        queryProcessed.add("pqwerlay");
        queryProcessed.add("cat");
        ServerAPI.Link[] lk = rank.startRank(queryProcessed,"EG");
    }
}
