package com.example.cameron.ethereumtest1.activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.database.DBPublication;
import com.example.cameron.ethereumtest1.database.DBUserContentItem;
import com.example.cameron.ethereumtest1.fragments.EthTransactionListFragment;
import com.example.cameron.ethereumtest1.fragments.PublicationListFragment;
import com.example.cameron.ethereumtest1.fragments.SearchResultsPublicationsFragment;
import com.example.cameron.ethereumtest1.model.ContentItem;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.fragments.PublicationContentListFragment;
import com.example.cameron.ethereumtest1.fragments.UserFragment;
import com.example.cameron.ethereumtest1.ipfs_daemon.IPFSDaemon;
import com.example.cameron.ethereumtest1.ipfs_daemon.IPFSDaemonService;
import com.example.cameron.ethereumtest1.util.DataUtils;
import com.example.cameron.ethereumtest1.util.PrefUtils;
import org.ethereum.geth.Account;
import org.ethereum.geth.Geth;
import org.ethereum.geth.KeyStore;
import java.util.ArrayList;
import io.ipfs.kotlin.IPFS;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_SEND_ETH;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_AMOUNT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PUB_ADMIN_PAY;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PUB_META_DATA;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PUB_MIN_COST_WEI;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PUB_NAME;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_RECIPIENT;
import static com.example.cameron.ethereumtest1.ethereum.EthereumConstants.KEY_STORE;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_REGISTER_USER;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.ETH_UPDATE_USER_PIC;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_PASSWORD;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_USER_IMAGE_PATH;
import static com.example.cameron.ethereumtest1.ethereum.EthereumClientService.PARAM_USER_NAME;
import static com.example.cameron.ethereumtest1.util.PrefUtils.SELECTED_CONTENT_LIST;
import static com.example.cameron.ethereumtest1.util.PrefUtils.SELECTED_PUBLICATION_LIST;
import static com.example.cameron.ethereumtest1.util.PrefUtils.SELECTED_TRANSACTION_FRAGMENT;
import static com.example.cameron.ethereumtest1.util.PrefUtils.SELECTED_USER_FRAGMENT;

public class MainActivity extends AppCompatActivity implements
        PublicationContentListFragment.OnListFragmentInteractionListener,
        EthTransactionListFragment.OnFragmentInteractionListener {

    private final static String TAG = MainActivity.class.getName();

    private int PICK_IMAGE_REQUEST = 1;

    private TextView mSynchInfoTextView;
    private TextView mAccountTextView;
    private RelativeLayout mSwitchAccountButton;
    private ImageView mToolBarShadowView;
    private ImageView mFABShadowView;

    private PublicationContentListFragment mPublicationContentListFragment;
    private PublicationListFragment mPublicationListFragment;
    private UserFragment mUserFragment;
    private EthTransactionListFragment mEthTransactionListFragment;
    private PublicationFragment mPublicationFragment;
    private SearchResultsPublicationsFragment mSearchResultsPublicationsFragment;

    private ImageButton mContentListButton;
    private ImageButton mUserFragmentButton;
    private ImageButton mEthereumButton;
    private ImageButton mPublicationListButton;

    private FloatingActionButton mFloatingActionButton;
    private FloatingActionButton mFloatingActionButton1;
    private FloatingActionButton mFloatingActionButton2;
    private FloatingActionButton mFloatingActionButton3;

    PopupMenu.OnMenuItemClickListener mSwitchAccountPopupListener;

    private KeyStore mKeyStore;

    private IPFSDaemon mIpfsDaemon = new IPFSDaemon(this);

    private boolean mIsFabOpen = false;
    private int mNumAccounts = 0;
    private long mSelectedAccount;
    private final int BLOCK_NUMBER_FADE_TIME_MILLI = 50000;

    // handler for received data from service
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent.getAction().equals(EthereumClientService.UI_UPDATE_ETH_BLOCK)) {
                final long blockNumber = intent.getLongExtra(EthereumClientService.PARAM_BLOCK_NUMBER, 0);
                final long highestBlockNumber = intent.getLongExtra(EthereumClientService.PARAM_HIGHEST_BLOCK_NUMBER, 0);
                mSynchInfoTextView.setText(DataUtils.formatBlockNumber(blockNumber) + (blockNumber < highestBlockNumber ? "/" + DataUtils.formatBlockNumber(highestBlockNumber) : ""));
                ObjectAnimator colorAnim = ObjectAnimator.ofInt(mSynchInfoTextView, "textColor",
                        Color.CYAN, Color.RED);
                colorAnim.setEvaluator(new ArgbEvaluator());
                colorAnim.setDuration(BLOCK_NUMBER_FADE_TIME_MILLI);
                colorAnim.start();
            }
        }
    };
    /*
     * Lifecycle Methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSynchInfoTextView = (TextView) findViewById(R.id.synchInfo);
        mAccountTextView = (TextView) findViewById(R.id.accountInfo);
        mSwitchAccountButton = (RelativeLayout) findViewById(R.id.account_switch);
        mToolBarShadowView = (ImageView) findViewById(R.id.shadowDraw);
        mFABShadowView = (ImageView) findViewById(R.id.fabShadow);

        mContentListButton = (ImageButton) findViewById(R.id.button_content_list);
        mUserFragmentButton = (ImageButton) findViewById(R.id.user_fragment_button);
        mEthereumButton = (ImageButton) findViewById(R.id.button_ethereum);
        mPublicationListButton = (ImageButton) findViewById(R.id.button_publications);

        mContentListButton.setColorFilter(Color.WHITE);
        mUserFragmentButton.setColorFilter(Color.DKGRAY);
        mEthereumButton.setColorFilter(Color.DKGRAY);
        mPublicationListButton.setColorFilter(Color.DKGRAY);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingActionButton1 = (FloatingActionButton) findViewById(R.id.fab1);
        mFloatingActionButton2 = (FloatingActionButton) findViewById(R.id.fab2);
        mFloatingActionButton3 = (FloatingActionButton) findViewById(R.id.fab3);

        IntentFilter filter = new IntentFilter();
        filter.addAction(EthereumClientService.UI_UPDATE_ETH_BLOCK);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        mKeyStore = new KeyStore(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)  + KEY_STORE, Geth.LightScryptN, Geth.LightScryptP);
        mNumAccounts = (int)mKeyStore.getAccounts().size();
        if (mNumAccounts > 0) {
            mSelectedAccount = PrefUtils.getSelectedAccountNum(getBaseContext());
        }
        try {
            String accountString = mKeyStore.getAccounts().get(mSelectedAccount).getAddress().getHex();
            mAccountTextView.setText(accountString.substring(0, 4) + "..." + accountString.substring(accountString.length() - 4, accountString.length()));
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving account" + e.getMessage());
        }

        refreshAccounts();

        startIPFSDaemon();
        startService(new Intent(MainActivity.this, EthereumClientService.class).setAction(EthereumClientService.START_ETHEREUM_SERVICE));

        int selectedFragment = PrefUtils.getSelectedFragment(getBaseContext());
        switch (selectedFragment) {
            case SELECTED_CONTENT_LIST:
                showContentList(null);
                break;
            case SELECTED_PUBLICATION_LIST:
                showPublications(null);
                break;
            case SELECTED_USER_FRAGMENT:
                showUserFragment(null);
                break;
            case SELECTED_TRANSACTION_FRAGMENT:
                showEthereum(null);
            default:
                Log.e("ERROR", "ERROR");
                break;
        }

        drawTopBarShadow();
        drawFloatingActionButtonShadow();
    }

    private void drawTopBarShadow() {
        LinearGradient gradient = new LinearGradient(0, 0, 0, 300, 0x33FF0000, Color.BLACK, Shader.TileMode.CLAMP);
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
        mToolBarShadowView.setImageDrawable(drawable);
    }

    private void drawFloatingActionButtonShadow() {
        RadialGradient gradient = new RadialGradient(200,200,200,0xCC00FFFF, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);

        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawCircle(200, 200, 200, p);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        mFABShadowView.setImageDrawable(drawable);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("PERMISSION","Permission is granted");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
    }

    private void refreshAccounts() {
        mSwitchAccountPopupListener = new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals("NEW ACCOUNT")) {
                    createAccount();
                } else {
                    String selectedAddress = "error";
                    try {
                        selectedAddress = mKeyStore.getAccounts().get(item.getItemId()).getAddress().getHex();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int selectedAccountNum = item.getItemId();
                    PrefUtils.saveSelectedAccount(MainActivity.this, selectedAccountNum, selectedAddress);
                    mAccountTextView.setText(DataUtils.formatEthereumAccount(selectedAddress));
                    if (mUserFragment != null) mUserFragment.reloadUserInfo();
                    Toast.makeText(MainActivity.this, "switch to " + item.getTitle(), Toast.LENGTH_SHORT).show();

                }
                return true;
            }
        };
    }


    public void switchUser(View view) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, mSwitchAccountButton);
        if (mNumAccounts > 0) {
            try {
                for (int i = 0; i < mNumAccounts; i++) {
                    popupMenu.getMenu().add(i, i, i, DataUtils.formatEthereumAccount(mKeyStore.getAccounts().get(i).getAddress().getHex()));
                }
            } catch (Exception e) {

            }
        }
        popupMenu.getMenu().add(mNumAccounts, mNumAccounts, mNumAccounts, "NEW ACCOUNT");
        popupMenu.setOnMenuItemClickListener(mSwitchAccountPopupListener);
        popupMenu.show();
    }


    /*
     * Methods for managing IPFS Connectivity
     */
    private void startIPFSDaemon() {
        if (!mIpfsDaemon.isReady()) {
            mIpfsDaemon.download(this, new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    startIPFSServiceAndCheckForConnectivity.run();
                    return null;
                }
            });
        } else {
            startIPFSServiceAndCheckForConnectivity.run();
        }
    }

    private Runnable startIPFSServiceAndCheckForConnectivity = new Runnable() {
        @Override
        public void run() {
            startService(new Intent(MainActivity.this, IPFSDaemonService.class));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    IPFS ipfs = new IPFS();
                    String version = null;
                    while (version == null) {
                        try {
                            version = ipfs.getInfo().version().getVersion();
                        } catch (Exception e) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e1) {
                                Log.e("AHHHH", e1.getMessage());
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "connected to IPFS!", Toast.LENGTH_SHORT).show();
                            Log.e("IPFS", "CONNECTED!");
                        }
                    });
                }
            } ).start();
        }
    };

    /*
     * Methods for Managing Ethereum Accounts
     */
    public void createAccount() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_new_account);
        dialog.setTitle("Enter New Account Password");

        final EditText text = (EditText) dialog.findViewById(R.id.editMessage);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonDone);
        dialogButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    Account newAcc = mKeyStore.newAccount(text.getText().toString());
                    String account = newAcc.getAddress().getHex();
                    mNumAccounts = (int)mKeyStore.getAccounts().size();
                    PrefUtils.saveSelectedAccount(MainActivity.this, mNumAccounts - 1, account);
                    mAccountTextView.setText(account.substring(0,4) + "..." + account.substring(account.length() -4,account.length() - 1));
                    refreshAccounts();
                    if (mUserFragment != null) mUserFragment.reloadUserInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onListFragmentInteraction(ContentItem item) {
        Intent intent = new Intent(this, ViewContentActivity.class);
        ArrayList<ContentItem> contentItems = new ArrayList<>();
        contentItems.add(item);
        intent.putParcelableArrayListExtra("content_items", contentItems);
        startActivity(intent);
    }

    public void previewPost(View view) {
        Toast.makeText(getApplicationContext(), "Not yet implemented!", Toast.LENGTH_SHORT).show();
    }

    public void registerUser(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_register_user);
        dialog.setTitle("Register user");

        final EditText usernameEditText = (EditText) dialog.findViewById(R.id.editUsername);
        usernameEditText.setHint("desired username");
        final EditText pw = (EditText) dialog.findViewById(R.id.editPassword);
        pw.setHint("account password");
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonDone);
        dialogButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    startService(new Intent(MainActivity.this, EthereumClientService.class)
                            .putExtra(PARAM_USER_NAME, usernameEditText.getText().toString())
                            .putExtra(PARAM_PASSWORD, pw.getText().toString())
                            .setAction(ETH_REGISTER_USER));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void editContent(DBUserContentItem dbUserContentItem) {
        Intent intent = new Intent(this, EditContentActivity.class);
        intent.putExtra("dbItem", dbUserContentItem);
        startActivity(intent);
    }

    public void createNewContent(View view) {
        animateFabMenu(null);
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(this, EditContentActivity.class);
                startActivity(intent);
        //}
    }



    public void sendEth(View view) {
        animateFabMenu(null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_send_eth);

        final EditText recipientEditText = (EditText) dialog.findViewById(R.id.editRecipient);
        final EditText amountEditText = (EditText) dialog.findViewById(R.id.editAmount);
        final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editPassword);

        Button dialogSubmitButton = (Button) dialog.findViewById(R.id.dialogButtonSubmit);
        dialogSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, EthereumClientService.class)
                        .putExtra(PARAM_RECIPIENT, recipientEditText.getText().toString())
                        .putExtra(PARAM_AMOUNT, amountEditText.getText().toString())
                        .putExtra(PARAM_PASSWORD, passwordEditText.getText().toString())
                        .setAction(ETH_SEND_ETH));
                dialog.dismiss();
                animateFabMenu(null);
            }
        });
        dialog.show();
    }

    public void createPublication(View view) {
        animateFabMenu(null);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_publication);

        final EditText nameEditText = (EditText) dialog.findViewById(R.id.editName);
        final EditText metaEditText = (EditText) dialog.findViewById(R.id.editMetaData);
        final EditText minCostEditText = (EditText) dialog.findViewById(R.id.editMinSupportCost);
        final EditText adminPayEditText = (EditText) dialog.findViewById(R.id.editAdminPayPercentage);
        final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editPassword);

        Button dialogSubmitButton = (Button) dialog.findViewById(R.id.dialogButtonSubmit);
        dialogSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, EthereumClientService.class)
                        .putExtra(PARAM_PUB_NAME, nameEditText.getText().toString())
                        .putExtra(PARAM_PUB_META_DATA, metaEditText.getText().toString())
                        .putExtra(PARAM_PUB_MIN_COST_WEI, minCostEditText.getText().toString())
                        .putExtra(PARAM_PUB_ADMIN_PAY, adminPayEditText.getText().toString())
                        .putExtra(PARAM_PASSWORD, passwordEditText.getText().toString())
                        .setAction(EthereumClientService.ETH_CREATE_PUBLICATION));
                dialog.dismiss();
                animateFabMenu(null);
            }
        });
        dialog.show();
    }

    public void updateMetaData(View view) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_profile_pic);

        final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editPassword);

        Button dialogUploadButton = (Button) dialog.findViewById(R.id.dialogUploadButton);
        dialogUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // Show only images, no videos or anything else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonSubmit);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, EthereumClientService.class)
                        .putExtra(PARAM_USER_IMAGE_PATH, mUri)
                        .putExtra(PARAM_PASSWORD, passwordEditText.getText().toString())
                        .setAction(ETH_UPDATE_USER_PIC));
                dialog.dismiss();
                animateFabMenu(null);
            }
        });
        dialog.show();
    }

    /*
     * Code used for selecting a photo to upload
     */

    private String mUri;

    public void uploadPhoto(View v) {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mUri = getRealPathFromURI(data.getData());
            Toast.makeText(getApplicationContext(), "Upload this photo " + mUri, Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(contentURI);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    /*
     * UI Response Methods
     */

    public void showContentList(View view) {
        if (mPublicationContentListFragment == null)
            mPublicationContentListFragment = PublicationContentListFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mPublicationContentListFragment);
        transaction.commit();
        mContentListButton.setColorFilter(Color.CYAN);
        mPublicationListButton.setColorFilter(Color.WHITE);
        mUserFragmentButton.setColorFilter(Color.WHITE);
        mEthereumButton.setColorFilter(Color.WHITE);
        PrefUtils.saveSelectedFragment(getBaseContext(), SELECTED_CONTENT_LIST);
        showFAB(true);
    }

    public void showPublications(View view) {
        if (mPublicationListFragment == null)
            mPublicationListFragment = PublicationListFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mPublicationListFragment);
        transaction.commit();
        mContentListButton.setColorFilter(Color.WHITE);
        mPublicationListButton.setColorFilter(Color.CYAN);
        mUserFragmentButton.setColorFilter(Color.WHITE);
        mEthereumButton.setColorFilter(Color.WHITE);
        PrefUtils.saveSelectedFragment(getBaseContext(), SELECTED_PUBLICATION_LIST);
        showFAB(true);
    }


    public void showUserFragment(View view) {
        if (mUserFragment == null)
            mUserFragment = UserFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mUserFragment);
        transaction.commit();
        mContentListButton.setColorFilter(Color.WHITE);
        mPublicationListButton.setColorFilter(Color.WHITE);
        mUserFragmentButton.setColorFilter(Color.CYAN);
        mEthereumButton.setColorFilter(Color.WHITE);

        PrefUtils.saveSelectedFragment(getBaseContext(), SELECTED_USER_FRAGMENT);
        showFAB(true);
    }

    public void showEthereum(View view) {
        if (mEthTransactionListFragment == null)
            mEthTransactionListFragment = EthTransactionListFragment.newInstance("", "");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mEthTransactionListFragment);
        transaction.commit();
        mContentListButton.setColorFilter(Color.WHITE);
        mPublicationListButton.setColorFilter(Color.WHITE);
        mUserFragmentButton.setColorFilter(Color.WHITE);
        mEthereumButton.setColorFilter(Color.CYAN);

        PrefUtils.saveSelectedFragment(getBaseContext(), SELECTED_TRANSACTION_FRAGMENT);
        showFAB(true);
    }

    public void showPublicationFragment(DBPublication publication) {
        if (mPublicationFragment == null) {
            mPublicationFragment = PublicationFragment.newInstance(publication);
        } else if (mPublicationFragment.getPublication().publicationID != publication.publicationID) {
            mPublicationFragment = PublicationFragment.newInstance(publication);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mPublicationFragment);
        transaction.commit();
        mContentListButton.setColorFilter(Color.WHITE);
        mPublicationListButton.setColorFilter(Color.CYAN);
        mUserFragmentButton.setColorFilter(Color.WHITE);
        mEthereumButton.setColorFilter(Color.WHITE);

        PrefUtils.saveSelectedFragment(getBaseContext(), SELECTED_PUBLICATION_LIST);
        showFAB(false);
    }

    public void showSearchResultsFragment(String searchTerm, int sortCategory, boolean isDescending) {
        mSearchResultsPublicationsFragment = SearchResultsPublicationsFragment.newInstance(searchTerm, sortCategory, isDescending);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mSearchResultsPublicationsFragment);
        transaction.commit();
        mContentListButton.setColorFilter(Color.WHITE);
        mPublicationListButton.setColorFilter(Color.CYAN);
        mUserFragmentButton.setColorFilter(Color.WHITE);
        mEthereumButton.setColorFilter(Color.WHITE);

        PrefUtils.saveSelectedFragment(getBaseContext(), SELECTED_PUBLICATION_LIST);
        showFAB(false);
    }

    public void scrollToTop(View view) {
        mPublicationContentListFragment.scrollToTop();
        showFAB(true);
    }

    public void showFAB(boolean shouldShow) {
        if (shouldShow) {
           mFloatingActionButton.show();
           mFloatingActionButton1.show();
           mFloatingActionButton2.show();
           mFloatingActionButton3.show();
           mFABShadowView.setVisibility(View.VISIBLE);
        } else {
            mFloatingActionButton3.hide();
            mFloatingActionButton2.hide();
            mFloatingActionButton1.hide();
            mFloatingActionButton.hide();
            mFABShadowView.setVisibility(View.GONE);

        }
    }

    public void animateFabMenu(View v) {
        if (mIsFabOpen) {
            mIsFabOpen=false;
            mFloatingActionButton1.animate().translationY(0);
            mFloatingActionButton2.animate().translationY(0);
            mFloatingActionButton3.animate().translationY(0);
            mFloatingActionButton.animate().rotationBy(180);
        } else {
            mIsFabOpen = true;
            mFloatingActionButton1.animate().translationY(-getResources().getDimension(R.dimen.standard_60));
            mFloatingActionButton2.animate().translationY(-getResources().getDimension(R.dimen.standard_120));
            mFloatingActionButton3.animate().translationY(-getResources().getDimension(R.dimen.standard_180));
            mFloatingActionButton.animate().rotationBy(180);
        }
    }

    public void checkAuthorClaim(View view) {
        DBPublication pub = mPublicationFragment.getPublication();
        if (!mPublicationFragment.mReadyToClaimAuthorFunds) {
            startService(new Intent(this, EthereumClientService.class)
                    .putExtra(EthereumClientService.PARAM_WHICH_PUBLICATION, pub.publicationID)
                    .putExtra(EthereumClientService.PARAM_ADDRESS_STRING, PrefUtils.getSelectedAccountAddress(this))
                    .setAction(EthereumClientService.ETH_FETCH_AUTHOR_CLAIM_AMOUNT));
        } else {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_withdraw_author_claims);

            final EditText passwordEditText = (EditText) dialog.findViewById(R.id.editPassword);
            final TextView whichPubID = (TextView)dialog.findViewById(R.id.whichPublicationID);
            whichPubID.setText("PublicationID: " + pub.publicationID);

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonSubmit);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = passwordEditText.getText().toString();
                    startService(new Intent(getApplicationContext(), EthereumClientService.class)
                            .putExtra(EthereumClientService.PARAM_WHICH_PUBLICATION, mPublicationFragment.getPublication().publicationID)
                            .putExtra(EthereumClientService.PARAM_ADDRESS_STRING, PrefUtils.getSelectedAccountAddress(getApplicationContext()))
                            .putExtra(EthereumClientService.PARAM_PASSWORD, password)
                            .setAction(EthereumClientService.ETH_WITHDRAW_AUTHOR_CLAIM));
                    dialog.dismiss();
                    animateFabMenu(null);
                }
            });
            dialog.show();
        }
    }

    public void managePublication(View view) {
        if (mPublicationFragment != null) {
            mPublicationFragment.managePublication();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void copyAddress(View view) {
        if (mUserFragment != null) {
            mUserFragment.copyAddress();
            Toast.makeText(this, "copied address", Toast.LENGTH_SHORT).show();
        }
    }

    public void mostRevenue(View view) {

    }

    public void mostSupporters(View view) {
        showSearchResultsFragment(null, SearchResultsPublicationsFragment.SORT_CATEGORY_UNIQUE_SUPPORTERS, true);
    }

    public void newest(View view) {
    }

    public void restartNetwork(View view) {
        startService(new Intent(MainActivity.this, EthereumClientService.class)
                    .setAction(EthereumClientService.RESTART_ETHEREUM_CLIENT));
            Toast.makeText(this, "stopping Ethereum client...", Toast.LENGTH_SHORT).show();
//        }
    }
}

