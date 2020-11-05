package com.galinasoft.simucovid;

public class Individu {
	final static int MAXAGESTUDENT = 30; // tune it for specify % of young people
	final static int MINAGESENIOR = 65; // tune it for specify % of young people
	short age;
	int school;
	int enterprise;
	int family;
	int friendGroup;
	boolean alive;
	boolean infected;
	short howManyInfected;
	HowInfected howInfected; // 1=family 2=Work; 3=school
	boolean immunized;
	short dayOfContamination; 
	short virusGeneration;
	
	public Individu(short age, int school, int work, int family, int friendGroup) {
		this.age = age;
		this.school = school;
		this.enterprise = work;
		this.family = family;
		this.friendGroup = friendGroup;
		this.alive = true;
		this.infected = false;
		this.immunized = false;
		this.dayOfContamination = -1;
		this.virusGeneration = 0;
		this.howManyInfected = 0;
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
	
	public boolean isContagious(int day) {
		return (infected && !immunized && alive && ((day - dayOfContamination) > Simu.dayStartOfContagiousness));
	}
	
	public boolean isInfected() {
		return infected ;
	}
	
	public boolean isInfectable() {
		return !infected && !immunized && alive;
	}
	public boolean isDetected(int day) {
		return (alive && infected && (day - dayOfContamination) > Simu.dayOfFirstSign);
	}
}
