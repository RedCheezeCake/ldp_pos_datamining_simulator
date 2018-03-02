package db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.StringTokenizer;

import global.Parameter;

public class DBProcess {
	String dbURL;
	String username;
	String password;
	
	Connection connection = null;
	Statement stmt = null;
	ResultSet rs = null;
	
	public DBProcess(String dbURL, String username, String password) {
		this.dbURL = dbURL;
		this.username = username;
		this.password = password;
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("NOT FOUND OJDBC DRIVER!!");
			e.printStackTrace();
		}
		try {
			// database connect
			connection = DriverManager.getConnection(dbURL,username,password);
			stmt = connection.createStatement();
			System.out.println("DATABASE CONNECTION OK...");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createTable(String tableName, String inputPath) {
		try {
			stmt.executeQuery("DROP TABLE "+tableName);
			stmt.executeQuery("CREATE TABLE "+tableName +"("
					+ "PRE VARCHAR(20),"
					+ "CUR VARCHAR(20),"
					+ "PROBABILITY NUMBER,"
					+ "PATH VARCHAR(50),"
					+ "LENGTH NUMBER)");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[][] matrix = makeMatrix(inputPath);
		insertMatrix(tableName, matrix);
//		showTable(tableName);
	}

	private double[][] makeMatrix(String inputPath){
		double[][] matrix = new double[Parameter.storeNum][Parameter.storeNum];
		try {
			FileInputStream stream;
			stream = new FileInputStream(inputPath);
			InputStreamReader reader = new InputStreamReader(stream);
			@SuppressWarnings("resource")
			BufferedReader buffer = new BufferedReader(reader);
			if(inputPath.contains("EMData")) {
				try {
					buffer.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for(int i=0; i<Parameter.storeNum; i++) {
				String line = "";
				try {
					line = buffer.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StringTokenizer st = new StringTokenizer(line,"\t");
				for(int j=0; j<Parameter.storeNum; j++) 
					matrix[i][j] = Double.parseDouble(st.nextToken());
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Row Sum = 1
		for(int i=0; i<matrix.length; i++) {
			double sum = 0;
			for(int j=0; j<matrix[i].length; j++) {
				sum += matrix[i][j];
			}
			for(int j=0; j<matrix[i].length; j++) {
				matrix[i][j] = matrix[i][j]/sum;
			}
		}
		return matrix;		
	}
			
	private void insertMatrix(String tableName, double[][] matrix) {
		try {
			for(int i=0; i<matrix.length; i++){
				for(int j=0; j<matrix[i].length; j++) {
					if(matrix[i][j] != 0)
						stmt.executeQuery("INSERT INTO "+tableName +"("
								+ "PRE, CUR, PROBABILITY, PATH, LENGTH )"
								+ "VALUES ("
								+ i+"," 
								+ j+"," 
								+ matrix[i][j]+"," 
								+ i+"||'-'||"+j+","
								+ "1)");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LinkedList<LinkedList<Object>> recursiveQuery(String tableName, int pre, int cur, int length, int k) {
		LinkedList<LinkedList<Object>> result = new LinkedList<LinkedList<Object>>();
		int minDistance = (pre+cur)/Parameter.moveRange;	// for more fast sort
		try {
			rs = stmt.executeQuery("WITH ROUTES(PRE, CUR, PROBABILITY, PATH, LENGTH) AS ("
					+ 	"SELECT PRE, CUR, PROBABILITY, PATH, LENGTH "
					+ 	"FROM "+tableName+" "
					+ 	"UNION ALL "
					+ 		"SELECT ROUTES.PRE, "+tableName+".CUR, "
					+ 				"ROUTES.PROBABILITY*"+tableName+".PROBABILITY as PROBABILITY,"
					+ 				"ROUTES.PATH||'-'||"+tableName+".CUR as PATH,"
					+ 				"ROUTES.LENGTH+"+tableName+".LENGTH as LENGTH "
					+ 		"FROM ROUTES, "+tableName+" "
					+ 		"WHERE ROUTES.PRE = '"+pre+"' and ROUTES.CUR = "+tableName+".PRE and ROUTES.LENGTH+"+tableName+".LENGTH<="+length
					+ ")"
					+ "SELECT * "
					+ "FROM (SELECT * FROM "
									+ "(SELECT * FROM ROUTES WHERE LENGTH>="+minDistance+") "
							+ "ORDER BY PROBABILITY DESC) "
					+ "WHERE PRE='"+pre+"' and CUR='"+cur+"' and LENGTH<="+length+" and ROWNUM<="+k+" "
					);
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnNumber = rsmd.getColumnCount();
			while(rs.next()) {
				result.add(new LinkedList<Object>());
				for(int i=0; i<columnNumber; i++) {
					result.getLast().add(rs.getString(i+1));
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public void showTable(String tableName) {
		try {
			rs = stmt.executeQuery("select * from "+ tableName);
			ResultSetMetaData rsmd = rs.getMetaData();
			
			int columnNumber = rsmd.getColumnCount();
			while(rs.next()) {
				for(int i=0; i<columnNumber; i++) {
					System.out.print(rs.getString(i+1)+"\t");
				}
				System.out.println();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeProcess() {
		try {
			rs.close();
			stmt.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) { 
		String dbURL = "jdbc:oracle:thin:@localhost:1521:ORCL";
		String username = "scott";
		String password = "tiger";
		int start = 0;
		int end = 10;
		int[] length = {1, 3, 5};
		int[] k = {3, 5, 10, 20};
		
		int dataSize = 1500000;
		int beaconNum = 30;
		int num = 10000;
		LinkedList<LinkedList<Object>> OriginalResult;
		LinkedList<LinkedList<Object>> EmResult;
		DBProcess dbp = new DBProcess(dbURL, username, password);
		for(int i=0; i<length.length; i++) {
			for(int j=0; j<k.length; j++) {
				System.out.println(start+"->"+end+"\tlength="+(length[i]+end)+"\tk="+k[j]);
				dbp.createTable("ORIGINAL", "output\\result\\originalData_"+dataSize+"_"+beaconNum+".txt");
				OriginalResult = dbp.recursiveQuery("ORIGINAL", start, end, (length[i]+end), k[j]);
				
				dbp.createTable("BEACON", "output\\result\\EMData_"+dataSize+"_"+beaconNum+"_0.25_0.35_0.65_"+num+".txt");
				EmResult = dbp.recursiveQuery("BEACON", start, end, (length[i]+end), k[j]);
				
				// matching check
				for(int x=0; x<k[j]; x++) {
					boolean flag = false;
					for(int y=0; y<k[j]; y++) {
						if(OriginalResult.get(x).get(3).toString().compareTo(EmResult.get(y).get(3).toString())==0) {
							flag = true;
							break;
						}
					}
					if(flag)
						OriginalResult.get(x).add("matched");
					else
						OriginalResult.get(x).add("Not matched");
				}
				// show result
				System.out.println("=== O R I G I N A L ===");
				for(LinkedList<Object> p : OriginalResult) {
					for(Object q : p) {
						System.out.print(q + "\t");
					}
					System.out.println();
				}
				System.out.println("=== E M ===");
				for(LinkedList<Object> p : EmResult) {
					for(Object q : p) {
						System.out.print(q + "\t");
					}
					System.out.println();
				}
				System.out.println();
			}
		}
		dbp.closeProcess();
		
	}
}
