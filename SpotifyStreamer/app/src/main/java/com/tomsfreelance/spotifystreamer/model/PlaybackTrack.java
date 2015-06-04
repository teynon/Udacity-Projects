package com.tomsfreelance.spotifystreamer.Model;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by gabriella on 5/31/2015.
 */

// Reference: http://stackoverflow.com/a/6923794/697477
public class PlaybackTrack implements Parcelable {
    public String TrackID;
    public String SongName;
    public String Artist;
    public String AlbumID;
    public String AlbumName;
    public long TrackLength;
    public String AlbumImage;
    public String AlbumImageSmall;
    public String StreamURL;

    public PlaybackTrack(Track fromTrack) {
        TrackID = fromTrack.id;
        SongName = fromTrack.name;

        for (ArtistSimple t : fromTrack.artists) {
            if (Artist == null) Artist = t.name;
            else Artist += " | " + t.name;
        }

        AlbumID = fromTrack.album.id;
        AlbumName = fromTrack.album.name;
        TrackLength = fromTrack.duration_ms;
        if (fromTrack.album.images.size() > 0) {
            AlbumImage = fromTrack.album.images.get(0).url;
            AlbumImageSmall = fromTrack.album.images.get(fromTrack.album.images.size() - 1).url;
        }
        StreamURL = fromTrack.preview_url.replace("https://", "http://");
    }

    public PlaybackTrack(Parcel in) {
        ReadFromParcel(in);
    }


    public static final Parcelable.Creator<PlaybackTrack> CREATOR = new Parcelable.Creator<PlaybackTrack>() {
        public PlaybackTrack createFromParcel(Parcel in ) {
            return new PlaybackTrack( in );
        }

        public PlaybackTrack[] newArray(int size) {
            return new PlaybackTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TrackID);
        dest.writeString(SongName);
        dest.writeString(Artist);
        dest.writeString(AlbumID);
        dest.writeString(AlbumName);
        dest.writeLong(TrackLength);
        dest.writeString(AlbumImage);
        dest.writeString(AlbumImageSmall);
        dest.writeString(StreamURL);
    }

    private void ReadFromParcel(Parcel in) {
        TrackID = in.readString();
        SongName = in.readString();
        Artist = in.readString();
        AlbumID = in.readString();
        AlbumName = in.readString();
        TrackLength = in.readLong();
        AlbumImage = in.readString();
        AlbumImageSmall = in.readString();
        StreamURL = in.readString();
    }
}
