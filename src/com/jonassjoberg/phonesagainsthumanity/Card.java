package com.jonassjoberg.phonesagainsthumanity;

public class Card {
	private Integer id;
	private Integer color;
	private String text;

	public Card() {
		id = 0;
		color = 2;
		text = "";
	}

	public Card(Integer id, Integer color, String text) {
		this.id = id;
		this.color = color;
		this. text = text;
	}
	
	public Integer getId() {
		return id;
	}
	
	public Integer getColor() {
		return color;
	}
	
	public String getText() {
		return text;
	}
}
