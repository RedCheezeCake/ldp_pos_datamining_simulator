package global;

import java.io.File;

public class Parameter {

	public static final String outputType = "static";
	
	public static int peopleNum = 100000; 	// 사용자 수
	public static int stepNum = 1;	// 움직일 횟수
	public static int storeNum = 10;	// 스토어의 갯수

	public static double threshold = 0.0001;	// 임계값
	
	public static int sHigh = 0;  	// 0~storeNum-1
	public static double pIncrease = 1.3;
	public static double sMaxIncrease = 1.25;	// max와 min 사이의 지수 값이 임의로 설정되어 다음 스토어로 가는 확률 분포를 형성합니다. 
	public static double sMinIncrease = 1.2; 		// 
	
	public static double f = 0; 		// 0 <= f <= 0.5 / f->0 is low error 
	public static double p = 0.25; 		// 0 <= p <= 0.5 / p->0 is low error
	public static double q = 0.75; 		// 0.5 <= q <= 1 / q->1 is low error
	
	public static String originalTXTOutputPath = "output/"+outputType+"/originalData_"+peopleNum+"_"+storeNum+"_"+stepNum+".txt";		// output .txt as line form (people by people)
	public static String noiseTXTOutputPath =  "output/"+outputType+"/noiseData_"+peopleNum+"_"+stepNum+"_"+f+"_"+p+"_"+q+".txt";
	public static String errorTXTOutputPath = "output/"+outputType+"/errorData_"+peopleNum+"_"+stepNum+"_"+f+"_"+p+"_"+q+".txt";

	public static String workspace=new File("").getAbsolutePath();
}
