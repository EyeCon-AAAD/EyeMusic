package com.projectx.eyemusic.Music;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.projectx.eyemusic.Fragments.PlayerFragment;
import com.projectx.eyemusic.R;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {
    public ArrayList<MyTrack> tracks;
    public Context context;
    SpotifyAppRemote mSpotifyRemote;

    public TracksAdapter(ArrayList<MyTrack> tracks, Context context, SpotifyAppRemote mSpotifyRemote) {
        this.tracks = tracks;
        this.context = context;
        this.mSpotifyRemote = mSpotifyRemote;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link //#onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public TracksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_item_view, parent, false);
        return new ViewHolder(view, mSpotifyRemote, context);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link //#onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TracksAdapter.ViewHolder holder, int position) {
        MyTrack track = tracks.get(position);
        holder.tv_track_name.setText(track.getTrackName());
        holder.tv_track_artist.setText(track.getArtistName());
        Picasso.get().load(track.getImageURL()).into(holder.iv_track_image);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return this.tracks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_track_image;
        public TextView tv_track_name;
        public TextView tv_track_artist;


        public ViewHolder(@NonNull View itemView, SpotifyAppRemote spotifyAppRemote, Context context) {
            super(itemView);
            iv_track_image = itemView.findViewById(R.id.iv_track_image);
            tv_track_artist = itemView.findViewById(R.id.tv_track_artist_name);
            tv_track_name = itemView.findViewById(R.id.tv_track_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int  i = getAdapterPosition();
                    MyTrack track = tracks.get(i);
                    // play track
                    spotifyAppRemote.getPlayerApi().play(track.getSpotifyURI());
                    Toast.makeText(context, "Playing " + track.getTrackName(), Toast.LENGTH_SHORT).show();
                    Bundle playerBundle = new Bundle();
                    playerBundle.putInt("played", i);
                    playerBundle.putParcelableArrayList("tracks", tracks);
                    Fragment fragment = new PlayerFragment();
                    fragment.setArguments(playerBundle);
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment_container, fragment, "Player Fragment")
                            .addToBackStack(null) // on back pressed go back
                            .commit();

                }
            });
        }
    }
}
