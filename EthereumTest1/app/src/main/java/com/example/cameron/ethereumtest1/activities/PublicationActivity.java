package com.example.cameron.ethereumtest1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.adapters.PublicationItemRecyclerViewAdapter;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.util.PrefUtils;

public class PublicationActivity extends AppCompatActivity {

    public static final String TAG = PublicationActivity.class.getName();

    private DBPublication mPublication;
    private RecyclerView mRecyclerView;
    private TextView mPublicationTitleTextView;
    private TextView mSupportersTextView;
    private TextView mNumArticlesTextView;
    private Button mManagePublicationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_publication);

        Intent intent = getIntent();
        mPublication = intent.getParcelableExtra("publication");
        mRecyclerView = (RecyclerView) findViewById(R.id.publicationContentList);
        mPublicationTitleTextView = (TextView) findViewById(R.id.publicationTitle);
        mSupportersTextView = (TextView) findViewById(R.id.supporters);
        mNumArticlesTextView = (TextView) findViewById(R.id.numArticles);
        mManagePublicationButton = (Button) findViewById(R.id.managePublication);

        mPublicationTitleTextView.setText(mPublication.name);
        mSupportersTextView.setText(mPublication.uniqueSupporters + " supporters");
        mNumArticlesTextView.setText(mPublication.numPublished + " articles");

        String admin = mPublication.adminAddress;
        String selectedAddress = PrefUtils.getSelectedAccountAddress(this);
        if (admin.equals(selectedAddress)) {
            mManagePublicationButton.setVisibility(View.VISIBLE);
        }

        loadContentList(mPublication.publicationID);
    }



    private void loadContentList(int whichPub) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new PublicationItemRecyclerViewAdapter(this, new DatabaseHelper(this).getPublicationContentCursor(whichPub, 10), false));

       // if ((PrefUtils.should(getActivity()))) {
            try {
                startService(new Intent(this, EthereumClientService.class)
                        .putExtra(EthereumClientService.PARAM_WHICH_PUBLICATION, whichPub).setAction(EthereumClientService.ETH_FETCH_PUBLICATION_CONTENT));
            } catch (Exception e) {
                Log.e(TAG, "Error updating pub info: " + e.getMessage());
            }
       // }
    }


    public void managePublication(View view) {

    }

    public void checkAuthorClaim(View view) {

    }
}
