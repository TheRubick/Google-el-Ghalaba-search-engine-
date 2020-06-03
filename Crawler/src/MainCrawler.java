// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.sql.ResultSet;
import java.util.Scanner;

public class MainCrawler {
  public static void main(String[] args) throws Exception,SocketException {


    MySQLAccess db = new MySQLAccess();
    ResultSet databaseFetchedLinks = db.readDataBase("SELECT Link FROM `crawler_table`");
    while (databaseFetchedLinks.next())
    {
        String data = databaseFetchedLinks.getString(1);
        System.out.println(data);
    }
    /*
    File myObj;
    try {
      myObj = new File("takenLinks.txt");
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      } else {
        System.out.println("File already exists.");
      }
      myObj.delete();


      *FileWriter myWriter = new FileWriter("filename.txt");
      myWriter.append("Files in Java might be tricky, but it is fun enough!\n");
      myWriter.append("sdfsdfsdf\n");
      myWriter.close();
      System.out.println("Successfully wrote to the file.");
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        if(!data.isEmpty())
          System.out.println(data.length());
      }
      myReader.close();



    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    */


      /*


      try{

          URL url = new URL("https://www.tutorialspoint.com/java/index.htm");
          HttpURLConnection connection = (HttpURLConnection)url.openConnection();
          connection.setRequestMethod("HEAD");
          connection.connect();

          code = connection.getResponseCode();
      }catch (UnknownHostException e)
      {
          code = -1;
        //e.printStackTrace();
      }
      catch(SocketException s){
        //s.printStackTrace();
        code = -1;
      }

      System.out.println(code);
      long endTime = System.nanoTime();
      long duration = endTime - startTime;
      System.out.println(duration*Math.pow(10,-9));
      */
      //Document doc = Jsoup.connect("https://dynaimage.cdn.cnn.com/cnn/digital-images/org/ef10cfad-99be-4dd4-9123-068ef562bd62.jpg").get();

      //String title = doc.getElementsByTag("title").text();
      //System.out.println(title);
      /*
      MySQLAccess dao = new MySQLAccess();
      dao.readDataBase("select * from crawler_table");
      //dao.writeResultSet("sdfsdfsdf", "sdfdsf", "sdfsdf", "sdfsf","dfsdf");
	  System.out.println(dao.isEmptyCrawler());
	  */

  }

}