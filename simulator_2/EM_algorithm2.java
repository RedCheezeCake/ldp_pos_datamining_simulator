package simulator_2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.StringTokenizer;

import global.Parameter;
import global.textUtil;

public class EM_algorithm2 {
	class Line{
		public int id;
		public String prePos;
		public String curPos;
		public Line(int _id, String _prePos, String _curPos) {
			this.id = _id;
			this.prePos = _prePos;
			this.curPos = _curPos;
		}
	}
	
	private int peopleNum = Parameter.peopleNum;
	private int stepNum = Parameter.stepNum;
	private int storesNum = Parameter.storeNum;

	private double f = Parameter.f;
	private double p = Parameter.p;
	private double q = Parameter.q;
	
	private double[][] theta = new double[storesNum][storesNum];
	private double[][] preTheta = new double[storesNum][storesNum];
	
	private double threshold = Parameter.threshold;
	private String EMDataOutputPath = "output/result/EMData_"+peopleNum+"_"+storesNum+"_"+f+"_"+p+"_"+q;
	private Writer txtWriter;

	private double[][][] prob_noised_ij_original_ij = new double[peopleNum][storesNum][storesNum];
	
	private ArrayList<Line> noiseData = new ArrayList<Line>();
	
	public EM_algorithm2() {
		System.out.println("==========================");
		System.out.println("  E M - A P P R O A C H  2");
		System.out.println("==========================");
		System.out.println("people : "+ this.peopleNum);
		System.out.println("step   : "+ this.stepNum);
		System.out.println("store  : "+ this.storesNum);
		System.out.println("   f   : "+ this.f);
		System.out.println("   p   : "+ this.p);
		System.out.println("   q   : "+ this.q);
		System.out.println("\n");	
	}
	public void readData() {
		for(int curStep=1; curStep<stepNum+1; curStep++) {
			try {
				System.out.print(".");
				String inputPath = "output/noise/noiseData_"+peopleNum+"_"+storesNum+"_"+curStep+"-"+stepNum+"_"+f+"_"+p+"_"+q+".txt";
				FileInputStream stream;
				stream = new FileInputStream(inputPath);
				InputStreamReader reader = new InputStreamReader(stream);
				@SuppressWarnings("resource")
				BufferedReader buffer = new BufferedReader(reader);
							
				while(true) {
					try {
						String line;
						line = buffer.readLine();
						if(line == null) 
							break;
						StringTokenizer lst = new StringTokenizer(line,"\t");
						int id = Integer.parseInt(lst.nextToken());
						String pre = lst.nextToken();
						String cur = lst.nextToken();
						noiseData.add(new Line(id,pre,cur)); 
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("\nD A T A   R E A D   C O M P L E T E ! !\n");
	}
	
	private double posGivenLx(String pos, int x) {
		
		double prob = 1;
		for(int i=0; i<storesNum; i++) {
			char _pos = pos.charAt(i);
			if(i==x) {
				switch(_pos) {
				case '1': // s=1, l=1
					prob *= (1-f/2)*q+f*(p/2);
				case '0': // s=0, l=1
					prob *= (1-f/2)*(1-q)+(f/2)*(1-p);
				}
			}else {
				switch(_pos) {
				case '1':	// s=1, l=0
					prob *= f*(q/2)+(1-f/2)*p;
				case '0':	// s=0, l=0
					prob *= (f/2)*(1-q) + (1-f/2)*(1-p);
				}
			}
		}
		return prob;
	}
	
	
	public void computeConditionalProb() {
		for (int r=0; r<(int)this.peopleNum; ++r) {
			Line line = noiseData.get(r);
//			System.out.println(line.id + " : " + line.prePos + " : " + line.curPos);
			for(int i=0; i<storesNum; i++) {
				for(int j=0; j<storesNum; j++) {				
					this.prob_noised_ij_original_ij[r][i][j] =  posGivenLx(line.prePos, i) * posGivenLx(line.curPos, j);
//					System.out.print(this.prob_noised_ij_original_ij[r][i][j]+"\t");
				}
	//			System.out.println();
			}
//			break;
		}
	}
	
	public void run() {
		readData();
		
		System.out.println("THRESHOLD : " + this.threshold);
		for(int i=0; i<storesNum; i++) {
			int min = i - Parameter.moveRange;
			int max = i + Parameter.moveRange;
			for(int j=min; j<=max; j++) 
				if(j != i)
					theta[i][(j+storesNum)%storesNum] =  1/(double)(storesNum*Parameter.moveRange*2);
		}
		
		computeConditionalProb();
		
		//2. Iteration
		int cnt = 0;
		double lastMax=0;
		while(true) {
			for(int i=0; i<storesNum; i++) 
				for(int j=0; j<storesNum; j++)
					preTheta[i][j] = theta[i][j];
			
			
			double[][][] prob_original_ij_noised_ij = new double[this.storesNum][this.storesNum][this.peopleNum];
			double[] prob_posr = new double[(int)this.peopleNum];
			
			for (int r=0; r<(int)this.peopleNum; ++r) {
			
				prob_posr[r]=0;
				for (int y=0; y<this.storesNum; ++y) {
					for (int z=0; z<this.storesNum; ++z) {
						prob_posr[r] = prob_posr[r]  + this.preTheta[y][z] * this.prob_noised_ij_original_ij[r][y][z];			
					}
				}	
			}
			
			//  E-Step
			double sumProb =0;
			for(int i=0; i<storesNum; i++) {
				for(int j=0; j<storesNum; j++){
					for(int r=0; r<(int)this.peopleNum; ++r) {
						prob_original_ij_noised_ij[i][j][r] = (this.preTheta[i][j] * this.prob_noised_ij_original_ij[r][i][j]) / prob_posr[r];
						sumProb = sumProb +prob_original_ij_noised_ij[i][j][r];
					}
				}
			}
			System.out.println(sumProb);
			// M-Step
			for(int i=0; i<storesNum; i++) {
				for(int j=0; j<storesNum; j++){
					double tempSum = 0;
					for(int r=0; r<(int)this.peopleNum; ++r) {
						tempSum = tempSum + prob_original_ij_noised_ij[i][j][r];
					}
	//				System.out.println(tempSum);
					this.theta[i][j] = tempSum/sumProb;
	//				System.out.print(theta[i][j]+"\t");
				}
	//			System.out.println();
			}
			
			// Check termination condition
			double max = 0;	
			for(int i=0; i<storesNum; i++) {
				for(int j=0; j<storesNum; j++){
					if(max < Math.abs(this.preTheta[i][j]-this.theta[i][j]))
						max = Math.abs(this.preTheta[i][j]-this.theta[i][j]);
				}
			}
			System.out.println("[" + cnt+ ":" + max + "]  ");
		
			if(max < threshold) {
				lastMax = max;
				writeResult(cnt, lastMax);
				break;
			}
			if(cnt%500 == 0) {
				lastMax=max;
				writeResult(cnt, lastMax);
			}
			++cnt;
		}
		
	}
	
	public void writeResult(int cnt, double lastMax) {
		txtWriter = textUtil.createTXTFile(EMDataOutputPath+"_"+cnt+".txt");
		textUtil.writeString(txtWriter, cnt+"-"+lastMax+"\n");
		System.out.println("===============");
		System.out.println("  R E S U L T ");
		System.out.println("===============");
		System.out.print("p|c\t");
		for(int i=0; i<storesNum; i++)
			System.out.print("["+i+"]\t");
		System.out.println();
		for(int i=0; i<storesNum; i++) {
			System.out.print("["+i+"]");
			for(int j=0; j<storesNum; j++) {
				System.out.print("\t"+theta[i][j]);
				textUtil.writeString(txtWriter, theta[i][j]+"\t");
//				System.out.print(Double.parseDouble(String.format("%.2f",theta[i][j]*100))+"\t");
			}
//			textUtil.writeString(txtWriter, "\n");
			System.out.println();
		}
		textUtil.saveTXTFile(txtWriter);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EM_algorithm2 em = new EM_algorithm2();
		em.run();
		System.out.println("Done !!!!");
	}

}

