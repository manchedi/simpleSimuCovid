package com.galinasoft.simucovid;

import java.util.HashSet;
import java.util.Set;

public class Family {
	Set<Individu> members;
	
	public Family() {
		members = new HashSet<Individu>();
	}
	
	public void add(Individu ind) {
		members.add(ind);
	}
}
