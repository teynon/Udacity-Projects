package com.tomsfreelance.spotifystreamer.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tomsfreelance.spotifystreamer.Model.PlaybackArtist;
import com.tomsfreelance.spotifystreamer.R;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by gabriella on 5/30/2015.
 */
public class ArtistResultAdapter extends ArrayAdapter<PlaybackArtist> {
    private Context ctx = null;
    private ArrayList<PlaybackArtist> Artists = null;

    public ArtistResultAdapter(Context context, ArrayList<PlaybackArtist> artists) {
        super(context, R.layout.artist_result, artists);
        ctx = context;
        Artists = artists;
    }

    public ArrayList<PlaybackArtist> GetArtists() {
        return Artists;
    }

    // Used http://stackoverflow.com/questions/11927967/dynamically-populate-the-linear-layout-depending-upon-markers-on-the-map
    // as a reference.
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ArtistHolder holder = null;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.artist_result, null, false);
            holder = new ArtistHolder();
            holder.txtArtistName = (TextView)view.findViewById(R.id.txtArtistName);
            holder.imgArtist = (ImageView)view.findViewById(R.id.imgArtistIcon);
            view.setTag(holder);
        }
        else {
            holder = (ArtistHolder)view.getTag();
        }

        if (holder != null) {
            PlaybackArtist artist = Artists.get(position);
            holder.txtArtistName.setText(artist.ArtistName);
            if (artist.ArtistImage != null) {
                Picasso.with(ctx).load(artist.ArtistImage).into(holder.imgArtist);
            }
        }

        return view;
    }

    static class ArtistHolder {
        public TextView txtArtistName;
        public ImageView imgArtist;
    }
}
