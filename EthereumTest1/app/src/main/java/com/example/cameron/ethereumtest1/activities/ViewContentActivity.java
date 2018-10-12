package com.example.cameron.ethereumtest1.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.database.DBPublicationContentItem;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.ethereum.EthereumConstants;
import com.example.cameron.ethereumtest1.model.ContentItem;
import com.example.cameron.ethereumtest1.util.DataUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_FETCH_COMMENTS;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_SEND_ETH;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_SUPPORT_POST;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_AMOUNT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_COMMENT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_CONTENT_ITEM;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_NUM_COMMENTS;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PASSWORD;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PUBLICATION_CONTENT_ITEM_NUMBER;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_RECIPIENT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_WHICH_PUBLICATION;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ViewContentActivity extends AppCompatActivity {

    private DBPublicationContentItem mContentItem;
    private TextView mTitleTextView;
    private TextView mDateTextView;
    private TextView mAuthorTextView;
    private ImageView mShadowView;
    private WebView mBodyWebView;
    private TextView mSupportersTextView;
    private TextView mRevenueTextView;
    private TextView mNumCommentsTextButton;
    private ListView mCommentsListView;
    private boolean mShowingComments = false;
    private ArrayList<String> mComments = new ArrayList<>();
    //private TextView mNumCommentsTextView; TODO: Add click for comments if there are some

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(EthereumClientService.UI_UPDATE_PUBLICATION_CONTENT_COMMENTS_LIST)) {
                mComments = intent.getStringArrayListExtra(EthereumClientService.PARAM_ARRAY_PUBLICATION_CONTENT_COMMENTS_LIST);
                updateCommentsList();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_content);

        IntentFilter filter = new IntentFilter();
        filter.addAction(EthereumClientService.UI_UPDATE_PUBLICATION_CONTENT_COMMENTS_LIST);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        Intent intent = getIntent();
        ArrayList<Parcelable> items = intent.getParcelableArrayListExtra("content_items");
        mContentItem = (DBPublicationContentItem) items.get(0);

        mTitleTextView = (TextView) findViewById(R.id.contentTitle);
        mDateTextView = (TextView) findViewById(R.id.date);
        mAuthorTextView = (TextView) findViewById(R.id.author);
        mShadowView = (ImageView) findViewById(R.id.shadowDraw);
        mBodyWebView = (WebView) findViewById(R.id.contentBody);
        mSupportersTextView = (TextView) findViewById(R.id.supporters);
        mRevenueTextView = (TextView) findViewById(R.id.revenue);
        mNumCommentsTextButton = (TextView) findViewById(R.id.commentsButton);
        mCommentsListView = (ListView) findViewById(R.id.commentsListView);

        mTitleTextView.setText(mContentItem.title);
        String dateAndPublishedBy = DataUtils.convertTimeStampToDateString(mContentItem.publishedDate);
        mDateTextView.setText(dateAndPublishedBy);
        mAuthorTextView.setText(mContentItem.publishedByEthAddress);
        mBodyWebView.loadData(mContentItem.primaryText, "text/html; charset=UTF-8", null);
        mSupportersTextView.setText(mContentItem.uniqueSupporters + " supporter(s)");
        mRevenueTextView.setText(DataUtils.formatAccountBalanceEther(mContentItem.revenueWei, 6));
        mNumCommentsTextButton.setText("view " + mContentItem.numComments + " comment(s)");
        ImageView imageView = (ImageView) findViewById(R.id.image_content_activity);
        Glide.with(getBaseContext())
                .load(EthereumConstants.IPFS_GATEWAY_URL + mContentItem.imageIPFS)
                .into(imageView);
        drawShadow();
    }

    private void drawShadow() {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 300, 0x4D000000, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        //p.setDither(true);
        p.setShader(gradient);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Bitmap bitmap = Bitmap.createBitmap(width, 400, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawRect(new Rect(0,0, width,300), p);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        mShadowView.setImageDrawable(drawable);
    }


    public void close(View view) {
        onBackPressed();
    }

    public void supportPost(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_support_post);

        final EditText amountEditText = (EditText) dialog.findViewById(R.id.editAmount);
        final EditText commentEditText = (EditText) dialog.findViewById(R.id.editComment);
        final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editPassword);

        Button dialogSubmitButton = (Button) dialog.findViewById(R.id.dialogButtonSubmit);
        dialogSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(ViewContentActivity.this, EthereumClientService.class)
                        .putExtra(PARAM_AMOUNT, amountEditText.getText().toString())
                        .putExtra(PARAM_COMMENT, commentEditText.getText().toString())
                        .putExtra(PARAM_PASSWORD, passwordEditText.getText().toString())
                        .putExtra(PARAM_WHICH_PUBLICATION, mContentItem.publicationIndex)
                        .putExtra(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER, mContentItem.publicationContentIndex)
                        .setAction(ETH_SUPPORT_POST));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void viewComments(View view) {
        mNumCommentsTextButton.setText(mShowingComments ? "view " + mContentItem.numComments + " comment(s)" : "hide comment(s)");
        startService(new Intent(ViewContentActivity.this, EthereumClientService.class)
                .putExtra(PARAM_WHICH_PUBLICATION, mContentItem.publicationIndex)
                .putExtra(PARAM_PUBLICATION_CONTENT_ITEM_NUMBER, mContentItem.publicationContentIndex)
                .putExtra(PARAM_NUM_COMMENTS, mContentItem.numComments)
                .setAction(ETH_FETCH_COMMENTS));
    }



    private void updateCommentsList() {
        mCommentsListView.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mComments);
        mCommentsListView.setAdapter(adapter);
    }
}
