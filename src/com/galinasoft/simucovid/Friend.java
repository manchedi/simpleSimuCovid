package com.galinasoft.simucovid;

import java.util.HashSet;
import java.util.Set;

public class Friend {
	Set<Individu> relations;
	
	public Friend() {
		relations = new HashSet<Individu>();
	}
	
	public void add(Individu ind) {
		relations.add(ind);
	}

}
