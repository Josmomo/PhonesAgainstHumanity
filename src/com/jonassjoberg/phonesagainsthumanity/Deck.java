package com.jonassjoberg.phonesagainsthumanity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

public class Deck {
	private HashMap<String, Card> deck;
	private	ArrayList<Integer> deckOrder;
	Properties properties;
	private Integer color;;
	long seed;

	public Deck(Context context, Integer color, String fileName){
		deck = new HashMap<String, Card>();
		deckOrder = new ArrayList<Integer>();
		properties = new Properties();
		this.color = color;
		seed = System.nanoTime();

		Resources resources = context.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
		    InputStream inputStream = assetManager.open(fileName);
		    properties.load(inputStream);
		    System.out.println("The properties are now loaded");
		    System.out.println("properties: " + properties);
		} catch (IOException e) {
		    System.err.println("Failed to open microlog property file");
		    e.printStackTrace();
		}

		// Populate deck
		for (String key : properties.stringPropertyNames()) {
			String value = properties.getProperty(key);
			Card c = new Card(Integer.parseInt(key), color, value);
			deck.put(key, c);
			Log.d(this.getClass().getName(), "key lika med " + key);
			Log.d(this.getClass().getName(), "value lika med " + value);
		}

		// ArrayList with random card order
		for (int i=1; i<=deck.size(); i++) {
			deckOrder.add(i);
		}
		Collections.shuffle(deckOrder, new Random(seed));
		
	}

	public Integer getColor() {
		return color;
	}
	
	/**
	 * Picks the next Card from the deck and removes it.
	 * @return a Card
	 */
	public Card nextCard() {
		
		Integer size = deckOrder.size() - 1;
		if (size < 0) {
			Log.d(this.getClass().getName(), "size < 0, return empty Card");
			return new Card();
		}
		
		// Take the top card and remove it
		// key has to be a String
		return deck.get(deckOrder.remove((int)size).toString());
	}


}
