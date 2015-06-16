package edu.buffalo.cse.blueseal.networkflow.summary;

public class TfidfCounter {
	
	private int count;
	private String stringName;
	private String apkName;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getName() {
		return stringName;
	}

	public String getApkName() {
		return apkName;
	}
	
	public void setName(String name) {
		this.stringName = name;
	}

	
	public TfidfCounter(int newCount, String newName, String newApkName){
		this.count = 1;
		this.stringName = newName;
		this.apkName = newApkName;
	}

	public void incrementCount(){
		count++;
	}

}
