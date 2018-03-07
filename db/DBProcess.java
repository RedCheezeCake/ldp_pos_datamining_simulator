package db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.StringTokenizer;

import global.Parameter;
import global.textUtil;

public class DBProcess {
	String dbURL;
	String username;
	String password;
	
	Connection connection = null;
	Statement stmt = null;
	ResultSet rs = null;
	
	Writer txtWriter;

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
	
	public LinkedList<LinkedList<Object>> recursiveQuery(String table, int pre, int cur, int length, int k) {
		LinkedList<LinkedList<Object>> result = new LinkedList<LinkedList<Object>>();
		try {
			rs = stmt.executeQuery("WITH ROUTES(PRE, CUR, PROBABILITY, PATH, LENGTH) AS ("
					+ 	"SELECT PRE, CUR, PROBABILITY, PATH, LENGTH "
					+ 	"FROM "+table+" "
					+ 	"UNION ALL "
					+ 		"SELECT ROUTES.PRE, "+table+".CUR, "
					+ 				"ROUTES.PROBABILITY*"+table+".PROBABILITY as PROBABILITY,"
					+ 				"ROUTES.PATH||'-'||"+table+".CUR as PATH,"
					+ 				"ROUTES.LENGTH+"+table+".LENGTH as LENGTH "
					+ 		"FROM ROUTES, "+table+" "
					+ 		"WHERE ROUTES.PRE = '"+pre+"' and ROUTES.CUR = "+table+".PRE and ROUTES.LENGTH+"+table+".LENGTH<="+length
					+ ")"
					+ "SELECT * "
					+ "FROM (SELECT * FROM ROUTES "
							+"ORDER BY PROBABILITY DESC) "
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
		int[] length = {1,3,5};
		
		int maxk = 100;
		int dataSize = 1500000;
		int beaconNum = 30;
		int num = 10000;
		LinkedList<LinkedList<Object>> OriginalResult;
		LinkedList<LinkedList<Object>> EmResult;
		DBProcess dbp = new DBProcess(dbURL, username, password);

		dbp.createTable("ORIGINAL", "output\\result\\originalData_"+dataSize+"_"+beaconNum+".txt");
		dbp.createTable("BEACON", "output\\result\\EMData_"+dataSize+"_"+beaconNum+"_0.25_0.35_0.65_"+num+".txt");
		
		for(int i=0; i<length.length; i++) {
			long time1 = System.currentTimeMillis (); 
			int shotestPath = Math.abs(end-start)/2;
			if(Math.abs(end-start)%2!=0)
				shotestPath++;
			dbp.txtWriter = textUtil.createTXTFile("output/topk/"+start+"_"+end+"_"+(shotestPath+length[i])+"_"+maxk+".txt");
			System.out.println(start+"->"+end+"\t length : "+(shotestPath+length[i]));

			OriginalResult = dbp.recursiveQuery("ORIGINAL", start, end, (shotestPath+length[i]), maxk);
			EmResult = dbp.recursiveQuery("BEACON", start, end, (shotestPath+length[i]), maxk);

			// matching check
			for(int x=0; x<maxk && x<OriginalResult.size(); x++) {
				boolean flag = false;
				for(int y=0; y<maxk && x<OriginalResult.size(); y++) {
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
			textUtil.writeString(dbp.txtWriter, "ORIGINAL\n");
			for(LinkedList<Object> p : OriginalResult) {
				for(Object q : p) {
					textUtil.writeString(dbp.txtWriter, q+"\t");
					System.out.print(q + "\t");
				}
				textUtil.writeString(dbp.txtWriter, "\n");
				System.out.println();
			}
			
			System.out.println("=== E M ===");
			textUtil.writeString(dbp.txtWriter, "EM-APPROACH\n");
			for(LinkedList<Object> p : EmResult) {
				for(Object q : p) {
					textUtil.writeString(dbp.txtWriter, q+"\t");
					System.out.print(q + "\t");
				}
				textUtil.writeString(dbp.txtWriter, "\n");
				System.out.println();
			}
			System.out.println();
			
			textUtil.saveTXTFile(dbp.txtWriter);
			OriginalResult.clear();
			EmResult.clear();
			
			long time2 = System.currentTimeMillis ();
			System.out.println ( ( time2 - time1 ) / 1000.0 );
		}
		dbp.closeProcess();
	}
}
