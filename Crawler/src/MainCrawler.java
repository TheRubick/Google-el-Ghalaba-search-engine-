// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
public class MainCrawler {
  public static void main(String[] args) throws Exception {
    MySQLAccess dao = new MySQLAccess();
    dao.readDataBase("select * from crawler_table");
    dao.writeResultSet("sdfsdfsdf", "sdfdsf", "sdfsdf", "sdfsf","dfsdf");
  }

}