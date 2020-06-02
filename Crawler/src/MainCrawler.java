// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.*;

import java.net.*;

public class MainCrawler {
  public static void main(String[] args) throws Exception,SocketException {

      long startTime = System.nanoTime();
      int code = 0;

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