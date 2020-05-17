import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        QueryProcessor queryProcessor = new QueryProcessor();
        queryProcessor.getRequest();
        ArrayList<String> parts = queryProcessor.startProcessing();
        String type = queryProcessor.getType();
        queryProcessor.printOutputToConsole();
    }
}
