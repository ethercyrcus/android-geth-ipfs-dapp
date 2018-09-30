package com.example.cameron.ethereumtest1.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.adapters.PublicationItemRecyclerViewAdapter;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.util.DataUtils;
import com.example.cameron.ethereumtest1.util.PrefUtils;

public class PublicationActivity extends AppCompatActivity {

    public static final String TAG = PublicationActivity.class.getName();

    private DBPublication mPublication;
    private RecyclerView mRecyclerView;
    private TextView mPublicationTitleTextView;
    private TextView mSupportersTextView;
    private TextView mNumArticlesTextView;
    private Button mManagePublicationButton;
    private Button mCheckAuthorOwedAmountButton;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case EthereumClientService.UI_UPDATE_AMOUNT_OWED_AUTHOR:
                    String amount = intent.getStringExtra(EthereumClientService.PARAM_AMOUNT_OWED_AUTHOR);
                    updateAuthorOwedAmount(amount);
                    break;
            }
        }
    };



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
        mCheckAuthorOwedAmountButton = (Button) findViewById(R.id.checkAuthorClaim);

        mPublicationTitleTextView.setText(mPublication.name);
        mSupportersTextView.setText(mPublication.uniqueSupporters + " supporters");
        mNumArticlesTextView.setText(mPublication.numPublished + " articles");

        IntentFilter filter = new IntentFilter();
        filter.addAction(EthereumClientService.UI_UPDATE_AMOUNT_OWED_AUTHOR);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        String admin = mPublication.adminAddress;
        String selectedAddress = PrefUtils.getSelectedAccountAddress(this);
        if (admin.equals(selectedAddress)) {
            mManagePublicationButton.setVisibility(View.VISIBLE);
        }

        loadContentList(mPublication.publicationID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
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
        startService(new Intent(this, EthereumClientService.class)
                .putExtra(EthereumClientService.PARAM_WHICH_PUBLICATION, mPublication.publicationID)
                .putExtra(EthereumClientService.PARAM_ADDRESS_STRING, PrefUtils.getSelectedAccountAddress(this))
                .setAction(EthereumClientService.ETH_FETCH_AUTHOR_CLAIM_AMOUNT));
    }

    private void updateAuthorOwedAmount(String amount) {
        mCheckAuthorOwedAmountButton.setText(DataUtils.formatAccountBalanceEther(amount, 6));
    }
}
