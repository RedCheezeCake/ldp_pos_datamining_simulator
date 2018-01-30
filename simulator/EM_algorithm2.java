package simulator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import global.Parameter;

public class EM_algorithm2 {

	private int peopleNum = Parameter.peopleNum;
	private int stepNum = Parameter.stepNum;
	private int storesNum = Parameter.storeNum;

	private double f = Parameter.f;
	private double p = Parameter.p;
	private double q = Parameter.q;
	
	private double[][] theta = new double[storesNum][storesNum];
	private double[][] preTheta = new double[storesNum][storesNum];
	private HashMap<String,Double> numeProbMap = new HashMap<String,Double>();
	private double threshold = Parameter.threshold;
	
	private double[][][] prob_noised_ij_original_ij = new double[peopleNum][storesNum][storesNum];
	
	private ArrayList<Line> noiseData = new ArrayList<Line>();
	
	public EM_algorithm2() {
		System.out.println("==========================");
		System.out.println("  E M - A P P R O A C H  2");
		System.out.println("==========================");
		
	}
	public void readData() {
		for(int curStep=0; curStep<stepNum; curStep++) {
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
//					System.out.println("==========================================");
//					System.out.println(this.cond_prob_noised_ij_original_ij[r][i][j]);
//					break;
				}
			}
		}
	}
	
	public void run() {
		readData();
		
		//1. Initialization
		System.out.println("THRESHOLD : " + this.threshold);
		for(int i=0; i<storesNum; i++) {
			for(int j=0; j<storesNum; j++) {
				if (i==j)
					theta[i][j] = 0;
				else
					theta[i][j] =  1/(double)(storesNum*storesNum-storesNum);
			}
		}
		
		computeConditionalProb();
		
		//2. Iteration
		int cnt = 0;
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
			
			// M-Step
			for(int i=0; i<storesNum; i++) {
				for(int j=0; j<storesNum; j++){
					double tempSum = 0;
					for(int r=0; r<(int)this.peopleNum; ++r) {
						tempSum = tempSum + prob_original_ij_noised_ij[i][j][r];
					}
					this.theta[i][j] = tempSum/sumProb;
				}
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
		
			if(max < threshold)
				break;
			
			
			++cnt;
			
		}
		System.out.println("===============");
		System.out.println("  R E S U L T ");
		System.out.println("===============");
		System.out.print("p|c\t");
		for(int i=0; i<storesNum; i++)
			System.out.print("["+i+"]\t");
		System.out.println();
		for(int i=0; i<storesNum; i++) {
			System.out.print("["+i+"]");
			for(int j=0; j<storesNum; j++)
				System.out.print("\t"+theta[i][j]);
//				System.out.print(Double.parseDouble(String.format("%.2f",theta[i][j]*100))+"\t");
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EM_algorithm2 em = new EM_algorithm2();
		em.run();
		System.out.println("Done !!!!");
	}

}

