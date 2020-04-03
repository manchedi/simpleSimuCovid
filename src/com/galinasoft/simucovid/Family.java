package com.galinasoft.simucovid;

import java.util.ArrayList;
import java.util.List;


public class Family {
	List<Individu> members;
	
	public Family(int initialCapacity) {
		members = new ArrayList<Individu>(initialCapacity);
	}
	
	public void add(Individu ind) {
		members.add(ind);
	}
}
