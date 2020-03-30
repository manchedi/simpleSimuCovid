package com.galinasoft.simucovid;

import java.util.HashSet;
import java.util.Set;

public class School {
	Set<Individu> students;
	
	public School() {
		students = new HashSet<Individu>();
	}
	
	public void add(Individu ind) {
		students.add(ind);
	}

}