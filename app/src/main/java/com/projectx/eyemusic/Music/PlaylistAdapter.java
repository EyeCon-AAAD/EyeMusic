package com.projectx.eyemusic.Music;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

import com.projectx.eyemusic.Fragments.PlaylistFragment;
import com.projectx.eyemusic.Fragments.TracksFragment;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.Model.GazePoint;
import com.projectx.eyemusic.R;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    public ArrayList<Playlist> playlists;
    public Context context;
    SpotifyAppRemote spotifyAppRemote;


    static private ArrayList<View> references_playlistItems = new ArrayList<View>();

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlists, SpotifyAppRemote spotifyAppRemote) {
        this.playlists = playlists;
        this.context = context;
        this.spotifyAppRemote =spotifyAppRemote;
    }

    public static ArrayList<View> getReferencesPlaylistItems() {
        return references_playlistItems;
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
     * {@link #//onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
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
    public PlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate R.layout.playlist_item_view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_view, parent, false);
        return new ViewHolder(v, context, spotifyAppRemote);
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
     * Override {@link #//onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.ViewHolder holder, int position) {
        // get individual playlist position
        Playlist playlist = playlists.get(position);
        // update text and image
        holder.tv_playlist_name.setText(playlist.getName());
        Picasso.get().load(playlist.getImageURL()).into(holder.iv_playlist_image);
}

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_playlist_image;
        public TextView tv_playlist_name;
        public Context context;
        public SpotifyAppRemote spotifyAppRemote;
        public View playlistItemView;

        public View getPlaylistItemView() {
            return playlistItemView;
        }

        public ViewHolder(@NonNull View itemView, Context context, SpotifyAppRemote spotifyAppRemote) {
            super(itemView);
            this.playlistItemView = itemView;
            iv_playlist_image = itemView.findViewById(R.id.iv_playlist_image);
            tv_playlist_name = itemView.findViewById(R.id.tv_playlist_name);
            this.context = context;
            this.spotifyAppRemote = spotifyAppRemote;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Playlist playlist = playlists.get(getAdapterPosition());
                    //spotifyAppRemote.getPlayerApi().play(playlist.getSpotifyURI());
                    // go to the Tracks Fragment
                    // Playlist id will be needed to fetch tracks of current album
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    Fragment fragment = TracksFragment.newInstance(playlist.getId());

                    Fragment frag = null;
                    MainActivity.currentFragment = fragment;
                    for (Fragment f: activity.getSupportFragmentManager().getFragments()){
                        if (f.getTag().equals("Playlist Fragment"))
                            frag = f;
                    }
                    activity.getSupportFragmentManager().beginTransaction()
                            .remove(frag)
                            .add(R.id.main_fragment_container, fragment, "Tracks Fragment")
                            .addToBackStack(null) // on back pressed go back
                            .commit();
                }
            });
        }
    }
}
