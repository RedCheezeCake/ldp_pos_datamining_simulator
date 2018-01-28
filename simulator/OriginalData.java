package simulator;

import java.io.Writer;

import global.Parameter;
import global.textUtil;

public class OriginalData {
	private int peopleNum = Parameter.peopleNum;
	private int stepNum = Parameter.stepNum;
	private int curStep = 0;
	private int storesNum = Parameter.storeNum;
	
	private int sHigh = Parameter.sHigh;
	private double pIncrease = Parameter.pIncrease;		// person -> store increase
	private double sMaxIncrease = Parameter.sMaxIncrease; // store -> store increase
	private double sMinIncrease = Parameter.sMinIncrease; 
	private Person[] people;
	private Store[] stores;
	private int[][] peopleSumArr = new int[storesNum][storesNum];

	private Writer txtWriter;
	private String outputPath = "output/original/originalData_"+peopleNum+"_"+storesNum+"_"+curStep+"-"+stepNum+".txt";

	// 인원, 상점 리스트 생성
	public OriginalData() {
		this.people = new Person[peopleNum];
		this.stores = new Store[storesNum];
	}
	
	// 초기 데이터 생성
	private void init() {
		double[] increArr = setPSDistribution();
		setSSDistribution();
		txtWriter = textUtil.createTXTFile(outputPath);
		for(int i=0; i<peopleNum; i++) {
			String tmp = "";
			int loc=0;
			double random = Math.random();
			for(;loc<increArr.length;loc++) {
				if(random < increArr[loc])
					break;
			}
			for(int j=0; j<storesNum; j++) {
				if(j==loc)
					tmp += "1";
				else
					tmp += "0";
			}
			this.people[i] = new Person(i, loc, tmp);
			textUtil.writeString(txtWriter, i+"\t"+tmp+"\n");
		}
		textUtil.saveTXTFile(txtWriter);
		this.curStep++;
		System.out.println("I N I T");
		showStat();
		for(int i=0; i<stores.length; i++)
			stores[i].showStat();
	}
	
	// set probability density (Person to Store )
	public double[] setPSDistribution() {
		double[] increArr = new double[storesNum];
		double sum=0;
		int maxDistance;
		
		if(sHigh>=storesNum/2)
			maxDistance=sHigh+1;
		else
			maxDistance=storesNum-sHigh;
		
		for(int i=0; i<increArr.length; i++) {
			increArr[i] = Math.pow(pIncrease, maxDistance-Math.abs(sHigh-i));
			sum += increArr[i];
		}
		for(int i=0; i<increArr.length; i++) {
			if(i==0)
				increArr[i] = increArr[i]/sum;
			else
				increArr[i] = increArr[i-1]+increArr[i]/sum;
		}
		return increArr;
	}
	
	// set probability density (Store to Store (move) )
	public void setSSDistribution() {
		for(int i=0; i<storesNum; i++) {
			// 지수값을 임의로 지정하여 해당 스토어에 넣습니다.
			double increRatio = Math.random()*(sMaxIncrease-sMinIncrease)+sMinIncrease;
			stores[i] = new Store(i, storesNum, increRatio);
		}
	}
	
	// move
	public void move() {
		outputPath = "output/original/originalData_"+peopleNum+"_"+storesNum+"_"+curStep+"-"+stepNum+".txt";
		txtWriter = textUtil.createTXTFile(outputPath);
		for(int i=0; i<peopleNum; i++) {
			double p = Math.random();
			int currStore = people[i].getCurStore();
			int nextStore = stores[people[i].getCurStore()].nextStore(p);
			people[i].moveNextStore(nextStore);
			if(currStore != nextStore) {
				textUtil.writeString(txtWriter, people[i].getID()+"\t"+people[i].getPosStr()+"\n");
				peopleSumArr[currStore][nextStore]++;
			}
		}
		textUtil.saveTXTFile(txtWriter);
		this.curStep++;
	}
	
	
	public void showStat() {
		System.out.println("================");
		System.out.println(" Current State");
		System.out.println("================");
		for(int i=0; i<this.storesNum; i++) 
			System.out.print("("+i+")\t");
		System.out.println();
		int[] pSum = new int[storesNum];
		for(int i=0; i<peopleNum; i++) 
			pSum[people[i].getCurStore()]++;
		for(int i=0; i<pSum.length; i++)
			System.out.print(pSum[i]+"\t");
		System.out.println();
		for(int i=0; i<pSum.length; i++)
			System.out.print(((int)((double)pSum[i]/(double)peopleNum*100))+"%\t");
		System.out.println();

	}
	
	public void run() {
		init();
		for(int i=0; i<stepNum; i++) {
			System.out.println();
			System.out.println("["+this.curStep + "] S T E P");
			move();
			showStat();
		}
		System.out.println("\nPEOPLE SUM");
		for(int i=0; i<storesNum; i++) {
			for(int j=0; j<storesNum; j++) {
				System.out.print((peopleSumArr[i][j]++)+"\t");
			}
			System.out.println();
		}
		System.out.println();
		System.out.println("\nD A T A  M A K I N G  C O M P L E T E ! !\n\n");
	}
	public static void main(String[] args) {
		OriginalData od = new OriginalData();
		od.run();
	}
}

class Person{
	private String postPosStr;
	private String currPosStr;
	private int curStore;
	private int pId;
	
	public Person(int _pId, int _curStore, String _pData) {
		this.currPosStr = _pData;
		this.postPosStr = _pData;
		this.curStore = _curStore;
		this.pId = _pId;
	}
	
	public int getID() {	return this.pId;	}
	public int getCurStore() { return this.curStore; }
	public String getPosStr() { return this.postPosStr + "\t" + this.currPosStr;	}
	
	public void moveNextStore(int _nextStore) {
		if(_nextStore == curStore)
			this.postPosStr = this.currPosStr;
		else {
			String tmp = "";
			for(int i=0; i<currPosStr.length(); i++) {
				if(i==_nextStore)
					tmp += "1";
				else
					tmp += "0";
			}
			this.postPosStr = this.currPosStr;
			this.currPosStr = tmp;
			this.curStore = _nextStore;
		}
	}
	
}

class Store{
	private int storeId;
	private double[][] storesProbArr;
	
	public Store(int _sid, int _storesNum, double _increRatio) {
		this.storeId = _sid;
		this.storesProbArr = new double[_storesNum-1][2]; //[n][0] : 다음 스토어, [n][1] : 다음 스토어로 갈 확률
		
		
		for(int i=0; i<storesProbArr.length; i++)
			storesProbArr[i][0] = -1;
		// 확률 배열의 다음 스토어의 순서를 무작위로 집어 넣습니다. 
		for(int i=0; i<_storesNum;) {
			if(i == this.storeId)
				i++;
			int idx = (int) (Math.random()*storesProbArr.length);
			if(storesProbArr[idx][0] == -1) 
				storesProbArr[idx][0] = i++;
		}
		
		// 다음 스토어로 갈 확률 배열을 지수값을 기준으로 형성합니다. 
		double probSum = 0;
		for(int i=0; i<storesProbArr.length; i++) {
			storesProbArr[i][1] = Math.pow(_increRatio, i);
			probSum += storesProbArr[i][1];
		}
		for(int i=0; i<storesProbArr.length; i++) {
			if(i==0)
				storesProbArr[i][1] = storesProbArr[i][1]/probSum;
			else if (i==storesProbArr.length-1)
				storesProbArr[i][1] = 1;
			else
				storesProbArr[i][1] = storesProbArr[i-1][1] + storesProbArr[i][1]/probSum;
		}
	}
	
	public int nextStore(double _p) {
		int n = 0;
		for(int i=0; i<storesProbArr.length; i++) {
			if(_p<storesProbArr[i][1]) {
				n=i;
				break;
			}
		}
		return (int) storesProbArr[n][0];
	}
	
	public void showStat() {
		//System.out.println("\nSTORE ["+this.storeId+"]\t(->"+storesProbArr[storesProbArr.length-1][0]+")");
		/*
		for(int i=0; i<this.storesProbArr.length+1; i++)
			if(i==storeId)
				System.out.print(i+"\t");
			else {
				for(int j=0; j<this.storesProbArr.length; j++) {
					if(i==storesProbArr[j][0]) 
						System.out.print(storesProbArr[j][0]+"\t");
				}
			}
		System.out.println();
		*/
		for(int i=0; i<this.storesProbArr.length+1; i++) {
			if(i==storeId)
				System.out.print("0\t");
			else {
				for(int j=0; j<this.storesProbArr.length; j++){
					if(i==storesProbArr[j][0]) {
						if(j==0)
							System.out.print(Double.parseDouble(String.format("%.3f",storesProbArr[j][1]))+"\t");
						else
							System.out.print(Double.parseDouble(String.format("%.3f",(storesProbArr[j][1]-storesProbArr[j-1][1])))+"\t");
					}
				}
			}
		}
		System.out.println();
	}
}