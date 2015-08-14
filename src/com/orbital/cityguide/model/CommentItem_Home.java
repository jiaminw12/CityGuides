package com.orbital.cityguide.model;

public class CommentItem_Home {

	private String comment_id;
	private String attr_id;
	private String attr_title;
	private String title;
	private String description;
	private String rate;
	private String usr_img;

	public CommentItem_Home() {
	}

	public CommentItem_Home(String comment_id, String attr_id, String attr_title, String usr_img, String title, String rate, String description) {
		this.comment_id = comment_id;
		this.attr_id = attr_id;
		this.attr_title = attr_title;
		this.usr_img = usr_img;
		this.title = title;
		this.rate = rate;
		this.description = description;
	}
	
	public String getCommentID() {
		return this.comment_id;
	}

	public void setCommentID(String comment_id) {
		this.comment_id = comment_id;
	}
	
	public String getAttrID() {
		return this.attr_id;
	}

	public void setAttrID(String attr_id) {
		this.attr_id = attr_id;
	}
	
	public String getAttrTitle() {
		return this.attr_title;
	}

	public void setAttrTitle(String attr_title) {
		this.attr_title = attr_title;
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

	public String getUsr_img() {
		return this.usr_img;
	}

	public void setUsr_img(String usr_img) {
		this.usr_img = usr_img;
	}
}
