package simulator_2;

import java.io.IOException;

import global.Parameter;

/*
 * 1. Parameter Class 에서 변수를 설정합니다.
 * 2-1. 오리지널 데이터를 생성하지 않았을 경우 
 * 		-> Main 클래스를 실행합니다. (오리지널 데이터 및 노이즈 데이터 생성, 그리고 em 단계까지 자동적으로 수행)  
 * 2-2. 오리지널 데이터를 생성했을 경우(Main을 통해 생성된 데이터를 em 단계만 돌리고 싶은 경우) 
 * 		-> Parameter 조정 후 em class만 실행시키면 됩니다.
 * 
 */
public class Main {

	public void run(int people, int step, int store, double t, double f, double p, double q) {
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
		/*
		NoisedData n = new NoisedData();
		try {
			n.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		EM_algorithm3 em = new EM_algorithm3();
		em.run();

		long time2 = System.currentTimeMillis ();
		System.out.println ( ( time2 - time1 ) / 1000.0 );
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Main m1 = new Main();

		m1.noiseRun(500000, 1, 10, 0.00001, 0.25, 0.35, 0.65);
	//	m1.run(500000, 1, 10, 0.00001, 0.25, 0.35, 0.65);

		
	}

}
