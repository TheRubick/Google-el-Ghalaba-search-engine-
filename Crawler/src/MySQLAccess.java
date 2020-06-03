// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MySQLAccess {
	
  private Connection connect = null;
  private Statement statement = null;
  private PreparedStatement preparedStatement = null;
  private ResultSet resultSet = null;

  final private String host = config.host;
  final private String user = config.user;
  final private String passwd = config.passwd;
  
  public MySQLAccess() {
	  try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      
	      // Setup the connection with the DB
	      connect = DriverManager.getConnection("jdbc:mysql://" + host + "/crawler_database?" + "user=" + user + "&password=" + passwd );
	      
	  }
	  catch (Exception e) {} 
  }
  
  public ResultSet readDataBase(String s) throws Exception {
    try {
      //reading from the crawler database
      statement = connect.createStatement();
      ResultSet resultSet = statement.executeQuery(s);
      return resultSet;
//      while(resultSet.next())
//      {
//    	  System.out.println(resultSet.getString(1)+" "+resultSet.getString(2));
//      }
      //select the first row
//      resultSet.absolute(10);
//      System.out.println(resultSet.getString(1)); // kda ana hatb3 el link ely fe row one
      
      
      //select the third row
//      resultSet.absolute(3);
      
//      System.out.println(resultSet.getString(3)); // kda ana hatb3 el image sources ely fel third row
      
    } catch (Exception e) {
        throw e;
      }
  } 
      //writeResultSet(resultSet);

      /*
      // PreparedStatements can use variables and are more efficient
      preparedStatement = connect
          .prepareStatement("insert into  feedback.comments values (default, ?, ?, ?, ? , ?, ?)");
      // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
      // Parameters start with 1
      preparedStatement.setString(1, "Test");
      preparedStatement.setString(2, "TestEmail");
      preparedStatement.setString(3, "TestWebpage");
      preparedStatement.setDate(4, new java.sql.Date(2009, 12, 11));
      preparedStatement.setString(5, "TestSummary");
      preparedStatement.setString(6, "TestComment");
      preparedStatement.executeUpdate();

      preparedStatement = connect
          .prepareStatement("SELECT myuser, webpage, datum, summary, COMMENTS from feedback.comments");
      resultSet = preparedStatement.executeQuery();
      writeResultSet(resultSet);

      // Remove again the insert comment
      preparedStatement = connect
      .prepareStatement("delete from feedback.comments where myuser= ? ; ");
      preparedStatement.setString(1, "Test");
      preparedStatement.executeUpdate();
      
      resultSet = statement
      .executeQuery("select * from feedback.comments");
      writeMetaData(resultSet);
      
    

  }

  private void writeMetaData(ResultSet resultSet) throws SQLException {
    //   Now get some metadata from the database
    // Result set get the result of the SQL query
    
    System.out.println("The columns in the table are: ");
    
    System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
    for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
      System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
    }
  }
  */


  public ArrayList<String>[] getbylastupdate(Timestamp ts) {

	  ArrayList<String> [] arr = new ArrayList[3];
	  try {
	      String s = "select * from crawler_table where last_update >= '" + ts +"';";
	      statement = connect.createStatement();
	      resultSet = statement.executeQuery(s);
	      System.out.println(resultSet);
		    resultSet.beforeFirst();
		    arr[0] = new ArrayList<String>();
		    arr[1] = new ArrayList<String>();
		    arr[2] = new ArrayList<String>();
		    while(resultSet.next()) {
		    	arr[0].add(resultSet.getString("Link"));
		    	arr[1].add(resultSet.getString("Text"));
		    	arr[2].add(resultSet.getString("Image_links"));
		    }
	      
	    } catch (Exception e) {
	      }
	return arr;
  }
  
  public void delbyUrl(String url) {
	  try {
	      String s = "delete from word_url where url = '" + url +"';";
	      statement = connect.createStatement();
	      statement.executeUpdate(s);
	      
	    } catch (Exception e) {
	      }
  }
  

  public void insertWordUrl(String url, HashMap<String, Double > wordScore) {
	  try {
	      String s = "INSERT INTO word_url VALUES " ;
	      for (HashMap.Entry<String, Double> entry : wordScore.entrySet()) {
	    		 s = s.concat("( '" + entry.getKey() +"','" + url +"'," + entry.getValue() +"),");
	      }
	      s = s.substring(0,s.length()-1);
	      s = s.concat(";"); 
	      statement = connect.createStatement();
	      statement.executeUpdate(s);
	      
	    } catch (Exception e) {
	      }
  }

  public void insertImages(HashMap<String, Integer > Imgs) {
	  try {
	      String s = "INSERT IGNORE INTO word_url VALUES " ;
	      for (HashMap.Entry<String, Integer> entry : Imgs.entrySet()) {
	    	  String[] arr = entry.getKey().split("@@::;;@@;", -2);
	    		s = s.concat("( '" + arr[0] +"','" + arr[1]+"),");
	      }
	      s = s.substring(0,s.length()-1);
	      s = s.concat(";"); 
	      statement = connect.createStatement();
	      statement.executeUpdate(s);
	      
	    } catch (Exception e) {
	      }
  }
  
  public boolean isEmptyCrawler() throws ClassNotFoundException, SQLException
  {
      //reading from the crawler database
      statement = connect.createStatement();
      ResultSet resultSet = statement.executeQuery("select * from crawler_table");
      return !(resultSet.absolute(1));
  }
  public void writeResultSet(String link , String text , String image_sources, String title, String refererLink, String URLLocation) throws SQLException {
	  //Connection connect = null;
	  PreparedStatement preparedStatement = null;
	  try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://" + host + "/Crawler_database?"
	              + "user=" + user + "&password=" + passwd );
	    } catch (Exception e) {
	  		e.printStackTrace();
	        System.out.println("database error");
	      } finally {
	        //close();
	      }
	  preparedStatement = connect.prepareStatement("INSERT INTO crawler_database.crawler_table VALUES (?,?,?,?,?,?,DEFAULT)");
      preparedStatement.setString(1, link);
      preparedStatement.setString(2, text);
      preparedStatement.setString(3, image_sources);
      preparedStatement.setString(4, title); // should be fixed
      preparedStatement.setString(5, refererLink);
	  preparedStatement.setString(6, URLLocation);
      preparedStatement.executeUpdate();
      preparedStatement.close();
    }
  public void saveRank(String values) throws SQLException {
	  Connection connect = null;
	  PreparedStatement preparedStatement = null;
	  try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://" + host + "/Crawler_database?"
	              + "user=" + user + "&password=" + passwd );
	    } catch (Exception e) {
	        System.out.println("database error");
	      } finally {
	        close();
	      }
	  statement = connect.createStatement();
	  statement.executeUpdate("DROP TABLE IF EXISTS POPULARITY_RANK;");
	  statement.executeUpdate("CREATE TABLE POPULARITY_RANK(LINK VARCHAR(256) NOT NULL PRIMARY KEY, POPULARITY_SCORE DOUBLE NOT NULL);");
	  
	  preparedStatement = connect.prepareStatement("INSERT INTO POPULARITY_RANK(LINK,POPULARITY_SCORE) VALUES "+values);
//      preparedStatement.setString(1, values);
      preparedStatement.executeUpdate();
//      preparedStatement.close();
//      connect.close();
    }
  
  public void saveRankRelevance(String values) throws SQLException {
	  Connection connect = null;
	  PreparedStatement preparedStatement = null;
	  try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://" + host + "/Crawler_database?"
	              + "user=" + user + "&password=" + passwd );
	    } catch (Exception e) {
	        System.out.println("database error");
	      } finally {
	        close();
	      }
	  statement = connect.createStatement();
	  statement.executeUpdate("DROP TABLE IF EXISTS RELEVANCE_RANK;");
	  statement.executeUpdate("CREATE TABLE RELEVANCE_RANK(LINK VARCHAR(256) NOT NULL PRIMARY KEY, RELEVANCE_SCORE DOUBLE NOT NULL);");
	  
	  preparedStatement = connect.prepareStatement("INSERT INTO RELEVANCE_RANK(LINK,RELEVANCE_SCORE) VALUES "+values);
      preparedStatement.executeUpdate();

    }
  
  
  // You need to close the resultSet
  public void close() {
    try {
    	
      if (resultSet != null) {
        resultSet.close();
      }

      if (preparedStatement != null) {
    	  preparedStatement.close();
        }
      
      if (statement != null) {
        statement.close();
      }
		
      if (connect != null) {
        connect.close();
      }
    } catch (Exception e) {
    	System.out.println("error occured on closing one of the instances");
    }
  }
  
 /*
 public void writeMetaData(ResultSet resultSet) throws SQLException {
	    //   Now get some metadata from the database
	    // Result set get the result of the SQL query
	    
	    System.out.println("The columns in the table are: ");
	    
	    System.out.println("Table: " + resultSet.getMetaData().getTableName(1));
	    for  (int i = 1; i<= resultSet.getMetaData().getColumnCount(); i++){
	      System.out.println("Column " +i  + " "+ resultSet.getMetaData().getColumnName(i));
	    }
	  }
 */
  /*
  // Statements allow to issue SQL queries to the database
  statement = connect.createStatement();
  // Result set get the result of the SQL query
  resultSet = statement.executeQuery("select * from crawler_database.crawler_table");
 
  //select the first row
  resultSet.absolute(3);
  */
 
  //inserting into the table
  /*
  preparedStatement = connect.prepareStatement("INSERT INTO hotsite.users VALUES (?,?,?)");
  preparedStatement.setInt(1, 2);
  preparedStatement.setString(2, "admin");
  preparedStatement.setString(3, "sfdsdf");
  preparedStatement.executeUpdate();
  System.out.println(resultSet.getString(2));
*/

}