package com.example.nav.navmap.models;

/**
 * Created by nav on 3/10/16.
 */

public class Sys {
    private String message;

    private String sunset;

    private String sunrise;

    private String country;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "ClassPojo [message = " + message + ", sunset = " + sunset + ", sunrise = " + sunrise + ", country = " + country + "]";
    }
}
