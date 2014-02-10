package com.cjdell.podclient.adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjdell.podclient.R;
import com.cjdell.podclient.models.Playlist;

import java.io.File;
import java.util.List;

/**
 * CastAdapter
 */
public class PlaylistAdapter extends ArrayAdapter<Playlist> {
    private List<Playlist>  mPlaylists;
    private Activity        mActivity;

    private OnPlaylistMenuButtonClick mOnPlaylistMenuButtonClick;

    public PlaylistAdapter(Activity activity, List<Playlist> playlists) {
        super(activity, R.layout.cast_list_item, playlists);

        mPlaylists  = playlists;
        mActivity   = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        Playlist playlist = mPlaylists.get(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.playlist_list_item, null, false);

            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.refresh(position, playlist);

        return convertView;
    }

    public OnPlaylistMenuButtonClick getOnMenuClickListener() {
        return mOnPlaylistMenuButtonClick;
    }

    public void setOnMenuClickListener(OnPlaylistMenuButtonClick onPlaylistMenuButtonClick) {
        mOnPlaylistMenuButtonClick = onPlaylistMenuButtonClick;
    }

    public interface OnPlaylistMenuButtonClick {
        public void onPlaylistMenuButtonClick(Playlist playlist, View v);
    }

    private class ViewHolder {
        int         position;

        View        root;

        TextView    title;
        ImageView   thumbnail;

        LinearLayout    multi_thumbs;
        ImageView       thumbnail_small_1;
        ImageView       thumbnail_small_2;
        ImageView       thumbnail_small_3;
        ImageView       thumbnail_small_4;

        ImageView   menu_button;

        public ViewHolder(View view) {
            root = view;

            title           = (TextView)    root.findViewById(R.id.title);
            thumbnail       = (ImageView)   root.findViewById(R.id.thumbnail);

            multi_thumbs        = (LinearLayout)    root.findViewById(R.id.multi_thumbs);
            thumbnail_small_1   = (ImageView)       root.findViewById(R.id.thumbnail_small_1);
            thumbnail_small_2   = (ImageView)       root.findViewById(R.id.thumbnail_small_2);
            thumbnail_small_3   = (ImageView)       root.findViewById(R.id.thumbnail_small_3);
            thumbnail_small_4   = (ImageView)       root.findViewById(R.id.thumbnail_small_4);

            menu_button     = (ImageView)   root.findViewById(R.id.menu_button);
        }

        public void refresh(int pos, final Playlist playlist) {
            position = pos;

            title.setText(playlist.getTitle());

            List<File> thumbnails = playlist.getThumbnails();

            if (thumbnails.size() == 1) {
                thumbnail.setImageURI(Uri.fromFile(thumbnails.get(0)));

                thumbnail.setVisibility(View.VISIBLE);
                multi_thumbs.setVisibility(View.INVISIBLE);
            }
            else if (thumbnails.size() > 1) {
                thumbnail_small_1.setImageURI(null);
                thumbnail_small_2.setImageURI(null);
                thumbnail_small_3.setImageURI(null);
                thumbnail_small_4.setImageURI(null);

                if (thumbnails.size() > 0) thumbnail_small_1.setImageURI(Uri.fromFile(thumbnails.get(0)));
                if (thumbnails.size() > 1) thumbnail_small_2.setImageURI(Uri.fromFile(thumbnails.get(1)));
                if (thumbnails.size() > 2) thumbnail_small_3.setImageURI(Uri.fromFile(thumbnails.get(2)));
                if (thumbnails.size() > 3) thumbnail_small_4.setImageURI(Uri.fromFile(thumbnails.get(3)));

                thumbnail.setVisibility(View.INVISIBLE);
                multi_thumbs.setVisibility(View.VISIBLE);
            }
            else {
                thumbnail.setVisibility(View.INVISIBLE);
                multi_thumbs.setVisibility(View.INVISIBLE);
            }

            menu_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnPlaylistMenuButtonClick.onPlaylistMenuButtonClick(playlist, menu_button);
                }
            });
        }
    }
}