package com.tomsfreelance.spotifystreamer.Model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Created by teynon on 6/1/2015.
 */
public class PlaybackArtist implements Parcelable {
    public String ArtistID;
    public String ArtistName;
    public String ArtistImage;

    public PlaybackArtist(Artist artist) {
        ArtistID = artist.id;
        ArtistName = artist.name;
        if (artist.images.size() > 0) {
            ArtistImage = artist.images.get(artist.images.size() - 1).url;
        }
    }

    public PlaybackArtist(String artistID, String artistName, String artistImage) {
        ArtistID = artistID;
        ArtistName = artistName;
        ArtistImage = artistImage;
    }

    public PlaybackArtist(Parcel in) {
        ReadFromParcel(in);
    }

    public static final Parcelable.Creator<PlaybackArtist> CREATOR = new Parcelable.Creator<PlaybackArtist>() {
        public PlaybackArtist createFromParcel(Parcel in ) {
            return new PlaybackArtist( in );
        }

        public PlaybackArtist[] newArray(int size) {
            return new PlaybackArtist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ArtistID);
        dest.writeString(ArtistName);
        dest.writeString(ArtistImage);
    }

    private void ReadFromParcel(Parcel in) {
        ArtistID = in.readString();
        ArtistName = in.readString();
        ArtistImage = in.readString();
    }
}
