import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        String query = "how+to+send+a+tRavElling+in+get   +and+  TravEl+in+Andr     oid+OR+traVeler";
        QueryProcessor queryProcessor = new QueryProcessor(query);
        ArrayList<String> parts = queryProcessor.startProcessing();
        queryProcessor.printOutputToConsole();

    }
}
