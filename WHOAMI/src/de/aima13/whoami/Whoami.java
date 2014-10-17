package de.aima13.whoami;

import de.aima13.whoami.modules.Food;

/**
 * Created by D060469 on 16.10.14.
 */
public class Whoami {
	public static void main(String[] args) {

		//Achtung nur zum testen
		//++++++++++++++++++++++++++++++++
		Food f=new Food();
		Thread t = new Thread(f);//++++
		t.start();
		System.out.println("Fertich");
		//++++++++++++++++++++++++++++++++

	}
}
