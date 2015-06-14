package com.orbital.cityguide.model;

public class CommentItem {

	private String title;
	private String description;
	private String rate;
	private String date;
	private String usr_img;
	private String username;

	public CommentItem() {
	}

	public CommentItem(String usr_img, String title, String rate,
			String username, String description, String date) {
		this.usr_img = usr_img;
		this.title = title;
		this.rate = rate;
		this.username = username;
		this.description = description;
		this.date = date;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRate() {
		return this.rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUsr_img() {
		return this.usr_img;
	}

	public void setUsr_img(String usr_img) {
		this.usr_img = usr_img;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
