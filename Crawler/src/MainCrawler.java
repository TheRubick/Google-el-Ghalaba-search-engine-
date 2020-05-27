// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
public class MainCrawler {
  public static void main(String[] args) throws Exception {
    
	  MySQLAccess dao = new MySQLAccess();
    //dao.readDataBase("select * from crawler_table");
    //dao.writeResultSet("sdfsdfsdf", "sdfdsf", "sdfsdf", "sdfsf","dfsdf");
	  System.out.println(dao.isEmptyCrawler());
	  /*
	 String x = "hello@@::;;@@world ; here we test the delimiter of the @rr@@::;;@@ and it works";
	 String [] arr = x.split("@@::;;@@");
	 System.out.println(arr[0]);
	 System.out.println(arr.length);
	 */
  }

}