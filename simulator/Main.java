package simulator;

import java.io.IOException;

import global.Parameter;

/*
 * 1. Parameter Class ���� ������ �����մϴ�.
 * 2-1. �������� �����͸� �������� �ʾ��� ��� 
 * 		-> Main Ŭ������ �����մϴ�. (�������� ������ �� ������ ������ ����, �׸��� em �ܰ���� �ڵ������� ����)  
 * 2-2. �������� �����͸� �������� ���(Main�� ���� ������ �����͸� em �ܰ踸 ������ ���� ���) 
 * 		-> Parameter ���� �� em class�� �����Ű�� �˴ϴ�.
 * 
 */
public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println("=================");
		System.out.println("P A R A M E T E R");
		System.out.println("=================");
		System.out.println("people\t"+Parameter.peopleNum);
		System.out.println("step\t"+Parameter.stepNum);
		System.out.println("store\t"+Parameter.storeNum);
		System.out.println("f\t"+Parameter.f);
		System.out.println("p\t"+Parameter.p);
		System.out.println("q\t"+Parameter.q);
		System.out.println("threshold\t"+Parameter.threshold);
		
		long time1 = System.currentTimeMillis (); 
		OriginalData o = new OriginalData();
		o.run();
		
		NoisedData n = new NoisedData();
		try {
			n.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		EM_algorithm2 em = new EM_algorithm2();
		em.run();

		long time2 = System.currentTimeMillis ();
		System.out.println ( ( time2 - time1 ) / 1000.0 );
	}

}
