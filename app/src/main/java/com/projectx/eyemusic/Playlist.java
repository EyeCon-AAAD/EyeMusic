package com.projectx.eyemusic;

import androidx.annotation.NonNull;

class Playlist {
    private String name;
    private String spotifyURI;

    public Playlist(String name, String spotifyURI) {
        this.name = name;
        this.spotifyURI = spotifyURI;
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

    @NonNull
    @Override
    public String toString() {
        return "Playlist name: " + this.name + "\nSPotify URI: " + this.spotifyURI;
    }
}
