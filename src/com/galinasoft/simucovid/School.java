package com.galinasoft.simucovid;

import java.util.ArrayList;
import java.util.List;


public class School {
	List<Individu> students;
	
	public School(int initialCapacity) {
		students = new ArrayList<Individu>(initialCapacity);
	}
	
	public void add(Individu ind) {
		students.add(ind);
	}

}