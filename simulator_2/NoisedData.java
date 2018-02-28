package simulator_2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.StringTokenizer;

import global.Parameter;
import global.textUtil;

public class NoisedData {

	private int peopleNum = Parameter.peopleNum;
	private int stepNum = Parameter.stepNum;
	private int storesNum = Parameter.storeNum;

	private double f = Parameter.f;
	private double p = Parameter.p;
	private double q = Parameter.q;
	
	public NoisedData() {
		System.out.println("=============");
		System.out.println("  N O I S E ");
		System.out.println("=============");
		System.out.println("people : "+ this.peopleNum);
		System.out.println("step   : "+ this.stepNum);
		System.out.println("store  : "+ this.storesNum);
		System.out.println("   f   : "+ this.f);
		System.out.println("   p   : "+ this.p);
		System.out.println("   q   : "+ this.q);
		System.out.println("\n");		
	}
	
	
	// change or not change at first Noise : true is change
	private boolean firstNoiseCheck() {
		boolean check;			// false is not change
		if(Math.random()<f) 
			check=true;
		else
			check=false;
		return check;
	}
	
	// do first Noise
	public String firstNoise(String originalLine) {
		String line = "";
		for(int i=0; i<originalLine.length(); i++) {
			if(firstNoiseCheck()) {
				if(Math.random()<0.5)
					line += "1";
				else
					line += "0";
			}else
				line += originalLine.charAt(i);
		}
		return line;
	}

	// do second Noise
	public String secondNoise(String firstNoiseLine) {
		String line = "";
		for(int i=0; i<firstNoiseLine.length(); i++) {
			if(firstNoiseLine.charAt(i)=='0') {
				if(Math.random()<p)
					line += "1";
				else
					line += "0";
			}else {
				if(Math.random()<q)
					line += "1";
				else
					line += "0";
			}
		}
		return line;
	}
	
	public void run() throws IOException {

		for(int curStep=1; curStep<=stepNum; curStep++) {
			String outputPath = "output/noise/noiseData_"+peopleNum+"_"+storesNum+"_"+curStep+"-"+stepNum+"_"+f+"_"+p+"_"+q+".txt";
			Writer txtWriter = textUtil.createTXTFile(outputPath);

			try {
				System.out.print(".");
				String inputPath = "output/original/originalData_"+peopleNum+"_"+storesNum+"_"+curStep+"-"+stepNum+".txt";
				FileInputStream stream;
				stream = new FileInputStream(inputPath);
				InputStreamReader reader = new InputStreamReader(stream);
				@SuppressWarnings("resource")
				BufferedReader buffer = new BufferedReader(reader);
							
				while(true) {
					String line = buffer.readLine();
					if(line == null) 
						break;
					StringTokenizer lst = new StringTokenizer(line, "\t");
					String id = lst.nextToken();
					String postNoisedData = secondNoise(firstNoise(lst.nextToken()));
					String currNoisedData = secondNoise(firstNoise(lst.nextToken()));
					textUtil.writeString(txtWriter,
							id+"\t"+postNoisedData+"\t"+currNoisedData+"\n");
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			textUtil.saveTXTFile(txtWriter);
		}
		System.out.println("N O I S E   A D D I N G   C O M P L E T E ! !\n");
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		NoisedData n = new NoisedData();
		n.run();
	}

}
