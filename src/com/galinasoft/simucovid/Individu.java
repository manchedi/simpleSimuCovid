package com.galinasoft.simucovid;

public class Individu {
	final static int MAXAGESTUDENT = 30; // tune it for specify % of young people
	final static int MINAGESENIOR = 65; // tune it for specify % of young people
	int age;
	int school;
	int enterprise;
	int family;
	int friendGroup;
	boolean alive;
	boolean infected;
	HowInfected howInfected; // 1=family 2=Work; 3=school
	boolean immunized;
	int dayOfContamination; 
	
	public Individu(int age, int school, int work, int family, int friendGroup) {
		this.age = age;
		this.school = school;
		this.enterprise = work;
		this.family = family;
		this.friendGroup = friendGroup;
		this.alive = true;
		this.infected = false;
		this.immunized = false;
		this.dayOfContamination = -1;
	}
	
	public boolean isWorking() { // student or adult not senior
		return ((age % 4 < 3) && !isSenior()); // 75% des actifs travaillent ou vont à l'école mais pas les seniors
	}
	
	public boolean isSenior() {
		return age >= MINAGESENIOR;  // don't tune it for specify % of senior people, use
	}
	
	public boolean isStudent() {
		return age < MAXAGESTUDENT;  
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
