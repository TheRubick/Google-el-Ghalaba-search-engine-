// Accesses a database with student and phone information

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class AccessStudentsCalls {

	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	final private String host = "xxxxxxxxxxxxxxxxxxxxxx";
	final private String user = "xxxxxxxxxxxxxxxxxxxxxx";
	final private String passwd = "xxxxxxxxxxxxxxxxxxxxxx";
	final private String database = "xxxxxxxxxxxxxxxxxxxxxx";

	public void connectToDB() throws Exception {
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");

			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://" + host + "/"
					+ database + "?" + "user=" + user + "&password=" + passwd);

		} catch (Exception e) {
			throw e;
		}
	}

	public void readStudents() throws Exception {
		try {
			statement = connect.createStatement();
			resultSet = statement
					.executeQuery("select * from " + database + ".students");
			while (resultSet.next()) {
				int Id = resultSet.getInt("Id");
				String name = resultSet.getString("name");
				String phone = resultSet.getString("phone");

				System.out.println(String.format(
						"Id: %d name: %5s  phone: %5s", Id, name, phone));
			}
		} catch (Exception e) {
			throw e;
		}
	}

	// You need to close the resultSet
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}

}