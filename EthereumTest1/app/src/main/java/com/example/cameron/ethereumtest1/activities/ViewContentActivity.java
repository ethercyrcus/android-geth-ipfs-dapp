package com.example.cameron.ethereumtest1.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.database.DBPublicationContentItem;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.ethereum.EthereumConstants;
import com.example.cameron.ethereumtest1.model.ContentItem;
import com.example.cameron.ethereumtest1.util.DataUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_SUPPORT_POST;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_AMOUNT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_COMMENT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_CONTENT_ITEM;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PASSWORD;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PUBLICATION_CONTENT_ITEM_NUMBER;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_WHICH_PUBLICATION;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ViewContentActivity extends AppCompatActivity {

    private DBPublicationContentItem mContentItem;
    private TextView mTitleTextView;
    private TextView mDateAndAuthorTextView;
    private WebView mBodyWebView;
    private TextView mSupportersTextView;
    private TextView mRevenueTextView;
    //private TextView mNumCommentsTextView; TODO: Add click for comments if there are some


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_content);

        Intent intent = getIntent();
        ArrayList<Parcelable> items = intent.getParcelableArrayListExtra("content_items");
        mContentItem = (DBPublicationContentItem) items.get(0);

        mTitleTextView = (TextView) findViewById(R.id.contentTitle);
        mDateAndAuthorTextView = (TextView) findViewById(R.id.dateAndAuthor);
        mBodyWebView = (WebView) findViewById(R.id.contentBody);
        mSupportersTextView = (TextView) findViewById(R.id.supporters);
        mRevenueTextView = (TextView) findViewById(R.id.revenue);

        mTitleTextView.setText(mContentItem.title);
        String dateAndPublishedBy = "Published " + DataUtils.convertTimeStampToDateString(mContentItem.publishedDate)
                + " by " + mContentItem.publishedByEthAddress;
        mDateAndAuthorTextView.setText(dateAndPublishedBy);
        mBodyWebView.loadData(mContentItem.primaryText, "text/html; charset=UTF-8", null);
        mSupportersTextView.setText(mContentItem.uniqueSupporters + " supporters");
        mRevenueTextView.setText(DataUtils.formatAccountBalanceEther(mContentItem.revenueWei, 6));
        ImageView imageView = (ImageView) findViewById(R.id.image_content_activity);
        Glide.with(getBaseContext())
                .load(EthereumConstants.IPFS_GATEWAY_URL + mContentItem.imageIPFS)
                .into(imageView);
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
}
