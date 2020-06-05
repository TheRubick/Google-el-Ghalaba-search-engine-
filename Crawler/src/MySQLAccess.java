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
      
    } catch (Exception e) {
        throw e;
      }
  }


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

	public void delbyUrl(ArrayList<String> url) {
		try {
			String s = "delete from word_url where url IN (";
			StringBuilder sb = new StringBuilder(s);
			for (String entry : url) {
				sb.append("'" + entry + "',");
			}
			s = sb.substring(0,sb.length()-1);
			s = s.concat(");");
			statement = connect.createStatement();
			statement.executeUpdate(s);

		} catch (Exception e) {
		}
	}


	public void insertWordUrl(String url, HashMap<String, Double > wordScore) {
		String s = "INSERT INTO word_url VALUES " ;
		StringBuilder sb = new StringBuilder(s);
		try {
			for (HashMap.Entry<String, Double> entry : wordScore.entrySet()) {
				sb.append("( '" + entry.getKey() +"','" + url +"'," + entry.getValue() +"),");
			}
			s = sb.substring(0,sb.length()-1);
			s = s.concat(";");
			statement = connect.createStatement();
			statement.executeUpdate(s);

		} catch (Exception e) {
			System.out.println("error in inserting word URL");
			System.out.println(s);
		}
	}

	public void insertImages(HashMap<String, Integer > Imgs) {
		String s = "INSERT IGNORE INTO img_word VALUES " ;
		StringBuilder sb = new StringBuilder(s);
		try {
			for (HashMap.Entry<String, Integer> entry : Imgs.entrySet()) {
				String[] arr = entry.getKey().split("@@::;;@@;", -2);
				arr[1] = arr[1].replaceAll("[\']","");
				arr[0] = arr[0].replaceAll("[\']","");
				sb.append("( '" + arr[1] +"','" + arr[0]+"'),");
			}
			s = sb.substring(0,sb.length()-1);
			s = s.concat(";");
			statement = connect.createStatement();
			statement.executeUpdate(s);

		} catch (Exception e) {
			System.out.println("error in inserting image URL");
			System.out.println(s);
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
	      }
	  preparedStatement = connect.prepareStatement("INSERT IGNORE INTO crawler_database.crawler_table VALUES (?,?,?,?,?,?,DEFAULT)");
      preparedStatement.setString(1, link);
      preparedStatement.setString(2, text);
      preparedStatement.setString(3, image_sources);
      preparedStatement.setString(4, title); // should be fixed
      preparedStatement.setString(5, refererLink);
	  preparedStatement.setString(6, URLLocation);
      preparedStatement.executeUpdate();
      preparedStatement.close();
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

		//System.out.println(values);
		//preparedStatement = connect.prepareStatement("INSERT IGNORE INTO POPULARITY_RANK(LINK,POPULARITY_SCORE) VALUES "+values);
 		//preparedStatement.setString(1, values);
		values += ";";
		statement.executeUpdate("INSERT IGNORE INTO POPULARITY_RANK(LINK,POPULARITY_SCORE) VALUES "+values);
//      preparedStatement.close();
		connect.close();
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

}