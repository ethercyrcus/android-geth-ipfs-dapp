package com.example.cameron.ethereumtest1.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.adapters.PublicationsRecyclerViewAdapter;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.util.PrefUtils;

public class SearchResultsPublicationsFragment extends Fragment {

    private final static String TAG = SearchResultsPublicationsFragment.class.getName();

    public static final int SORT_CATEGORY_REVENUE = 0;
    public static final int SORT_CATEGORY_UNIQUE_SUPPORTERS = 1;
    public static final int SORT_CATEGORY_DATE = 2;

    private RecyclerView mSearchResultsRecyclerView;
    private static String mSearchTerm;
    private static int mSortCategory;
    private static boolean mIsDescending;

//    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            switch (action) {
//                case EthereumClientService.UI_UPDATE_PUBLICATION_LIST:
//                    reloadPublicationsDB();
//                    break;
//            }
//        }
//    };

    public static SearchResultsPublicationsFragment newInstance(String searchTerm, int sortCategory, boolean descending) {
        mSearchTerm = searchTerm;
        mSortCategory = sortCategory;
        mIsDescending = descending;
        SearchResultsPublicationsFragment fragment = new SearchResultsPublicationsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(EthereumClientService.UI_UPDATE_PUBLICATION_LIST);
//        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
//        bm.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_results_publications, container, false);

        mSearchResultsRecyclerView = (RecyclerView) v.findViewById(R.id.searchResultsPublicationsList);

        reloadPublicationsDB();
        //loadPublicationsFromEthereumChain();

        return v;
    }

    private void reloadPublicationsDB() {
        mSearchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchResultsRecyclerView.setAdapter(new PublicationsRecyclerViewAdapter((AppCompatActivity) getActivity(), new DatabaseHelper(getContext()).getPublicationsCursor(mSearchTerm, mSortCategory, mIsDescending)));
    }

//    private void loadPublicationsFromEthereumChain() {
//        if ((PrefUtils.shouldUpdateAccountContentList(getActivity()))) {
//            try {
//                getActivity().startService(new Intent(getContext(), EthereumClientService.class)
//                        .setAction(EthereumClientService.ETH_FETCH_PUBLICATION_LIST));
//            } catch (Exception e) {
//                Log.e(TAG, "Error requesting publications list: " + e.getMessage());
//            }
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
//        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
//        bm.unregisterReceiver(mBroadcastReceiver);
    }

}
