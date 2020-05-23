// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
public class MainCrawler {
  public static void main(String[] args) throws Exception {
    MySQLAccess dao = new MySQLAccess();
    dao.readDataBase();
    dao.writeResultSet("sdfsdfsdf", "sdfdsf", "sdfsdf", "sdfsf","dfsdf");
  }

}