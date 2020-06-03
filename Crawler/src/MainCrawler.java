// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class MainCrawler {
  public static void main(String[] args) throws Exception,SocketException {

    File myObj;
    try {
      myObj = new File("filename.txt");
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      } else {
        System.out.println("File already exists.");
      }

      if(myObj.delete())
      {
        System.out.println("File deleted successfully");
      }
      else
      {
        System.out.println("Failed to delete the file");
      }
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }



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