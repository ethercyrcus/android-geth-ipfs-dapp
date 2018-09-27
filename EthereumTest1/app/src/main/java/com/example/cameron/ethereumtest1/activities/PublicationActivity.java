package com.example.cameron.ethereumtest1.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.adapters.PublicationItemRecyclerViewAdapter;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;

public class PublicationActivity extends AppCompatActivity {

    public static final String TAG = PublicationActivity.class.getName();

    private DBPublication mPublication;
    private RecyclerView mRecyclerView;
    private TextView mPublicationTitleTextView;
    private TextView mInfoTextView;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_publication);

        Intent intent = getIntent();
        DBPublication pub = intent.getParcelableExtra("publication");
        mRecyclerView = (RecyclerView) findViewById(R.id.publicationContentList);
        mPublicationTitleTextView = (TextView) findViewById(R.id.publicationTitle);
        //mToolBar = (Toolbar) findViewById(R.id.toolBar);

        mPublicationTitleTextView.setText(pub.name);
        //mToolBar.setTitle(pub.name);
        //mToolBar.setTitleTextColor(Color.WHITE);
        loadContentList(pub.publicationID);
    }



    private void loadContentList(int whichPub) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new PublicationItemRecyclerViewAdapter(this, new DatabaseHelper(this).getPublicationContentCursor(whichPub, 10), false));

       // if ((PrefUtils.should(getActivity()))) {
            try {
                startService(new Intent(this, EthereumClientService.class)
                        .putExtra(EthereumClientService.PARAM_WHICH_PUBLICATION, whichPub).setAction(EthereumClientService.ETH_FETCH_PUBLICATION_CONTENT));
            } catch (Exception e) {
                Log.e(TAG, "Error updating account balance: " + e.getMessage());
            }
       // }
    }


}
