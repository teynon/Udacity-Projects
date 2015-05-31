package com.tomsfreelance.spotifystreamer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tomsfreelance.spotifystreamer.R;
import com.tomsfreelance.spotifystreamer.model.PlaybackTrack;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by gabriella on 5/30/2015.
 */
public class TrackResultAdapter extends ArrayAdapter<PlaybackTrack> {
    private Context ctx = null;
    private ArrayList<PlaybackTrack> Tracks = null;

    public TrackResultAdapter(Context context, ArrayList<PlaybackTrack> tracks) {
        super(context, R.layout.track_result, tracks);
        ctx = context;
        Tracks = tracks;
    }

    public ArrayList<PlaybackTrack> GetTracks() {
        return Tracks;
    }

    // Used http://stackoverflow.com/questions/11927967/dynamically-populate-the-linear-layout-depending-upon-markers-on-the-map
    // as a reference.
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        TrackHolder holder = null;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.track_result, null, false);
            holder = new TrackHolder();
            holder.txtSongName = (TextView)view.findViewById(R.id.txtSongName);
            holder.txtAlbumName = (TextView)view.findViewById(R.id.txtSongAlbum);
            holder.imgAlbum = (ImageView)view.findViewById(R.id.imgAlbumIcon);
            view.setTag(holder);
        }
        else {
            holder = (TrackHolder)view.getTag();
        }

        if (holder != null) {
            PlaybackTrack track = Tracks.get(position);
            holder.txtSongName.setText(track.SongName);

            if (track.AlbumName != null) {
                holder.txtAlbumName.setText(track.AlbumName);
            }

            if (track.AlbumImageSmall != "") {
                // Get the smallest one.
                Picasso.with(ctx).load(track.AlbumImageSmall).into(holder.imgAlbum);
            }
        }

        return view;
    }

    static class TrackHolder {
        public TextView txtSongName;
        public TextView txtAlbumName;
        public ImageView imgAlbum;
    }
}
