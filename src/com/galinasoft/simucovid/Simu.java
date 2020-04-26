package com.galinasoft.simucovid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Simu {
	// how is infected
	
	final int SIZE= 1_000_000;
	final int FAMILYSIZE = 4; // 4 person per family
	final int ENTERPRISESIZE = 20; // 20 workers per enterprise
	final int N_ENTERPRISE = SIZE/ENTERPRISESIZE; 
	final int SCHOOLSIZE = 200; // 200 students per school
	final int N_SCHOOL = SIZE/SCHOOLSIZE; 
	final int FRIENDGROUPSIZE = 4; // 4 friends per individual
	final int N_FRIENDGROUP = SIZE/FRIENDGROUPSIZE; 
	final double globalVirusRate = 0.55;
	final double FamilyTransmissionRate = 0.03 * globalVirusRate;
	final double EnterpriseTransmissionRate = 0.01 * globalVirusRate;
	final double SchoolTransmissionRate = 0.02 * globalVirusRate;
	final double FriendsTransmissionRate = 0.03 * globalVirusRate;
	final int MAXAGE = 80;  // use this parameter to tune % of senior (MAXAGE - 65)/ (MAXAGE)
	final double DeathRateYoung = 0.0002;
	final double DeathRateAdult = 0.0008;
	final double DeathRateSenior = 0.0039;
	final static int DayOfFirstSign = 8;
	final int DayOfFirstSevereSymptom = 9;
	final int RECOVERYTIME = 28;
	final int FirstDayOfContainment = 15;
	final int DurationOfContainment = 60;
	final int FirstDayOfTotalContainment = 999;
	final int DurationOfTotalContainment = 20;
	
	Individu[] group;
	Family[] families;
	Enterprise[] enterprises;
	School[] schools;
	FriendGroup[] friendGroups;
	int newInfecteds = 0;
	int newDeads = 0;
	
	Random rnd = new Random(42);
	
	public Simu() {
		this.group = new Individu[SIZE];
		this.families = new Family[SIZE/FAMILYSIZE];
		this.enterprises = new Enterprise[N_ENTERPRISE];
		this.schools = new School[N_SCHOOL];
		this.friendGroups = new FriendGroup[N_FRIENDGROUP];
	}
	
	public static void main(String[] args) {		
		Simu simu = new Simu();
		simu.createFamilies();
		simu.createEnterprises();
		simu.createSchools();
		simu.createFriends();
		simu.fillGroup();
		simu.initFirstCases();
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\Agraver\\sk-learn\\covid\\Reqsimu19.csv"));) {
			simu.run(200, bw);			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createFamilies() {
		for(int f=0 ; f< families.length ; f++) {
			families[f] = new Family(10); // 10 is default capacity of ArrayList
		}
	}
	public void createEnterprises() {
		for(int e=0 ; e< enterprises.length ; e++) {
			enterprises[e] = new Enterprise(ENTERPRISESIZE*2);
		}
	}
	public void createSchools() {
		for(int s=0 ; s<schools.length ; s++) {
			schools[s] = new School(SCHOOLSIZE*2);
		}
	}
	public void createFriends() {
		for(int f=0 ; f< friendGroups.length ; f++) {
			friendGroups[f] = new FriendGroup(10); // default capacity of ArrayList
		}
	}
	
	public void fillGroup() {
		
		for (int i=0; i<group.length ; i++) {
			int age = rnd.nextInt(MAXAGE) + 1;
			int school = rnd.nextInt(N_SCHOOL);
			int work = rnd.nextInt(N_ENTERPRISE);
			int family = rnd.nextInt((int)(SIZE/FAMILYSIZE));
			int friendGroup = rnd.nextInt(N_FRIENDGROUP);
			group[i] = new Individu((short)age, school, work, family, friendGroup);
			families[family].add(group[i]);
			friendGroups[friendGroup].add(group[i]);
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
		i0.virusGeneration = 1;
		i0.dayOfContamination = 0;
		i0.age = 40; // multiple de 4 = isWorking
		i0.howInfected = HowInfected.BYWORK;

		Individu i1 = group[20];   
		i1.infected = true;
		i1.virusGeneration = 1;
		i1.dayOfContamination = 0;
		i1.age = 12; // multiple de 4 = isWorking
		i1.howInfected = HowInfected.BYSCHOOL;
		
		Individu i2 = group[23];   // 20 years old
		i2.infected = true;
		i2.dayOfContamination = 0;
		i2.age = 23; // multiple de 4 = isWorking
		i2.howInfected = HowInfected.BYSCHOOL;
		
		Individu i3 = group[70];   // 20 years old
		i3.infected = true;
		i3.dayOfContamination = 0;
		i3.age = 70; // multiple de 4 = isWorking
		i3.howInfected = HowInfected.BYFAMILY;
		
		Individu i5 = group[79];   // 20 years old
		i5.infected = true;
		i5.dayOfContamination = 0;
		i5.age = 8; // multiple de 4 = isWorking
		i5.howInfected = HowInfected.BYFAMILY;
		
		Individu i4 = group[75];   // 20 years old
		i4.infected = true;
		i4.dayOfContamination = 0;
		i4.age = 52; // multiple de 4 = isWorking
		i4.howInfected = HowInfected.BYFAMILY;

	}
	
	public void run(int ndays, BufferedWriter bw) throws IOException {
		
		System.out.println("\"day\",\"infected\",\"immunized\",\"dead\",\"InfectByFamily\",\"InfectByWork\",\"InfectBySchool\",\"InfectByFriend\",\"newCases\",\"totalInfected\",\"newDeads\"");
		bw.write("\"day\",\"infected\",\"immunized\",\"dead\",\"InfectByFamily\",\"InfectByWork\",\"InfectBySchool\",\"InfectByFriend\",\"newCases\",\"totalInfected\",\"newDeads\"\n");

		int totalInfecteds = 0;
		
		// every day
		for (short day=0 ; day<ndays ; day++) {
			int infecteds = 0;
			int immunizeds = 0;
			int deads = 0;
			int infectedByFamily = 0;
			int infectedByWork = 0;
			int infectedBySchool = 0;
			int infectedByFriend = 0;
			
			newInfecteds = 0;
			newDeads = 0;
			
			// every person			
			for (Individu ind : group) {
				// contamination
				IndividualPropagationByDay(day, ind);
				
				// healing
				if (ind.infected && day > ind.dayOfContamination + RECOVERYTIME) {
					ind.infected = false;
					ind.immunized = true;
				}
				if (ind.isContagious()) {
					infecteds++;
					switch (ind.howInfected) {
					case BYWORK: infectedByWork++;
						break;
					case BYFAMILY: infectedByFamily++;
						break;
					case BYSCHOOL: infectedBySchool++;
						break;						
					case BYFRIEND: infectedByFriend++;
						break;						
					}					
				}
				if (ind.alive && ind.immunized) immunizeds++;
				if (!ind.alive) deads++;
			}
			totalInfecteds += newInfecteds;
			System.out.println(day + "," + infecteds + "," + immunizeds + "," + deads + "," + infectedByFamily + "," + infectedByWork + "," + infectedBySchool + "," + infectedByFriend + "," + newInfecteds + ',' + totalInfecteds + "," + newDeads );
			bw.write(day + "," + infecteds + "," + immunizeds + "," + deads + "," + infectedByFamily + "," + infectedByWork + "," + infectedBySchool + "," + infectedByFriend + "," + newInfecteds + ',' + totalInfecteds + "," + newDeads + "\n");
		}
		
		// result how many infected
		int howManyInfected = 0;
		int howManyImmunized = 0;
		int howManyDead = 0;
		int InfecByVirusGeneration[] = new int[102];
		for (Individu ind : group) {
			if (ind.isContagious()) howManyInfected++;
			if (ind.alive && ind.immunized) howManyImmunized++;
			if (!ind.alive) howManyDead++;
			if (ind.infected || ind.immunized) {
				if (ind.virusGeneration < 101) {
					InfecByVirusGeneration[ind.virusGeneration]++;
				} else {
					InfecByVirusGeneration[101]++;	
				}
			}
		}
		System.out.println("Total infected : " + howManyInfected );
		//System.out.println("Total immunized: " + howManyImmunized);
		//System.out.println("Total deads: " + howManyDead);
		for (int i=0 ; i<102 ; i++) {
			System.out.println("infected by generation " + i +  " is " + InfecByVirusGeneration[i]);
		}
		
		for (int i=1 ; i < 20; i++) {
			System.out.println(i + " " + virusAttenuation(i));
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void IndividualPropagationByDay(short day, Individu ind) {
		int dayOfWeek = day % 7;

		if (dayOfWeek < 5) {
			// week days
			if (ind.isContagious()) {
				if (ind.isWorking() && !ind.isDetected(day) && !isTotalContainment(day)) { // si détecté malade on ne travaille pas, si guéri on retravaille
					// Enterprise
					if (!ind.isStudent()) {
						for (Individu colleague : enterprises[ind.enterprise].colleagues) {
							if (colleague.isInfectable()) { // si détecté malade on ne travaille pas
								double x = rnd.nextDouble();
								if (isContainment(day)) x /= 3.0; // staff is reduced so rate is reduced
								x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
								if (x < EnterpriseTransmissionRate) {
									colleague.dayOfContamination = day;
									colleague.infected = true;
									colleague.virusGeneration = (short)(ind.virusGeneration + 1);
									colleague.howInfected = HowInfected.BYWORK;
									newInfecteds++;
								}

							}
						}
					}
					// School
					if (ind.isStudent() && !isContainment(day) && !isTotalContainment(day)) { // en confinement y a pas école
						for (Individu student : schools[ind.school].students) {
							if (student.isInfectable()) { // ind is not infectable so student != ind
								double x = rnd.nextDouble();
								x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
								if (x < SchoolTransmissionRate) {
									student.dayOfContamination = day;
									student.infected = true;
									student.virusGeneration = (short)(ind.virusGeneration + 1);
									student.howInfected = HowInfected.BYSCHOOL;
									newInfecteds++;
								}
							}
						}

					}
				}
				// family
				for (Individu member : families[ind.family].members) {
					if (member.isInfectable()) {
						double x = rnd.nextDouble();
						if (isContainment(day)) x /= 3; // containment increase contamination inside family						
						x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
						if (x < FamilyTransmissionRate) {
							member.dayOfContamination = day;
							member.infected = true;
							member.virusGeneration = (short)(ind.virusGeneration + 1);
							member.howInfected = HowInfected.BYFAMILY;
							newInfecteds++;
						}
					}
				}
			} // alive

		} else {
			// week-end
			// family
			if (ind.isContagious()) {
				for (Individu member : families[ind.family].members) {
					if (member.isInfectable()) {
						double x = rnd.nextDouble();
						if (isContainment(day))	x /= 3; // containment increase contamination inside family
						x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
						if (x < FamilyTransmissionRate) {
							member.dayOfContamination = day;
							member.infected = true;
							member.virusGeneration = (short)(ind.virusGeneration + 1);
							member.howInfected = HowInfected.BYFAMILY;
							newInfecteds++;
						}
					}
				}
				// week-end Friends
				if (!isContainment(day)) {
					for (Individu friend : friendGroups[ind.friendGroup].relations) {
						if (ind.isInfectable()) {
							double x = rnd.nextDouble();
							x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
							if (x < FriendsTransmissionRate) {
									friend.dayOfContamination = day;
									friend.infected = true;
									friend.virusGeneration = (short)(ind.virusGeneration + 1);
									friend.howInfected = HowInfected.BYFRIEND;
									newInfecteds++;
							}
						}

					}
				}
			}
		}
		// Death
		if (ind.isContagious()) {
			if (day - ind.dayOfContamination >= DayOfFirstSevereSymptom) {// death comes after Severe symptoms
				double death = rnd.nextDouble();
				if (ind.age < Individu.MAXAGESTUDENT) {
					// youngs
					if (death < DeathRateYoung) {
						ind.alive = false;
						newDeads++;
					}
				} else if (ind.age < Individu.MINAGESENIOR) {
					// adults
					if (death < DeathRateAdult) {
						ind.alive = false;
						newDeads++;
					}
				} else {
					// seniors
					if (death < DeathRateSenior) {
						ind.alive = false;
						newDeads++;
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
	
	private double virusAttenuation(int generation) {
		return 1.5 - (1.0 / (1.0 + Math.exp(-(generation/10.0))));
	}
}
