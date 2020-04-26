from Simucovid import Simu

class Individu:
	MAXAGESTUDENT = 30 # tune it for specify % of young people
	MINAGESENIOR = 65 # tune it for specify % of young people
	
	def __init__(self, age, school, work, family, friendGroup):
		self.age = age
		self.school = school
		self.enterprise = work
		self.family = family
		self.friendGroup = friendGroup
		self.alive = True
		self.infected = False
		self.immunized = False
		self.dayOfContamination = -1
		self.howInfected = -1
	
	def isWorking(self):
		return ((self.age % 4 < 3) and not self.isSenior())
	
	
	def isSenior(self):
		return self.age >= self.MINAGESENIOR
	
	
	def isStudent(self):
		return self.age <= self.MAXAGESTUDENT
	
	
	def isContagious(self):
		return (self.alive and self.infected and not self.immunized)
	
	def isInfectable(self):
		return self.alive and not self.infected and not self.immunized
	
	def isDetected(self, day):
		return (self.alive and self.infected and (day - self.dayOfContamination) > Simu.DayOfFirstSign)
	

