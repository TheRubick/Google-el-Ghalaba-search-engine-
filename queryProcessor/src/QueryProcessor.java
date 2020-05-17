import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class QueryProcessor {
    String query;
    String type;
    ArrayList<String> parts;

    public QueryProcessor() {
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public ArrayList<String> startProcessing() {
        parts = new ArrayList<>(Arrays.asList(query.split("%")));
        printQueryWordsCount();
        removeStopWords();
        System.out.println("after removal");
        printQueryWordsCount();
        stemQuery();
        return parts;
    }

    public void getRequest() {
        String query = "how%to%send%a%travelling%in%get%and%travel%in%android%OR%traveler";
        String type = "web";
        //TODO :: get request from server
        setType(type);
        setQuery(query);
    }

    public void printOutputToConsole() {
        for (String part : parts) {
            System.out.println("received : " + part);
        }
    }

    private void printQueryWordsCount() {
        System.out.println(parts.size());
    }

    private void removeStopWords() {
        String[] stopWords = {"a", "and", "or", "is"}; //to be continued
        for (Iterator<String> iterator = parts.iterator(); iterator.hasNext(); ) {
            String part = iterator.next();
            for (String stopWord : stopWords) {
                if (part.equalsIgnoreCase(stopWord)) {
                    iterator.remove();
                }
            }
        }
    }

    private void stemQuery() {
        for (int i = 0; i < parts.size(); i++) {
            PorterStemmer stemmer = new PorterStemmer();
            stemmer.setCurrent(parts.get(i)); //set string you need to stem
            stemmer.stem();  //stem the word
            parts.set(i, stemmer.getCurrent());
        }
    }

}
