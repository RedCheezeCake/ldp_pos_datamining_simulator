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

public class EM_algorithm {

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
	
	private ArrayList<Line> noiseData = new ArrayList<Line>();
	
	public EM_algorithm() {
		System.out.println("==========================");
		System.out.println("  E M - A P P R O A C H ");
		System.out.println("==========================");
		
		System.out.println("THRESHOLD : " + this.threshold);
		for(int i=0; i<storesNum; i++) {
			for(int j=0; j<storesNum; j++) {
				theta[i][j] =  1/(double)(storesNum*storesNum);
			}
		}
	}
	public void readData() {
		for(int curStep=1; curStep<=stepNum; curStep++) {
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
						
						double numeProb = 0;
						for(int i=0; i<storesNum; i++) 
							for(int j=0; j<storesNum; j++)
								numeProb += posGivenLx(pre,  i) * posGivenLx(cur,  j);
						numeProbMap.put(pre+cur, numeProb);
						
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
	
	public double Estep(String _prePos, String _curPos, int _preIdx, int _curIdx) {
		double denoProb=0;
		// 분자
		denoProb = preTheta[_preIdx][_curIdx] * posGivenLx(_prePos, _preIdx) * posGivenLx(_curPos, _curIdx);
			
		// 분모
		double numeProb = 0;
		// 잘못됨.
		double p = numeProbMap.get(_prePos+_curPos);
		for(int i=0; i<storesNum; i++) 
			for(int j=0; j<storesNum; j++)
				numeProb += preTheta[i][j]; 

		return denoProb/(numeProb* p);
	}
	
	public double Mstep(int _preStore, int _curStore) {
		double pSum = 0;
		for(int i=0; i<noiseData.size(); i++) {
			Line line = noiseData.get(i);
			pSum += Estep(line.prePos, line.curPos, _preStore, _curStore);
		}
		return pSum/(double) peopleNum*stepNum;
	}
	
	private void thetaNomalize() {
	//	double[][] tmpTheta = new double[storesNum][storesNum];
		double thetaSum=0;
		for(int i=0; i<storesNum; i++)
			for(int j=0; j<storesNum; j++)
				thetaSum+=theta[i][j];
		for(int i=0; i<storesNum; i++)
			for(int j=0; j<storesNum; j++)
				theta[i][j] = theta[i][j]/thetaSum;
	/*	
		double sum=0;
		for(int i=0; i<storesNum; i++)
			for(int j=0; j<storesNum; j++)
				sum += tmpTheta[i][j];
//		System.out.println("**"+sum);
		
		double d =0;
		if(sum > 1) 
			d = sum - 1;
		for(int i=0; i<storesNum; i++)
			for(int j=0; j<storesNum; j++)
				theta[i][j] = tmpTheta[i][j]-d;
			*/
	}
	public void run() {
		readData();
		int thresholdIdx=0;
		for(int s=0;; s++) {
			System.out.println("# "+s);
			for(int i=0; i<storesNum; i++) 
				for(int j=0; j<storesNum; j++)
					preTheta[i][j] = theta[i][j];
			
			for(int i=0; i<storesNum; i++) 
				for(int j=0; j<storesNum; j++) 
					theta[i][j] = Mstep(i, j);
				
			
			thetaNomalize();
			double sum=0;
			for(int i=0; i<storesNum; i++)
				for(int j=0; j<storesNum; j++)
					sum+=theta[i][j];
			System.out.println(sum);
			
			double max = 0;
			for(int i=0; i<storesNum; i++) {
				for(int j=0; j<storesNum; j++) {
					double t = Math.abs(theta[i][j] - preTheta[i][j]);
					if(t>max)
						max=t;
				}
			}
			System.out.println("Max : " + max);
			if(max<threshold)
				break;
/*
			System.out.print("p|c\t");
			for(int i=0; i<storesNum; i++)
				System.out.print("["+i+"]\t");
			System.out.println();
			for(int i=0; i<storesNum; i++) {
				System.out.print("["+i+"]");
				for(int j=0; j<storesNum; j++) 
					System.out.print("\t"+theta[i][j]);
				System.out.println();
			}
			*/
			/*
			if(max<Parameter.thresholdArr[thresholdIdx]) {
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
//						System.out.print(Double.parseDouble(String.format("%.2f",theta[i][j]*100))+"\t");
					System.out.println();
				}
				thresholdIdx++;
			}
			if(thresholdIdx>=Parameter.thresholdArr.length)
				break;
				*/
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
		EM_algorithm em = new EM_algorithm();
		em.run();
	}

}
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
