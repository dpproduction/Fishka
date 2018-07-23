package ru.funfishk.fishka;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.funfishk.fishka.clientserver.Repository;
import ru.funfishk.fishka.clientserver.ServerConnection;
import ru.funfishk.fishka.models.MainResponseModel;
import ru.funfishk.fishka.models.ResponseDataModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView = null;
    private FeedAdapter feedRecyclerAdapter = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private int page = 1;
    private boolean isScrollAdd = false;
    private boolean hasNextPage = false;
    private int lastVisibleItem = 0, totalItemCount = 0;
    private LinearLayoutManager linearLayoutManager = null;
    private Parcelable state = null;
    private boolean canLoad = false;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;

    // The number of native ads to load and display.
    public static final int NUMBER_OF_ADS = 5;

    // List of native ads that have been successfully loaded.
    public static List<NativeContentAd> mNativeAds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String appKey = "ca-app-pub-3940256099942544/8135179316";

        MobileAds.initialize(this, appKey);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/8135179316");


        mAdView = findViewById(R.id.adView);


        feedRecyclerAdapter = new FeedAdapter(this, this);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.feed_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(feedRecyclerAdapter);
        feedRecyclerAdapter.clearData();
        page = 1;
        loadData(page);

        initListeners();

        loadNativeAd(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (state != null) {
            linearLayoutManager.onRestoreInstanceState(state);
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onPause() {
        super.onPause();
        state = linearLayoutManager.onSaveInstanceState();
    }

    private void initListeners() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                feedRecyclerAdapter.clearData();
                page = 1;
                loadData(page);
            }
        });
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (lastVisibleItem == totalItemCount - 1 && hasNextPage) {
                    isScrollAdd = true;
                    if (canLoad)
                        loadData(++page);
                }
            }
        });
    }

    private void loadData(final int page) {
        canLoad = false;
        hasNextPage = false;
        if (!isScrollAdd)
            swipeRefreshLayout.setRefreshing(true);
        Repository feedRepo = new ServerConnection().createService(Repository.class);
        Call<MainResponseModel> call = feedRepo.getFeed(page, 15, Locale.getDefault().getCountry());
        call.enqueue(new Callback<MainResponseModel>() {
            @Override
            public void onResponse(Call<MainResponseModel> call, Response<MainResponseModel> response) {
                if (response.isSuccessful()) {
                    if (response.body().isStatus()) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.body().getData().size() > 0) {
                            for (int i = 0; i < response.body().getData().size(); i++) {
                                if ((i % 6) >= 5) {
                                    ResponseDataModel dataModel = new ResponseDataModel();
                                    dataModel.setType("ad");
                                    //   response.body().getData().add(dataModel);
                                    feedRecyclerAdapter.addData(dataModel);
                                }
                                feedRecyclerAdapter.addData(response.body().getData().get(i));
                            }

                            feedRecyclerAdapter.notifyDataSetChanged();
                            hasNextPage = true;
                            canLoad = true;
                        } else {
                            hasNextPage = false;
                        }
                    } else {
                        hasNextPage = false;
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    hasNextPage = false;
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(recyclerView, response.message(), Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MainResponseModel> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                hasNextPage = false;
                Snackbar.make(recyclerView, t.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void loadNativeAd(final int adLoadCount) {

        if (adLoadCount >= NUMBER_OF_ADS) {
            feedRecyclerAdapter.notifyDataSetChanged();
            return;
        }

        AdLoader.Builder builder = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/8135179316");
        AdLoader adLoader = builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
            @Override
            public void onAppInstallAdLoaded(NativeAppInstallAd ad) {
                // An app install ad loaded successfully, call this method again to
                // load the next ad in the items list.
            }
        }).forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
            @Override
            public void onContentAdLoaded(NativeContentAd ad) {
                // A content ad loaded successfully, call this method again to
                // load the next ad in the items list.
                mNativeAds.add(ad);
                loadNativeAd(adLoadCount + 1);
            }
        }).withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // A native ad failed to load. Call this method again to load
                // the next ad in the items list.
                Log.e("MainActivity", "The previous native ad failed to load. Attempting to" +
                        " load another.");
                loadNativeAd(adLoadCount + 1);
            }
        }).build();

        // Load the Native Express ad.
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
