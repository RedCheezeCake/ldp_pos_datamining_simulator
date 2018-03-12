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
		boolean flag = true;
		try {
			stmt.executeQuery("CREATE TABLE "+tableName +"("
					+ "PRE VARCHAR(20),"
					+ "CUR VARCHAR(20),"
					+ "PROBABILITY NUMBER,"
					+ "PATH VARCHAR(50),"
					+ "LENGTH NUMBER)");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("CREATING MATRIX TABLE IS FAIL : "+e.getMessage());
			flag = false;
		}
		if(flag) {
			double[][] matrix = makeMatrix(inputPath);
			insertMatrix(tableName, matrix);
//			showTable(tableName);
		}
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
	
	public void insertResultQuery(LinkedList<LinkedList<Object>> result, String table) {
		boolean flag = true;
		try {
			stmt.executeQuery("CREATE TABLE "+table +"("
					+ "PRE VARCHAR(20),"
					+ "CUR VARCHAR(20),"
					+ "PROBABILITY VARCHAR(100),"			// ** PROBABILITY IS NOT NUMBER! 
					+ "PATH VARCHAR(50),"
					+ "LENGTH NUMBER,"
					+ "MATCH VARCHAR(20))");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.print("CREATING RESULT TABLE IS FAIL : "+e.getMessage());
			flag = false;
		}
		if(!flag) {
			try {
				stmt.executeQuery("DROP TABLE "+table);

				stmt.executeQuery("CREATE TABLE "+table +"("
						+ "RANK NUMBER,"
						+ "PRE VARCHAR(20),"
						+ "CUR VARCHAR(20),"
						+ "PROBABILITY VARCHAR(100),"			// ** PROBABILITY IS NOT NUMBER! 
						+ "PATH VARCHAR(50),"
						+ "LENGTH NUMBER,"
						+ "MATCH VARCHAR(20))");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int cnt=1;
		for(LinkedList<Object> list : result) {
			String line = "";
			for(int i=0; i<list.size(); i++) {
				if(i != 0)
					line += ", ";
				if(i == list.size()-1)
					line += "'";
				line += list.get(i).toString();
				if(i == list.size()-1)
					line += "'";
			}
			try {
				stmt.executeQuery("INSERT INTO "+table +"("
						+ "PRE, CUR, PROBABILITY, PATH, LENGTH, MATCH )"
						+ "VALUES ("
						+ (cnt++)+","+line +")");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public double getProbabilityByK(String table, int k) {
		double totalCnt = 0;
		double matchCnt = 0;
		try {
			rs = stmt.executeQuery("SELECT COUNT(*) "
							+ "FROM "+table+" "
							+ "WHERE ROWNUM<="+k+" ");
			rs.next();	// Moves the cursor forward one row from its current position.
			totalCnt = rs.getDouble(1);
			rs = stmt.executeQuery("SELECT COUNT(*) "
					+ "FROM ("
							+ "SELECT * FROM "+table+" WHERE ROWNUM<="+k+")"
					+ "WHERE MATCH = 'match'");
			rs.next();
			matchCnt = rs.getDouble(1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.print(e.getMessage());
		}
		return matchCnt/totalCnt;
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
		int init = 0;
		int gap = 14;
		int time = 5;
		int[] lengthArray = {1,3,5};
		int[] kArray = {3, 5, 10, 20, 50, 100};
		double[] rankProb = new double[kArray.length];
		double[][] probMatrix = new double[lengthArray.length][kArray.length];
		long time1 = System.currentTimeMillis (); 

		int maxk = 100;
		int dataSize = 4500000;
		int beaconNum = 30;
		int emtime = 6000;
		LinkedList<LinkedList<Object>> OriginalResult;
		LinkedList<LinkedList<Object>> EmResult;
		DBProcess dbp = new DBProcess(dbURL, username, password);

		System.out.println("init : "+init+"\tgap : "+gap+"\ttime : "+time+"\tdata size : "+dataSize);
		dbp.createTable("ORIGINAL_"+dataSize, "output\\result\\originalData_"+dataSize+"_"+beaconNum+".txt");
		dbp.createTable("BEACON_"+dataSize, "output\\result\\EMData_"+dataSize+"_"+beaconNum+"_0.25_0.35_0.65_"+emtime+".txt");
		for(int t=0; t<time; t++) {
			int start = t+init;
			int end = (start+gap)%beaconNum;
			for(int i=0; i<lengthArray.length; i++) {
				int shotestPath = Math.abs(end-start)/2;
				if(Math.abs(end-start)%2!=0)
					shotestPath++;
				
				System.out.println(start+"->"+end+"\t length : "+(shotestPath+lengthArray[i]));
				dbp.txtWriter = textUtil.createTXTFile("output/topk/"+start+"_"+end+"_"+(shotestPath+lengthArray[i])+"_"+maxk+".txt");
				OriginalResult = dbp.recursiveQuery("ORIGINAL_"+dataSize, start, end, (shotestPath+lengthArray[i]), maxk);
				EmResult = dbp.recursiveQuery("BEACON_"+dataSize, start, end, (shotestPath+lengthArray[i]), maxk);
	
				// matching check
				double rankDiff = 0;
				for(int k=0; k<kArray.length; k++) {
					int matchCnt = 0;
					for(int x=0; x<kArray[k] && x<OriginalResult.size(); x++) {
						boolean flag = false;
						for(int y=0; y<kArray[k] && x<EmResult.size(); y++) {
							if(OriginalResult.get(x).get(3).toString().compareTo(EmResult.get(y).get(3).toString())==0) {
								rankDiff += Math.pow(x-y, 2);
								flag = true;
								break;
							}
						}
						if(flag)
							matchCnt++;
						else {
							rankDiff += Math.pow(k, 2);
						}
					}
					// calculate probability 
					rankProb[k] += 1-((6*rankDiff)/(kArray[k]*(Math.pow(kArray[k], 2)-1)));
					probMatrix[i][k] += (double) matchCnt / (double) kArray[k];
					System.out.println(matchCnt);
				}
				
				// show and write result
				textUtil.writeString(dbp.txtWriter, "ORIGINAL\n");
				for(LinkedList<Object> p : OriginalResult) {
					for(Object q : p) {
						textUtil.writeString(dbp.txtWriter, q+"\t");
					}
					textUtil.writeString(dbp.txtWriter, "\n");
				}
				
				textUtil.writeString(dbp.txtWriter, "EM-APPROACH\n");
				for(LinkedList<Object> p : EmResult) {
					for(Object q : p) {
						textUtil.writeString(dbp.txtWriter, q+"\t");
					}
					textUtil.writeString(dbp.txtWriter, "\n");
				}
				
				/*
				// insert result into database
				String resultTable = "Original_"+dataSize+"_"+start+"_"+end+"_"+(shotestPath+lengthArray[i])+"_"+kArray[i];
				dbp.insertResultQuery(OriginalResult, resultTable);
				
				for(int k=0; k<kArray.length; k++) {
					double prob = dbp.getProbabilityByK(resultTable, kArray[k]);
					probMatrix[i][k] += prob;
				}
				*/
				textUtil.saveTXTFile(dbp.txtWriter);
				OriginalResult.clear();
				EmResult.clear();
			}
		}

		long time2 = System.currentTimeMillis ();
		System.out.println ( ( time2 - time1 ) / 1000.0 );
		
		System.out.println("=========== R E S U L T ============");
		dbp.txtWriter = textUtil.createTXTFile("output/topk/ProbResult_"+dataSize+"_"+init+"_"+gap+"_"+time+".txt");
		System.out.println("init : "+init+"\tgap : "+gap+"\ttime : "+time);
		textUtil.writeString(dbp.txtWriter, "init : "+init+"\tgap : "+gap+"\ttime : "+time+"\n");

		System.out.println("LENGTH - K probability");
		textUtil.writeString(dbp.txtWriter, "LENGTH - K probability\n");
		
		for(int k : kArray) {
			System.out.print("\t"+k);
			textUtil.writeString(dbp.txtWriter, "\t"+k);
		}
		System.out.println();
		textUtil.writeString(dbp.txtWriter, "\n");

		for(int i=0; i<probMatrix.length; i++) {
			textUtil.writeString(dbp.txtWriter, lengthArray[i]+"\t");
			System.out.print(lengthArray[i]+"\t");
			for(int j=0; j<probMatrix[i].length; j++) {
				textUtil.writeString(dbp.txtWriter, Double.parseDouble(String.format("%.3f",probMatrix[i][j]/time))+"\t");
				System.out.print(Double.parseDouble(String.format("%.3f",probMatrix[i][j]/time))+"\t");
			}
			textUtil.writeString(dbp.txtWriter, "\n");
			System.out.println();
		}
		
		System.out.println("RANK - K probability");
		textUtil.writeString(dbp.txtWriter, "RANK - K probability\n");
		
		for(int k : kArray) {
			System.out.print(k+"\t");
			textUtil.writeString(dbp.txtWriter, k+"\t");
		}
		System.out.println();
		textUtil.writeString(dbp.txtWriter, "\n");

		for(int i=0; i<rankProb.length; i++) {
			System.out.print(Double.parseDouble(String.format("%.3f",rankProb[i]/time))+"\t");
			textUtil.writeString(dbp.txtWriter, Double.parseDouble(String.format("%.3f",rankProb[i]/time))+"\t");
		}
		textUtil.saveTXTFile(dbp.txtWriter);
		dbp.closeProcess();
	}
}