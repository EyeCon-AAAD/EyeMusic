package com.projectx.eyemusic.Music;

import androidx.annotation.NonNull;

public class Playlist {
    private String name;
    private String id;
    private String spotifyURI;
    private String imageURL;

    public Playlist(String name, String id, String spotifyURI, String imageURL) {
        this.name = name;
        this.id = id;
        this.spotifyURI = spotifyURI;
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpotifyURI() {
        return spotifyURI;
    }

    public void setSpotifyURI(String spotifyURI) {
        this.spotifyURI = spotifyURI;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @NonNull
    @Override
    public String toString() {
        return "\nPlaylist name: " + this.name + "\nid: "+ this.id +  "\nSPotify URI: " + this.spotifyURI + "\nImage URL: " + this.imageURL;
    }
}
