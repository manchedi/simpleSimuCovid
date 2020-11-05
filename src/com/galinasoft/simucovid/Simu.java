package com.galinasoft.simucovid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class Simu {
	// how is infected
	final int nDaysOfSimulation = 600;
	final int SIZE= 10_000_000;
	final int FAMILYSIZE = 4; // 4 person per family
	final int ENTERPRISESIZE = 20; // 20 workers per enterprise
	final int N_ENTERPRISE = SIZE/ENTERPRISESIZE; 
	final int SCHOOLSIZE = 200; // 200 students per school
	final int N_SCHOOL = SIZE/SCHOOLSIZE; 
	final int FRIENDGROUPSIZE = 4; // 4 friends per individual
	final int N_FRIENDGROUP = SIZE/FRIENDGROUPSIZE; 
	final double globalVirusRate = 1.0;
	final double familyTransmissionRate = 0.3 * globalVirusRate;
	final double enterpriseTransmissionRate = 0.01 * globalVirusRate;
	final double schoolTransmissionRate = 0.02 * globalVirusRate;
	final double friendsTransmissionRate = 0.03 * globalVirusRate;
	final int MAXAGE = 75;  // use this parameter to tune % of senior (MAXAGE - 65)/ (MAXAGE)
	final double deathRateYoung = 0.0002;
	final double deathRateAdult = 0.0008;
	final double deathRateSenior = 0.0039;
	final static int dayOfFirstSign = 10;
	final static int dayOfFirstSevereSymptom = dayOfFirstSign + 5;
	final static int dayStartOfContagiousness = 7;
	final int RECOVERYTIME = dayOfFirstSevereSymptom + 5;
	//final int firstDayOfContainment = 150;
	//final int durationOfContainment = 60;
	final int lockPeriods[][] = {{80,60},{1230,30},{1290,30}};  // {daystart, duty}
	final int firstDayOfTotalContainment = 999;
	final int durationOfTotalContainment = 20;
	final int endOfEpidemiaAfterNdays  = 5; // days without infected
	Individu[] population;
	Family[] families;
	Enterprise[] enterprises;
	School[] schools;
	FriendGroup[] friendGroups;
	int newInfecteds = 0;
	int newDeads = 0;
	int nDaysWithoutInfection = 0;
	
	Random rnd = new Random(42);
	
	public Simu() {
		this.population = new Individu[SIZE];
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
		simu.fillPopulation();
		simu.initFirstCases();
		
		try(BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\Agraver\\sk-learn\\covid\\Reqsimu19.csv"));) {
			simu.run(bw);			
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
	
	public void fillPopulation() {
		
		for (int i=0; i<population.length ; i++) {
			int age = rnd.nextInt(MAXAGE) + 1;
			int school = rnd.nextInt(N_SCHOOL);
			int work = rnd.nextInt(N_ENTERPRISE);
			int family = rnd.nextInt((int)(SIZE/FAMILYSIZE));
			int friendGroup = rnd.nextInt(N_FRIENDGROUP);
			population[i] = new Individu((short)age, school, work, family, friendGroup);
			families[family].add(population[i]);
			friendGroups[friendGroup].add(population[i]);
			if (population[i].isStudent()) {
				schools[school].add(population[i]);
			} else if (population[i].isWorking()) {
				enterprises[work].add(population[i]);
			}
		}
	}
	
	private void initFirstCases() {
		Individu i0 = population[40];
		i0.infected = true;
		i0.virusGeneration = 1;
		i0.dayOfContamination = -5;
		i0.age = 40; // multiple de 4 = isWorking
		i0.howInfected = HowInfected.BYWORK;

		Individu i1 = population[20];   
		i1.infected = true;
		i1.virusGeneration = 1;
		i1.dayOfContamination = -1;
		i1.age = 12; // multiple de 4 = isWorking
		i1.howInfected = HowInfected.BYSCHOOL;
		
		Individu i2 = population[23];   // 20 years old
		i2.infected = true;
		i2.dayOfContamination = -8;
		i2.age = 23; // multiple de 4 = isWorking
		i2.howInfected = HowInfected.BYSCHOOL;
		
		Individu i3 = population[70];   // 20 years old
		i3.infected = true;
		i3.dayOfContamination = 0;
		i3.age = 70; // multiple de 4 = isWorking
		i3.howInfected = HowInfected.BYFAMILY;
		
		Individu i5 = population[79];   // 20 years old
		i5.infected = true;
		i5.dayOfContamination = -4;
		i5.age = 8; // multiple de 4 = isWorking
		i5.howInfected = HowInfected.BYFAMILY;
		
		Individu i4 = population[75];   // 20 years old
		i4.infected = true;
		i4.dayOfContamination = -7;
		i4.age = 52; // multiple de 4 = isWorking
		i4.howInfected = HowInfected.BYFAMILY;

	}
	
	public void run(BufferedWriter bw) throws IOException {
		
		System.out.println("\"day\",\"infected\",\"immunized\",\"dead\",\"InfectByFamily\",\"InfectByWork\",\"InfectBySchool\",\"InfectByFriend\",\"newCases\",\"totalInfected\",\"newDeads\",\"R0\"");
		bw.write("\"day\",\"infected\",\"immunized\",\"dead\",\"InfectByFamily\",\"InfectByWork\",\"InfectBySchool\",\"InfectByFriend\",\"newCases\",\"totalInfected\",\"newDeads\",\"R0\"\n");

		int totalInfecteds = 0;
		// every day
		for (short day=0 ; day < nDaysOfSimulation ; day++) {
			int infecteds = 0;
			int immunizeds = 0;
			int deads = 0;
			int infectedByFamily = 0;
			int infectedByWork = 0;
			int infectedBySchool = 0;
			int infectedByFriend = 0;
			double R0 = 0;
			int qR0 = 0;
			int nR0 = 0;
			newInfecteds = 0;
			newDeads = 0;
			
			// every person			
			for (Individu ind : population) {
				// contamination
				IndividualPropagationByDay(day, ind);
				
				// healing
				if (ind.infected && day > ind.dayOfContamination + RECOVERYTIME) {
					ind.infected = false;
					ind.immunized = true;
				}
				if (ind.isContagious(day)) {
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
					default: break;
					}					
				}
				if (ind.howManyInfected > 0) {
					qR0 += ind.howManyInfected;
					nR0++;
				}
				if (ind.alive && ind.immunized) immunizeds++;
				if (!ind.alive) deads++;
			}
			totalInfecteds += newInfecteds;
			R0 = (double)(qR0)/nR0;
			System.out.println(day + "," + infecteds + "," + immunizeds + "," + deads + "," + infectedByFamily + "," + infectedByWork + "," + infectedBySchool + "," + infectedByFriend + "," + newInfecteds + ',' + totalInfecteds + "," + newDeads + "," + String.format(Locale.US, "%3.2f",R0));
			System.out.println(day + " R0=" + R0);
			bw.write(day + "," + infecteds + "," + immunizeds + "," + deads + "," + infectedByFamily + "," + infectedByWork + "," + infectedBySchool + "," + infectedByFriend + "," + newInfecteds + ',' + totalInfecteds + "," + newDeads + "," + String.format(Locale.US, "%3.2f",R0) + "\n");

			if (newInfecteds == 0) {
				nDaysWithoutInfection++;
			} else {
				nDaysWithoutInfection = 0;
			}
			if (nDaysWithoutInfection > endOfEpidemiaAfterNdays) break;// end of epidemia 
		}
		
		// result how many infected
		int howManyInfected = 0;
		int howManyImmunized = 0;
		int howManyDead = 0;
		int InfecByVirusGeneration[] = new int[102];
		for (Individu ind : population) {
			if (ind.isInfected()) howManyInfected++;
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
			if (ind.isContagious(day)) {
				if (ind.isWorking() && !ind.isDetected(day) && !isTotalContainment(day)) { // si détecté malade on ne travaille pas, si guéri on retravaille
					// Enterprise
					if (!ind.isStudent()) {
						for (Individu colleague : enterprises[ind.enterprise].colleagues) {
							if (colleague.isInfectable()) { // si détecté malade on ne travaille pas
								double x = rnd.nextDouble();
								if (isContainment(day)) x /= 3.0; // staff is reduced so rate is reduced
								x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
								if (x < enterpriseTransmissionRate) {
									colleague.dayOfContamination = day;
									colleague.infected = true;
									colleague.virusGeneration = (short)(ind.virusGeneration + 1);
									colleague.howInfected = HowInfected.BYWORK;
									newInfecteds++;
									ind.howManyInfected++;
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
								if (x < schoolTransmissionRate) {
									student.dayOfContamination = day;
									student.infected = true;
									student.virusGeneration = (short)(ind.virusGeneration + 1);
									student.howInfected = HowInfected.BYSCHOOL;
									newInfecteds++;
									ind.howManyInfected++;
								}
							}
						}

					}
				}
				// family all the week
				for (Individu member : families[ind.family].members) {
					if (member.isInfectable()) {
						double x = rnd.nextDouble();
						if (isContainment(day)) x /= 3; // containment increase contamination inside family						
						x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
						if (x < familyTransmissionRate) {
							member.dayOfContamination = day;
							member.infected = true;
							member.virusGeneration = (short)(ind.virusGeneration + 1);
							member.howInfected = HowInfected.BYFAMILY;
							newInfecteds++;
							ind.howManyInfected++;
						}
					}
				}
			} // alive

		} else {
			// week-end
			// family
			if (ind.isContagious(day)) {
				for (Individu member : families[ind.family].members) {
					if (member.isInfectable()) {
						double x = rnd.nextDouble();
						if (isContainment(day))	x /= 3; // containment increase contamination inside family
						x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
						if (x < familyTransmissionRate) {
							member.dayOfContamination = day;
							member.infected = true;
							member.virusGeneration = (short)(ind.virusGeneration + 1);
							member.howInfected = HowInfected.BYFAMILY;
							newInfecteds++;
							ind.howManyInfected++;
						}
					}
				}
				// week-end Friends
				if (!isContainment(day)) {
					for (Individu friend : friendGroups[ind.friendGroup].relations) {
						if (ind.isInfectable()) {
							double x = rnd.nextDouble();
							x = x / virusAttenuation(ind.virusGeneration);   // virus mutability decrease propagation
							if (x < friendsTransmissionRate) {
									friend.dayOfContamination = day;
									friend.infected = true;
									friend.virusGeneration = (short)(ind.virusGeneration + 1);
									friend.howInfected = HowInfected.BYFRIEND;
									newInfecteds++;
									ind.howManyInfected++;
							}
						}

					}
				}
			}
		}
		// Death
		if (ind.isContagious(day)) {
			if (day - ind.dayOfContamination >= dayOfFirstSevereSymptom) {// death comes after Severe symptoms
				double death = rnd.nextDouble();
				if (ind.age < Individu.MAXAGESTUDENT) {
					// youngs
					if (death < deathRateYoung) {
						ind.alive = false;
						newDeads++;
					}
				} else if (ind.age < Individu.MINAGESENIOR) {
					// adults
					if (death < deathRateAdult) {
						ind.alive = false;
						newDeads++;
					}
				} else {
					// seniors
					if (death < deathRateSenior) {
						ind.alive = false;
						newDeads++;
					}
				}
			}
		}
	}
	
	private boolean isContainment(int day) {
		
		for (int period=0 ; period < lockPeriods.length; period++) {
			if (day >= lockPeriods[period][0] && day < (lockPeriods[period][0]+lockPeriods[period][1])) {
				return true;
			}
		}
		return false;
	}
	private boolean isTotalContainment(int day) {
		return day >= firstDayOfTotalContainment && day <= (firstDayOfTotalContainment + durationOfTotalContainment);
	}
	
	private double virusAttenuation(int generation) {
		//return 1.5 - (1.0 / (1.0 + Math.exp(-(generation/10.0))));
		return 1.0;
	}
}
