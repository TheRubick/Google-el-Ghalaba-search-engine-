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
    } catch (Exception e) {
        throw e;
      }
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
        statement = connect.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS SITES_CLICKS(LINK VARCHAR(700) NOT NULL PRIMARY KEY, COUNT_CLICKS INT NOT NULL);");
        preparedStatement = connect.prepareStatement("INSERT INTO SITES_CLICKS (LINK,COUNT_CLICKS) VALUES ('"+link+"',0) ON DUPLICATE KEY UPDATE COUNT_CLICKS = COUNT_CLICKS +1 ");
        preparedStatement.executeUpdate();
    }


    public void countQuery(String query) throws SQLException {
        //Connection connect = null;
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
            //close();
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
        System.out.println("SELECT `query` FROM `old_queries` WHERE QUERY Like '%"+query+"%'");
        statement = connect.createStatement();
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

}