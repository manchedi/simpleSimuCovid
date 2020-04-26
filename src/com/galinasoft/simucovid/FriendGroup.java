package com.galinasoft.simucovid;

import java.util.ArrayList;
import java.util.List;


public class FriendGroup {
	List<Individu> relations;
	
	public FriendGroup(int initialCapacity) {
		relations = new ArrayList<Individu>(initialCapacity);
	}
	
	public void add(Individu ind) {
		relations.add(ind);
	}

}
