package com.cjdell.podclient.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.atermenji.android.iconicdroid.icon.IconicIcon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import com.cjdell.podclient.R;
import com.cjdell.podclient.helpers.TextHelper;
import com.cjdell.podclient.models.Cast;
import com.cjdell.podclient.models.Playlist;
import com.cjdell.podclient.models.playlist.PlaylistItemCollection;

/**
 * CastAdapter
 */
public class CastAdapter extends ArrayAdapter<Cast> {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE dd MMMM yyyy");

    private List<Cast>  mCasts;
    private Activity    mActivity;
    private Boolean     mAdd;
    private Boolean     mMenu;
    private Playlist    mPlaylist;

    private int         mSelectedIndex = -1;
    private int         mItemHeight = -1;

    private OnCastMenuButtonClick mOnCastMenuButtonClick;

    public CastAdapter(Activity activity, List<Cast> casts, Boolean add, Boolean menu) {
        super(activity, R.layout.cast_list_item, casts);

        mCasts      = casts;
        mActivity   = activity;
        mAdd        = add;
        mMenu       = menu;
    }

    public int getItemHeight() {
        return mItemHeight;
    }

    public void setPlaylist(Playlist playlist) {
        mPlaylist = playlist;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        Cast cast = mCasts.get(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cast_list_item, null, false);

            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.refresh(position, cast);

        return convertView;
    }

    public OnCastMenuButtonClick getOnMenuClickListener() {
        return mOnCastMenuButtonClick;
    }

    public void setOnMenuClickListener(OnCastMenuButtonClick onCastMenuButtonClick) {
        mOnCastMenuButtonClick = onCastMenuButtonClick;
    }

    public int getSelectedCast() {
        return mSelectedIndex;
    }

    public void setSelectedCast(int index) {
        mSelectedIndex = index;
        notifyDataSetChanged();
    }

    public interface OnCastMenuButtonClick {
        public void onCastMenuButtonClick(Cast cast, View v);
    }

    private class ViewHolder {
        int         position;
        boolean     fresh = true;

        View        root;

        TextView    title;
        TextView    pub_date;
        TextView    description;
        ImageView   list_image;

        View        downloaded_icon;
        View        played_icon;
        View        progress_bar;

        ImageView   menu_button;

        public ViewHolder(View view) {
            root = view;

            title            = (TextView)   root.findViewById(R.id.title);
            pub_date         = (TextView)   root.findViewById(R.id.pub_date);
            description      = (TextView)   root.findViewById(R.id.description);
            list_image       = (ImageView)  root.findViewById(R.id.list_image);

            downloaded_icon  = (View)       root.findViewById(R.id.downloaded_icon);
            played_icon      = (View)       root.findViewById(R.id.played_icon);
            progress_bar     = (View)       root.findViewById(R.id.progress_bar);

            menu_button     = (ImageView)   root.findViewById(R.id.menu_button);

            if (!mMenu) menu_button.setVisibility(View.GONE);
        }

        public void refresh(int pos, final Cast cast) {
            position = pos;

            String desc = cast.getDescription();

            if (desc != null && desc.length() > 150) desc = desc.substring(0, 150);

            title       .setText(cast.getTitle());
            pub_date    .setText(DATE_FORMAT.format(cast.getPubDate()));
            description .setText(desc);

            if (cast.getFeedId() != null) list_image.setImageURI(Uri.fromFile(cast.getImageFilePath()));

            downloaded_icon .setBackground(TextHelper.getIcon(mActivity.getApplicationContext(), IconicIcon.DOWNLOAD, !cast.isDownloaded() ? Color.argb(127, 127, 127, 127) : Color.BLACK));
            played_icon     .setBackground(TextHelper.getIcon(mActivity.getApplicationContext(), IconicIcon.PLAY_CIRCLE2, cast.getPlayed() == null ? Color.argb(127, 127, 127, 127) : Color.BLACK));

            int dpWidth = (200 * cast.getPlayedPercent()) / 100;

            ViewGroup.LayoutParams layoutParams = progress_bar.getLayoutParams();
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpWidth, mActivity.getResources().getDisplayMetrics());
            progress_bar.setLayoutParams(layoutParams);

            list_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cast.download();
                }
            });

            if (mAdd && mPlaylist != null) {
                final PlaylistItemCollection logic = mPlaylist.getItemCollection();

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (logic.hasItem(cast)) {
                            mPlaylist.getItemCollection().removeItem(cast);
                            unhighlight();
                        }
                        else {
                            mPlaylist.getItemCollection().addItem(cast);
                            highlight();
                        }
                    }
                });

                if (logic.hasItem(cast)) {
                    highlight();
                }
                else {
                    unhighlight();
                }
            }
            else {
                if (position == mSelectedIndex) {
                    highlight();
                }
                else {
                    unhighlight();
                }
            }

            if (mMenu) {
                menu_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnCastMenuButtonClick.onCastMenuButtonClick(cast, menu_button);
                    }
                });
            }

            fresh = false;

             if (mItemHeight == -1) mItemHeight = root.getMeasuredHeight();
        }

        public void highlight() {
            root.setBackground(mActivity.getResources().getDrawable(R.drawable.gradient_bg_selected));
        }

        public void unhighlight() {
            root.setBackground(mActivity.getResources().getDrawable(R.drawable.gradient_bg));
        }
    }

}