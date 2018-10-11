package com.example.cameron.ethereumtest1.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.activities.MainActivity;
import com.example.cameron.ethereumtest1.activities.PublicationFragment;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;

public class PublicationsRecyclerViewAdapter extends RecyclerView.Adapter<PublicationsRecyclerViewAdapter.ViewHolder>{

    private CursorAdapter mCursorAdapter;
    private final AppCompatActivity mActivity;

    public PublicationsRecyclerViewAdapter(AppCompatActivity activity, Cursor cursor) {
        mActivity = activity;
        mCursorAdapter = new CursorAdapter(activity, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_publication, parent, false);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                final DBPublication pub = DatabaseHelper.convertCursorToDBPublication(cursor);
                ViewHolder holder = new ViewHolder(view);

                holder.mNameView.setText(pub.name);
                holder.mPostCountView.setText("articles: " + pub.numPublished);
                holder.mSupportersView.setText("supporters: " + pub.uniqueSupporters);
                holder.mShadowImageView.setImageDrawable(holder.mShadowDrawable);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity)mActivity).showPublicationFragment(pub);
                    }
                });
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mCursorAdapter.newView(mActivity, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.mView, mActivity, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mPostCountView;
        public final TextView mSupportersView;
        public final BitmapDrawable mShadowDrawable;
        public final ImageView mShadowImageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mPostCountView = (TextView) view.findViewById(R.id.posts);
            mSupportersView = (TextView) view.findViewById(R.id.supporters);
            mShadowDrawable = drawTopBarShadow();
            mShadowImageView = (ImageView) view.findViewById(R.id.shadowDraw);
        }
    }

    private BitmapDrawable drawTopBarShadow() {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 300, 0x1A00FFFF, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        //p.setDither(true);
        p.setShader(gradient);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Bitmap bitmap = Bitmap.createBitmap(width, 400, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawRect(new Rect(0,0, width,300), p);

        return new BitmapDrawable(mActivity.getResources(), bitmap);
    }
}
