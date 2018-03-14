package simulator_2;

import java.io.IOException;

import global.Parameter;

/*
 * function description
 * 1. fullRun 	: original data ���� -> noise ������ ���� -> Em �˰���
 * 2. noiseRun 	: ���� original data ������� noise ������ ���� -> Em �˰���
 * 3. justEMRun	: ���� original, noise data ������� Em �˰��� ����
 * 
 * parameter description
 * 1. people	: ����� ��
 * 2. step 		: �����̴� Ƚ��
 * 3. store		: ����(����)�� ��
 * 4. t			: em �ܰ迡���� threshold
 * 5. f, p, q	: em �ܰ迡���� f, p, q ��.
 */
public class Main {

	public void fullRun(int people, int step, int store, double t, double f, double p, double q) {
		Parameter.peopleNum = people;
		Parameter.stepNum = step;
		Parameter.storeNum = store;
		Parameter.threshold = t;
		Parameter.f = f;
		Parameter.p = p;
		Parameter.q = q;
		
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
		
		EM_algorithm3 em = new EM_algorithm3();
		em.run();

		long time2 = System.currentTimeMillis ();
		System.out.println ( ( time2 - time1 ) / 1000.0 );
	}
	
	public void noiseRun(int people, int step, int store, double t, double f, double p, double q) {
		Parameter.peopleNum = people;
		Parameter.stepNum = step;
		Parameter.storeNum = store;
		Parameter.threshold = t;
		Parameter.f = f;
		Parameter.p = p;
		Parameter.q = q;
		
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
		
		NoisedData n = new NoisedData();
		try {
			n.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		EM_algorithm3 em = new EM_algorithm3();
		em.run();

		long time2 = System.currentTimeMillis ();
		System.out.println ( ( time2 - time1 ) / 1000.0 );
	}
	
	public void justEMRun(int people, int step, int store, double t, double f, double p, double q) {
		Parameter.peopleNum = people;
		Parameter.stepNum = step;
		Parameter.storeNum = store;
		Parameter.threshold = t;
		Parameter.f = f;
		Parameter.p = p;
		Parameter.q = q;
		
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

		EM_algorithm3 em = new EM_algorithm3();
		em.run();

		long time2 = System.currentTimeMillis ();
		System.out.println ( ( time2 - time1 ) / 1000.0 );
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Main m1 = new Main();

		// run 
		m1.noiseRun(500000, 1, 10, 0.00001, 0.25, 0.35, 0.65);

	}

}
