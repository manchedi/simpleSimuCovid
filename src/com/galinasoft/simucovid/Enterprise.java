package com.galinasoft.simucovid;

import java.util.ArrayList;
import java.util.List;


public class Enterprise {
	List<Individu> colleagues;
	
	public Enterprise(int initialCapacity) {
		colleagues = new ArrayList<Individu>(initialCapacity);
	}
	
	public void add(Individu ind) {
		colleagues.add(ind);
	}

}
