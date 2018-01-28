package global;

import java.io.File;

public class Parameter {

	public static final String outputType = "static";
	
	public static int peopleNum = 100000; 	// ����� ��
	public static int stepNum = 1;	// ������ Ƚ��
	public static int storeNum = 10;	// ������� ����

	public static double threshold = 0.0001;	// �Ӱ谪
	
	public static int sHigh = 0;  	// 0~storeNum-1
	public static double pIncrease = 1.3;
	public static double sMaxIncrease = 1.25;	// max�� min ������ ���� ���� ���Ƿ� �����Ǿ� ���� ������ ���� Ȯ�� ������ �����մϴ�. 
	public static double sMinIncrease = 1.2; 		// 
	
	public static double f = 0; 		// 0 <= f <= 0.5 / f->0 is low error 
	public static double p = 0.25; 		// 0 <= p <= 0.5 / p->0 is low error
	public static double q = 0.75; 		// 0.5 <= q <= 1 / q->1 is low error
	
	public static String originalTXTOutputPath = "output/"+outputType+"/originalData_"+peopleNum+"_"+storeNum+"_"+stepNum+".txt";		// output .txt as line form (people by people)
	public static String noiseTXTOutputPath =  "output/"+outputType+"/noiseData_"+peopleNum+"_"+stepNum+"_"+f+"_"+p+"_"+q+".txt";
	public static String errorTXTOutputPath = "output/"+outputType+"/errorData_"+peopleNum+"_"+stepNum+"_"+f+"_"+p+"_"+q+".txt";

	public static String workspace=new File("").getAbsolutePath();
}
