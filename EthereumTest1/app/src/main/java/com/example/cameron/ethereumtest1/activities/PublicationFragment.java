package com.example.cameron.ethereumtest1.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.adapters.PublicationItemRecyclerViewAdapter;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.util.Convert;
import com.example.cameron.ethereumtest1.util.DataUtils;
import com.example.cameron.ethereumtest1.util.PrefUtils;

import java.math.BigDecimal;

public class PublicationFragment extends Fragment {

    public static final String TAG = PublicationFragment.class.getName();

    static private DBPublication mPublication;
    private RecyclerView mRecyclerView;
    private TextView mPublicationTitleTextView;
    private TextView mSupportersTextView;
    private TextView mNumArticlesTextView;
    private Button mManagePublicationButton;
    private Button mCheckAuthorOwedAmountButton;
    public boolean mReadyToClaimAuthorFunds = false;
    private View.OnClickListener mManageFundsOnClickListerner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(getContext(), mManagePublicationButton);
            popupMenu.getMenu().add(0,0,0, "permission author");
            popupMenu.getMenu().add(1,1,1, "withdraw " + DataUtils.formatAccountBalanceEther(String.valueOf(mPublication.adminClaimsOwed), 6));
            popupMenu.getMenu().add(2,2,2, "update publication pic");
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 0:
                                showAuthorPermissionDialog();
                                break;
                            case 1:
                                showWithdrawAdminClaimDialog();
                                break;
                            case 2:
                                showUpdatePublicationPhotoDialog();
                                break;
                        }
                    return true;
                }
            });
            popupMenu.show();
        }
    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case EthereumClientService.UI_UPDATE_AMOUNT_OWED_AUTHOR:
                    String amount = intent.getStringExtra(EthereumClientService.PARAM_AMOUNT_OWED_AUTHOR);
                    updateAuthorOwedAmount(amount);
                    break;
                case EthereumClientService.UI_WITHDRAW_AUTHOR_CLAIM_SENT:
                    updateAuthorWithdrawSent();
                    break;
                case EthereumClientService.UI_WITHDRAW_ADMIN_CLAIM_SENT:
                    updateAdminWithdrawSent();
                    break;
            }
        }
    };

    public static PublicationFragment newInstance(DBPublication publication) {
        mPublication = publication;
        PublicationFragment fragment = new PublicationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(EthereumClientService.UI_UPDATE_AMOUNT_OWED_AUTHOR);
        filter.addAction(EthereumClientService.UI_WITHDRAW_AUTHOR_CLAIM_SENT);
        filter.addAction(EthereumClientService.UI_WITHDRAW_ADMIN_CLAIM_SENT);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
        bm.registerReceiver(mBroadcastReceiver, filter);
        mReadyToClaimAuthorFunds = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_publication, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.publicationContentList);
        mPublicationTitleTextView = (TextView) v.findViewById(R.id.publicationTitle);
        mSupportersTextView = (TextView) v.findViewById(R.id.supporters);
        mNumArticlesTextView = (TextView) v.findViewById(R.id.numArticles);
        mManagePublicationButton = (Button) v.findViewById(R.id.managePublication);
        mCheckAuthorOwedAmountButton = (Button) v.findViewById(R.id.checkAuthorClaim);

        mManagePublicationButton.setOnClickListener(mManageFundsOnClickListerner);

        mPublicationTitleTextView.setText(mPublication.name);
        mSupportersTextView.setText(mPublication.uniqueSupporters + " supporters");
        mNumArticlesTextView.setText(mPublication.numPublished + " articles");

        String admin = mPublication.adminAddress;
        String selectedAddress = PrefUtils.getSelectedAccountAddress(getContext());
        if (admin.equals(selectedAddress)) {
            mManagePublicationButton.setVisibility(View.VISIBLE);
        }

        loadContentList(mPublication.publicationID);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
        bm.unregisterReceiver(mBroadcastReceiver);
    }



    private void loadContentList(int whichPub) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new PublicationItemRecyclerViewAdapter(getContext(), new DatabaseHelper(getContext()).getPublicationContentCursor(whichPub, 10), false));

       // if ((PrefUtils.should(getActivity()))) {
            try {
                getActivity().startService(new Intent(getContext(), EthereumClientService.class)
                        .putExtra(EthereumClientService.PARAM_WHICH_PUBLICATION, whichPub).setAction(EthereumClientService.ETH_FETCH_PUBLICATION_CONTENT));
            } catch (Exception e) {
                Log.e(TAG, "Error updating pub info: " + e.getMessage());
            }
       // }
    }


    public void managePublication() {

    }

    private void updateAuthorOwedAmount(String amount) {
        if (Convert.toWei(amount, Convert.Unit.WEI).compareTo(new BigDecimal(0)) != 0) {
            mCheckAuthorOwedAmountButton.setText("claim: " + DataUtils.formatAccountBalanceEther(amount, 6));
            mReadyToClaimAuthorFunds = true;
        } else {
            mCheckAuthorOwedAmountButton.setText("no claims owed");
        }
    }

    private void updateAuthorWithdrawSent() {
        mCheckAuthorOwedAmountButton.setText("author withdraw sent!");
        Toast.makeText(getContext(), "Transaction for withdrawal sent!", Toast.LENGTH_SHORT).show();

    }

    private void updateAdminWithdrawSent() {
        Toast.makeText(getContext(), "Admin withdraw claims sent!", Toast.LENGTH_SHORT).show();
    }

    public static DBPublication getPublication() {
        return mPublication;
    }

    private void showAuthorPermissionDialog() {
    }

    private void showWithdrawAdminClaimDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_withdraw_admin_claims);

        final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editPassword);
        final TextView whichPubID = (TextView)dialog.findViewById(R.id.whichPublicationID);
        whichPubID.setText("PublicationID: " + mPublication.publicationID);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonSubmit);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
                getActivity().startService(new Intent(getContext(), EthereumClientService.class)
                        .putExtra(EthereumClientService.PARAM_WITHDRAW_ADMIN_CLAIM_WHICH_PUB, mPublication.publicationID)
                        .putExtra(EthereumClientService.PARAM_ADDRESS_STRING, PrefUtils.getSelectedAccountAddress(getContext()))
                        .putExtra(EthereumClientService.PARAM_PASSWORD, password)
                        .setAction(EthereumClientService.ETH_WITHDRAW_ADMIN_CLAIM));
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private void showUpdatePublicationPhotoDialog() {
    }
}
