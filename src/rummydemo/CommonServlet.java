package rummydemo;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;

public class CommonServlet{
	
	static int isLive = 0; // 0 = sandbox , 1 = live // KISHAN

	public static  Connection startDbConnection () {
		String url = null;
		Connection conn = null;
		
	    try { 
	    	
	    	Class.forName("com.mysql.jdbc.Driver");
		      if (isLive == 1) {
		    	  url = "jdbc:mysql://182.50.154.150:3306/reviewpr_bmn?user=reviewpr_common&password=imobdev@123";
		      } else {
			        // Local MySQL instance to use during development.
			        
			        url = "jdbc:mysql://127.0.0.1:3306/pokeru8?user=root";
			
		      }
	    } catch (Exception e) { 
	    	e.printStackTrace();
	    }
	    try {
	    	conn = (Connection) DriverManager.getConnection(url);
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return conn;

	}
	
	public static boolean closeDbConnection (Connection conn) {
		try {
	      if(conn != null){
	    	  conn.close();
	      }
	    } catch (SQLException e) {
	    
	    }
	    return true;
	}
	
}

