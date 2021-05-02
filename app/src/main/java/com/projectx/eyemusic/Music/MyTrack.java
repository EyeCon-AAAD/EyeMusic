package com.projectx.eyemusic.Music;

// create MyTrack class synonymous to Track in the Spotify SDK
// Easier for now, will setup for de/serialization later
public class MyTrack {
    private String artistName;
    private String trackName;
    private String imageURL;
    private String spotifyURI;
    private Long duration_ms;

    public MyTrack(String artistName, String trackName, String imageURL, String spotifyURI, Long duration_ms) {
        this.artistName = artistName;
        this.trackName = trackName;
        this.imageURL = imageURL;
        this.spotifyURI = spotifyURI;
        this.duration_ms = duration_ms;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getSpotifyURI() {
        return spotifyURI;
    }

    public void setSpotifyURI(String spotifyURI) {
        this.spotifyURI = spotifyURI;
    }

    public Long getDuration_ms() {
        return duration_ms;
    }

    public void setDuration_ms(Long duration_ms) {
        this.duration_ms = duration_ms;
    }
}
