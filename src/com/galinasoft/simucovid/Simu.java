package com.galinasoft.simucovid;

import java.util.Random;

public class Simu {
	// how is infected
	final static int WORK = 2;
	final static int FAMILY = 1;
	final static int SCHOOL = 3;
	
	final int SIZE= 1000;
	final int FAMILYSIZE = 4; 
	final int N_ENTERPRISE = SIZE/20;
	final int N_SCHOOL = SIZE/200;
	final double FamilyTransmissionRate = 0.05;
	final double EnterpriseTransmissionRate = 0.01;
	final double SchoolTransmissionRate = 0.02;
	final double DeathRateYoung = 0.0002;
	final double DeathRateAdult = 0.0012;
	final double DeathRateSenior = 0.0035;
	final static int DayOfFirstSign = 5;
	final int RECOVERYTIME = 21;
	final int FirstDayOfContainment =5;
	final int DurationOfContainment = 45;
	final int FirstDayOfTotalContainment = 140;
	final int DurationOfTotalContainment = 10;
	
	Individu[] group;
	Family[] families;
	Enterprise[] enterprises;
	School[] schools;
	
	Random rnd = new Random(42);
	
	public Simu() {
		this.group = new Individu[SIZE];
		this.families = new Family[SIZE/FAMILYSIZE];
		this.enterprises = new Enterprise[N_ENTERPRISE];
		this.schools = new School[N_SCHOOL];
	}
	
	public static void main(String[] args) {
		
		Simu simu = new Simu();
		simu.createFamilies();
		simu.createEnterprises();
		simu.createSchools();
		simu.fillGroup();
		simu.initFirstCases();
		
		simu.run(90);
	}
	
	public void createFamilies() {
		for(int f=0 ; f< families.length ; f++) {
			families[f] = new Family();
		}
	}
	public void createEnterprises() {
		for(int e=0 ; e< enterprises.length ; e++) {
			enterprises[e] = new Enterprise();
		}
	}
	public void createSchools() {
		for(int s=0 ; s< schools.length ; s++) {
			schools[s] = new School();
		}
	}
	
	public void fillGroup() {
		
		for (int i=0; i<group.length ; i++) {
			int age = rnd.nextInt(80) + 1;
			int school = rnd.nextInt(N_SCHOOL);
			int work = rnd.nextInt(N_ENTERPRISE);
			int family = rnd.nextInt((int)(SIZE/FAMILYSIZE));

			group[i] = new Individu(age, school, work, family);
			families[family].add(group[i]);
			if (group[i].isStudent()) {
				schools[school].add(group[i]);
			} else if (group[i].isWorking()) {
				enterprises[work].add(group[i]);
			}
		}
	}
	
	private void initFirstCases() {
		Individu i0 = group[40];
		i0.infected = true;
		i0.dayOfContamination=0;
		i0.age = 40;  // multiple de 4 = isWorking
		i0.howInfected = WORK;
		
		Individu i1 = group[20];
		i1.infected = true;
		i1.dayOfContamination=0;
		i1.age = 12; // multiple de 4 = isWorking
		i1.howInfected = SCHOOL;
		
	}
	
	public void run(int ndays) {
		System.out.println("\"day\",\"infected\",\"immunized\",\"dead\",\"byFamily\",\"byWork\",\"bySchool\"");
		// pour chaque jour
		for (int day=0 ; day<ndays ; day++) {
			// pour chaque individu
			int infecteds = 0;
			int immunizeds = 0;
			int deads = 0;
			int howmanyFamily = 0;
			int howmanyWork = 0;
			int howmanySchool = 0;
			for (int i=0; i < group.length; i++) {
				Individu ind = group[i];
				aday(day, ind);	
				// healing
				if (ind.infected && day > ind.dayOfContamination+RECOVERYTIME) {
					ind.infected = false;
					ind.immunized = true;
				}
				if (ind.isContagious()) {
					infecteds++;
					switch (ind.howInfected) {
					case WORK: howmanyWork++;
						break;
					case FAMILY: howmanyFamily++;
						break;
					case SCHOOL: howmanySchool++;
						break;						
					}
					
				}
				if (ind.alive && ind.immunized) immunizeds++;
				if (!ind.alive) deads++;
			}
			
			//System.out.println("day "+ day + " infecteds: " + infecteds + " immunized: " + immunizeds + " deads: " + deads );
			System.out.println(day + "," + infecteds + "," + immunizeds + "," + deads + "," + howmanyFamily + "," + howmanyWork + "," + howmanySchool );

		}
		
		// result how many infected
		int howManyInfected = 0;
		int howManyImmunized = 0;
		int howManyDead = 0;
		for (int i=0; i < group.length; i++) {
			Individu ind = group[i];
			if (ind.isContagious()) howManyInfected++;
			if (ind.alive && ind.immunized) howManyImmunized++;
			if (!ind.alive) howManyDead++;
		}
		System.out.println("Total infected : " + howManyInfected );
		System.out.println("Total immunized: " + howManyImmunized);
		System.out.println("Total deads: " + howManyDead);
	}
	
	
	public void aday(int day, Individu ind) {
		int dayOfWeek = day % 7;

		if (dayOfWeek < 5) {
			// en semaine
			if (ind.isContagious()) {
				if (ind.isWorking() && !ind.isDetected(day) && !isTotalContainment(day)) { //si détecté malade on travaille pas, si guéri on retravaille
					// Enterprise
					if (!ind.isStudent()) {
						for (Individu colleague : enterprises[ind.enterprise].colleagues) {
							if (!colleague.equals(ind) && colleague.alive && !colleague.isDetected(day)) { // si détecté malade on travaille pas
								double x = rnd.nextDouble();								
								if (isContainment(day)) x /= 3;		// effectif reduit en entreprise								
								if (x < EnterpriseTransmissionRate) {
									if (!colleague.infected && !colleague.immunized) {
										colleague.dayOfContamination  = day;
										colleague.infected = true;
										colleague.howInfected = WORK;
									}
								}
								
							}
						}
					}
					// School
					if (ind.isStudent() && !isContainment(day)) {  // en confinement y a pas école
						for (Individu student : schools[ind.school].students) {
							if (!student.equals(ind) && student.alive) {
								double x = rnd.nextDouble();
								if (x < SchoolTransmissionRate) {
									if (!student.infected && !student.immunized) {
										student.dayOfContamination  = day;
										student.infected = true;
										student.howInfected = SCHOOL;
									}
								}
							}
						}
						
					}
				}
				// family
				for (Individu member : families[ind.family].members) {
					if (!member.equals(ind)) {
						double x = rnd.nextDouble();
						if (isContainment(day)) x *= 3; // le confinement augmente la contamination en famille
						if (x < FamilyTransmissionRate) {
							if (!member.infected && !member.immunized) {
								member.dayOfContamination  = day;
								member.infected = true;
								member.howInfected = FAMILY;
							}
						}
					}
				}				
			} // alive 

		} else {
			// week-end
			// family
			if (ind.isContagious()) { 
				for (Individu member : families[ind.family].members) {
					if (!member.equals(ind)) {
						double x = rnd.nextDouble();
						if (isContainment(day)) x *= 3; // le confinement augmente la contamination en famille
						if (x < FamilyTransmissionRate) {
							if (member.isInfectable()) {
								member.dayOfContamination = day;
								member.infected = true;
								member.howInfected = FAMILY;
							}
						}
					}
				}
			}
		}
		// Death 
		if (ind.isContagious()) {
			if (day - ind.dayOfContamination >= DayOfFirstSign) {
				double death = rnd.nextDouble();
				if (ind.age <= 30) {
					// youngs
					if (death < DeathRateYoung) {
						ind.alive = false;
					}
				} else if (ind.age <= 60) {
					// adults
					if (death < DeathRateAdult) {
						ind.alive = false;
					}
				} else {
					// seniors
					if (death < DeathRateSenior) {
						ind.alive = false;
					}
				}
			}
		}
	}
	
	private boolean isContainment(int day) {
		return day >= FirstDayOfContainment && day <= (FirstDayOfContainment + DurationOfContainment);
	}
	private boolean isTotalContainment(int day) {
		return day >= FirstDayOfTotalContainment && day <= (FirstDayOfTotalContainment + DurationOfTotalContainment);
	}
}
