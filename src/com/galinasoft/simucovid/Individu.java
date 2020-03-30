package com.galinasoft.simucovid;

public class Individu {
	int age;
	int school;
	int enterprise;
	int family;
	boolean alive;
	boolean infected;
	int howInfected; // 1=family 2=Work; 3=school
	boolean immunized;
	int dayOfContamination; 
	
	public Individu(int age, int school, int work, int family) {
		this.age = age;
		this.school = school;
		this.enterprise = work;
		this.family = family;
		this.alive = true;
		this.infected = false;
		this.howInfected = 0;
		this.immunized = false;
		this.dayOfContamination = -1;
	}
	
	public boolean isWorking() { // student or adult not senior
		return ((age % 4 < 2) && !isSenior()); // 50% des actifs travaillent ou vont à l'école mais pas les seniors
	}
	
	public boolean isSenior() {
		return age > 64;
	}
	
	public boolean isStudent() {
		return age < 30;
	}
	
	public boolean isContagious() {
		return (alive && infected && !immunized);
	}
	
	public boolean isInfectable() {
		return alive && !infected && !immunized;
	}
	public boolean isDetected(int day) {
		return (alive && infected && (day - dayOfContamination) > Simu.DayOfFirstSign);
	}
}
