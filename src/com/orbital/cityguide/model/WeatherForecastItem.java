package com.orbital.cityguide.model;

public class WeatherForecastItem {

	private String date;
	private String iconString;
	private String temperature;

	public WeatherForecastItem() {
	}

	public WeatherForecastItem(String date, String iconString, String temperature) {
		this.date = date;
		this.iconString = iconString;
		this.temperature = temperature;
	}

	public String getDate() {
		return this.date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getIconString() {
		return this.iconString;
	}

	public void setIconString(String iconString) {
		this.iconString = iconString;
	}

	public String getTemperature() {
		return this.temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

}
