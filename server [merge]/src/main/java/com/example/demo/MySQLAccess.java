// Adapted from http://www.vogella.com/tutorials/MySQLJava/article.html
package com.example.demo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MySQLAccess {
	
  private Connection connect = null;
  private Statement statement = null;
  private PreparedStatement preparedStatement = null;
  private ResultSet resultSet = null;

  final private String host = "127.0.0.1";
  final private String user = "root";
  final private String passwd = "";
  
  public ResultSet readDataBase(String s) throws Exception {
    try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.cj.jdbc.Driver");
      
      // Setup the connection with the DB
      connect = DriverManager
          .getConnection("jdbc:mysql://" + host + "/crawler_database?"
              + "user=" + user + "&password=" + passwd );
      
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
      } finally {
//        close();
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
  public boolean isEmptyCrawler() throws ClassNotFoundException, SQLException
  {
	// This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.cj.jdbc.Driver");
      
      // Setup the connection with the DB
      connect = DriverManager
          .getConnection("jdbc:mysql://" + host + "/crawler_database?"
              + "user=" + user + "&password=" + passwd );
      
      //reading from the crawler database
      statement = connect.createStatement();
      ResultSet resultSet = statement.executeQuery("select * from crawler_table");
      if(resultSet.absolute(1))
    	  return true;
      else
    	  return false;
  }
  public void writeResultSet(String link , String text , String image_sources, String publish_date, String refererLink) throws SQLException {
	  Connection connect = null;
	  PreparedStatement preparedStatement = null;
	  try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://" + host + "/crawler_database?"
	              + "user=" + user + "&password=" + passwd );
	    } catch (Exception e) {
	        System.out.println("database error");
	      } finally {
	        close();
	      }
	  preparedStatement = connect.prepareStatement("INSERT INTO crawler_database.crawler_table VALUES (?,?,?,?,?,DEFAULT)");
      preparedStatement.setString(1, link);
      preparedStatement.setString(2, text);
      preparedStatement.setString(3, image_sources);
      preparedStatement.setDate(4, new java.sql.Date(2020-11-27)); // should be fixed
      preparedStatement.setString(5, refererLink);
      preparedStatement.executeUpdate();
      preparedStatement.close();
      connect.close();
    }


  public void createPopRank() throws  SQLException{
      Connection connect = null;
      PreparedStatement preparedStatement = null;
      try {
          // This will load the MySQL driver, each DB has its own driver
          Class.forName("com.mysql.cj.jdbc.Driver");

          // Setup the connection with the DB
          connect = DriverManager
                  .getConnection("jdbc:mysql://" + host + "/crawler_database?"
                          + "user=" + user + "&password=" + passwd );
      } catch (Exception e) {
          System.out.println("database error");
      } finally {
          close();
      }
      statement = connect.createStatement();
	  statement.executeUpdate("DROP TABLE IF EXISTS POPULARITY_RANK;");
	  statement.executeUpdate("CREATE TABLE POPULARITY_RANK(LINK VARCHAR(700) NOT NULL PRIMARY KEY, POPULARITY_SCORE DOUBLE NOT NULL);");

  }
  public void saveRank(String values) throws SQLException {
	  Connection connect = null;
	  PreparedStatement preparedStatement = null;
	  try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.cj.jdbc.Driver");
	      
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://" + host + "/crawler_database?"
	              + "user=" + user + "&password=" + passwd );
	    } catch (Exception e) {
	        System.out.println("database error");
	      } finally {
	        close();
	      }
	  statement = connect.createStatement();
//	  statement.executeUpdate("DROP TABLE IF EXISTS POPULARITY_RANK;");
//	  statement.executeUpdate("CREATE TABLE POPULARITY_RANK(LINK VARCHAR(700) NOT NULL PRIMARY KEY, POPULARITY_SCORE DOUBLE NOT NULL);");
	  
	  preparedStatement = connect.prepareStatement("INSERT IGNORE INTO POPULARITY_RANK(LINK,POPULARITY_SCORE) VALUES "+values);
//      preparedStatement.setString(1, values);
      preparedStatement.executeUpdate();
//      preparedStatement.close();
//      connect.close();
    }

    public void countLink(String link) throws SQLException {
        Connection connect = null;
        PreparedStatement preparedStatement = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + host + "/crawler_database?"
                            + "user=" + user + "&password=" + passwd );
        } catch (Exception e) {
            System.out.println("database error");
        } finally {
            close();
        }
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS SITES_CLICKS(LINK VARCHAR(700) NOT NULL PRIMARY KEY, COUNT_CLICKS INT NOT NULL);");
        preparedStatement = connect.prepareStatement("INSERT INTO SITES_CLICKS (LINK,COUNT_CLICKS) VALUES ('"+link+"',0) ON DUPLICATE KEY UPDATE COUNT_CLICKS = COUNT_CLICKS +1 ");
        preparedStatement.executeUpdate();
    }


    public void countQuery(String query) throws SQLException {
        Connection connect = null;
        PreparedStatement preparedStatement = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + host + "/crawler_database?"
                            + "user=" + user + "&password=" + passwd );
        } catch (Exception e) {
            System.out.println("database error");
        } finally {
            close();
        }
        statement = connect.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS old_queries(query VARCHAR(700) NOT NULL PRIMARY KEY);");
        preparedStatement = connect.prepareStatement("INSERT IGNORE INTO old_queries (query) VALUES ('"+query+"')");
        preparedStatement.executeUpdate();
    }


    public ResultSet getAutoComplete(String query) throws SQLException {
        Connection connect = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + host + "/crawler_database?"
                            + "user=" + user + "&password=" + passwd );
        } catch (Exception e) {
            System.out.println("database error");
        } finally {
            close();
        }
        ResultSet resultSet = statement.executeQuery("SELECT `query` FROM `old_queries` WHERE QUERY Like '%"+query+"%'");
        return resultSet;
    }


    public void writePersonName(String countryName,String personName) throws SQLException {
        Connection connect = null;
        PreparedStatement preparedStatement = null;
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://" + host + "/crawler_database?"
                            + "user=" + user + "&password=" + passwd );
        } catch (Exception e) {
            System.out.println("database error");
        }
        preparedStatement = connect.prepareStatement("INSERT INTO `trends_table`(`country`, `person_name`)" +
                " VALUES (?,?)");
        preparedStatement.setString(1, countryName);
        preparedStatement.setString(2, personName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        connect.close();
    }

 
  // You need to close the resultSet
  private void close() {
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