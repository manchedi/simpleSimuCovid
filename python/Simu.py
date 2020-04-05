import random
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from Individu import *
from Family import *
from Friend import *
from School import *
from Enterprise import *
	
def main():	
	random.seed(42)
	simu = Simu()
	simu.createFamilies()
	simu.createEnterprises()
	simu.createSchools()
	simu.createFriends()
	simu.fillGroup()
	simu.initFirstCases()
	
	simu.run(90)

	
class Simu:
	# how is infected
	DayOfFirstSign = 5
	
	def __init__ (self):
		self.SIZE = 1000
		self.FAMILYSIZE = 4 # 4 person per family
		self.ENTERPRISESIZE = 20 # 20 workers per enterprise
		self.N_ENTERPRISE = int(self.SIZE/self.ENTERPRISESIZE)
		self.SCHOOLSIZE = 200 # 200 students per school
		self.N_SCHOOL = int(self.SIZE/self.SCHOOLSIZE)
		self.FRIENDGROUPSIZE = 4 # 4 friends per individual
		self.N_FRIENDGROUP = int(self.SIZE/self.FRIENDGROUPSIZE)
		self.FamilyTransmissionRate = 0.05
		self.EnterpriseTransmissionRate = 0.01
		self.SchoolTransmissionRate = 0.02
		self.FriendsTransmissionRate = 0.05
		self.MAXAGE = 80  # use self parameter to tune % of senior (MAXAGE - 65)/ (MAXAGE)
		self.DeathRateYoung = 0.0002
		self.DeathRateAdult = 0.0012
		self.DeathRateSenior = 0.0039
		self.DayOfFirstSevereSymptom = 9
		self.RECOVERYTIME = 21
		self.FirstDayOfContainment = 4
		self.DurationOfContainment = 30
		self.FirstDayOfTotalContainment = 140
		self.DurationOfTotalContainment = 10
		
		self.group = []
		self.families = []
		self.enterprises = []
		self.schools = []
		self.friends = []
	
		self.df = pd.DataFrame(columns=["day","infected","immunized","dead","InfectByFamily","InfectByWork","InfectBySchool","InfectByFriend","newCases","totalInfected"], dtype=np.int32)
	
	def createFamilies(self):
		for f in range(int(self.SIZE/self.FAMILYSIZE)):
			self.families.append(Family())

	def createEnterprises(self):
		for e in  range(self.N_ENTERPRISE):
			self.enterprises.append(Enterprise())
	
	def createSchools(self):
		for s in range(self.N_SCHOOL):
			self.schools.append(School())

	def createFriends(self):
		for f in range(self.N_FRIENDGROUP):
			self.friends.append(Friend())
	
	def fillGroup(self):
		
		for i in range(self.SIZE):
			age = random.randrange(0, self.MAXAGE) + 1
			school = random.randrange(0, self.N_SCHOOL)
			work = random.randrange(0, self.N_ENTERPRISE)
			family = random.randrange(0, self.SIZE/self.FAMILYSIZE)
			friendgroup = random.randrange(0, self.N_FRIENDGROUP)
			self.group.append(Individu(age, school, work, family, friendgroup))
			self.families[family].add(self.group[i])
			self.friends[friendgroup].add(self.group[i])
			if self.group[i].isStudent():
				self.schools[school].add(self.group[i])
			elif self.group[i].isWorking():
				self.enterprises[work].add(self.group[i])
			

	
	def initFirstCases(self):
		i0 = self.group[40]
		i0.infected = True
		i0.dayOfContamination=0
		i0.age = 40;  # multiple de 4 = isWorking
		i0.howInfected = "BYWORK"
		
		i1 = self.group[20]
		i1.infected = True
		i1.dayOfContamination = 0
		i1.age = 12 # multiple de 4 = isWorking
		i1.howInfected = "BYSCHOOL"

	
	def run(self, ndays):
		print("\"day\",\"infected\",\"immunized\",\"dead\",\"InfectByFamily\",\"InfectByWork\",\"InfectBySchool\",\"InfectByFriend\",\"newCases\",\"totalInfected\"")
		
		totalInfecteds = 0
		
		# every day
		for day in range(ndays):
			# every person
			infecteds = 0
			immunizeds = 0
			deads = 0
			infectedByFamily = 0
			infectedByWork = 0
			infectedBySchool = 0
			infectedByFriend = 0

			newCases = 0
			for i in range(len(self.group)):
				ind = self.group[i]
				newCases += self.newCasesOfADay(day, ind)	
				# healing
				if ind.infected and (day > (ind.dayOfContamination + self.RECOVERYTIME)):
					ind.infected = False
					ind.immunized = True
				
				if (ind.isContagious()):
					infecteds += 1
					if (ind.howInfected == "BYWORK"):
						infectedByWork += 1
					elif (ind.howInfected == "BYFAMILY"):
						infectedByFamily += 1
					elif (ind.howInfected == "BYSCHOOL"):
						infectedBySchool += 1
					elif (ind.howInfected == "BYFRIEND"):
						infectedByFriend += 1
									
				if (ind.alive and ind.immunized):
					immunizeds += 1
				if not ind.alive: deads += 1
			
			totalInfecteds += newCases
			# output csv
			print(f"{day},{infecteds},{immunizeds},{deads},{infectedByFamily},{infectedByWork},{infectedBySchool},{infectedByFriend},{newCases},{totalInfecteds}")
			# pandas Dataframe
			df2 = pd.DataFrame([[day, infecteds, immunizeds, deads, infectedByFamily, infectedByWork, infectedBySchool, infectedByFriend, newCases, totalInfecteds]],
				columns=["day","infected","immunized","dead","InfectByFamily","InfectByWork","InfectBySchool","InfectByFriend","newCases","totalInfected"])
			self.df = pd.concat([self.df, df2])
		
		# result how many infected
		howManyInfected = 0
		howManyImmunized = 0
		howManyDead = 0
		for i in range(len(self.group)):
			ind = self.group[i]
			if ind.isContagious(): howManyInfected += 1
			if ind.alive and ind.immunized: howManyImmunized += 1
			if not ind.alive: howManyDead+= 1;
		
		print("Total infected : {}".format(howManyInfected))
		print("Total immunized: {}".format(howManyImmunized))
		print("Total deads: {}".format(howManyDead))

		fig, (ax1, ax2) = plt.subplots(2, sharex=False, sharey=False, figsize=(5, 8))
		fig.suptitle('Covid-19 Simulation 30d@4 containment')
		# ax1.subplot(111)
		ax1.plot(self.df['day'], self.df['infected'])
		ax1.plot(self.df['day'], self.df['InfectByFamily'])
		ax1.plot(self.df['day'], self.df['InfectByWork'])
		ax1.plot(self.df['day'], self.df['InfectBySchool'])
		ax1.plot(self.df['day'], self.df['InfectByFriend'])
		ax1.plot(self.df['day'], self.df['immunized'])
		ax1.set(xlabel='days', ylabel='individuals')
		plt.xlabel('days')
		ax1.legend(['infected','byFamily','byWork','bySchool','byFriend','immunized'])
		# plt.subplot(112)
		ax2.plot(self.df['totalInfected'], self.df['newCases'], label='infected')
		ax2.set(xscale='log')
		ax2.set(yscale='log')
		ax2.legend()
		ax2.set(xlabel='total Infected (log)', ylabel='new cases (log)')
		plt.show()


	
	def newCasesOfADay(self, day, ind):
		newCases = 0
		dayOfWeek = day % 7

		if dayOfWeek < 5:
			# week days
			if ind.isContagious():
				if ind.isWorking() and not ind.isDetected(day) and not self.isTotalContainment(day): #si detecte malade on travaille pas, si gueri on retravaille
					# Enterprise
					if not ind.isStudent():
						for colleague in self.enterprises[ind.enterprise].colleagues:
							if colleague is not ind and colleague.alive and not colleague.isDetected(day): # si detecte malade on travaille pas
								x = random.random()								
								if self.isContainment(day): x /= 3	# effectif reduit en entreprise
								if x < self.EnterpriseTransmissionRate:
									if not colleague.infected and not colleague.immunized:
										colleague.dayOfContamination = day
										colleague.infected = True
										colleague.howInfected = "BYWORK"
										newCases += 1
									

					# School
					if ind.isStudent() and not self.isContainment(day):  # en confinement y a pas �cole
						for student in self.schools[ind.school].students:
							if student is not ind and student.alive:
								x = random.random()
								if x < self.SchoolTransmissionRate:
									if not student.infected and not student.immunized:
										student.dayOfContamination = day
										student.infected = True
										student.howInfected = "BYSCHOOL"
										newCases += 1
									
				
				# family
				for member in self.families[ind.family].members:
					if member is not ind:
						x = random.random()
						if self.isContainment(day): x *= 3 # containment increase contamination inside family
						if x < self.FamilyTransmissionRate:
							if not member.infected and not member.immunized:
								member.dayOfContamination = day
								member.infected = True
								member.howInfected = "BYFAMILY"
								newCases += 1

		else:
			# week-end
			# family
			if ind.isContagious():
				for member in self.families[ind.family].members:
					if member is not ind:
						x = random.random()
						if self.isContainment(day): x *= 3 # containment increase contamination inside family
						if x < self.FamilyTransmissionRate:
							if member.isInfectable():
								member.dayOfContamination = day
								member.infected = True
								member.howInfected = "BYFAMILY"
								newCases += 1

				#week-end Friends
				if not self.isContainment(day):
					for friend in self.friends[ind.friendGroup].relations:
						if friend is not ind:
							x = random.random()
							if x < self.FriendsTransmissionRate:
								if friend.isInfectable():
									friend.dayOfContamination = day
									friend.infected = True
									friend.howInfected = "BYFRIEND"
									newCases += 1

		# Death 
		if ind.isContagious():
			if day - ind.dayOfContamination >= self.DayOfFirstSevereSymptom: # death comes after Severe symptoms
				death = random.random()
				if ind.age <= 30:
					# youngs
					if death < self.DeathRateYoung:
						ind.alive = False
				elif ind.age <= 60:
					# adults
					if death < self.DeathRateAdult:
						ind.alive = False
				else:
					# seniors
					if death < self.DeathRateSenior:
						ind.alive = False


		return newCases

	
	def isContainment(self, day):
		return day >= self.FirstDayOfContainment and day <= (self.FirstDayOfContainment + self.DurationOfContainment)
	
	def isTotalContainment(self, day):
		return day >= self.FirstDayOfTotalContainment and day <= (self.FirstDayOfTotalContainment + self.DurationOfTotalContainment)


if __name__ == "__main__":
    main()
	
