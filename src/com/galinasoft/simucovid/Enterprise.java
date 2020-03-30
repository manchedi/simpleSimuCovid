package com.galinasoft.simucovid;

import java.util.HashSet;
import java.util.Set;

public class Enterprise {
	Set<Individu> colleagues;
	
	public Enterprise() {
		colleagues = new HashSet<Individu>();
	}
	
	public void add(Individu ind) {
		colleagues.add(ind);
	}

}
