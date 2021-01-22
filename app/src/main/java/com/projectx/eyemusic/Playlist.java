package com.projectx.eyemusic;

import androidx.annotation.NonNull;

class Playlist {
    private String name;
    private String spotifyURI;
    private String imageURL;

    public Playlist(String name, String spotifyURI, String imageURL) {
        this.name = name;
        this.spotifyURI = spotifyURI;
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "\nPlaylist name: " + this.name + "\nSPotify URI: " + this.spotifyURI + "\nImage URL: " + this.imageURL;
    }
}
