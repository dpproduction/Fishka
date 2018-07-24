package ru.funfishk.fishka;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.lujun.androidtagview.TagContainerLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import ru.funfishk.fishka.models.ResponseDataModel;

import static ru.funfishk.fishka.MainActivity.mNativeAds;

/**
 * Created by Ghostman on 10.03.2018.
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int IMAGE_VIEW_TYPE = 1;
    private static final int GIF_VIEW_TYPE = 2;
    private static final int VIDEO_VIEW_TYPE = 3;
    private static final int AD_VIEW_TYPE = 4;
    private Animation likeAnimation = null;
    private List<ResponseDataModel> feedList = null;
    private Context context = null;
    private boolean isPlaying = false, isVisibleControls = false;
    private Activity activity = null;
    private Random r;


    public FeedAdapter(Context context, Activity activity) {
        this.context = context;
        feedList = new ArrayList<>();
        this.activity = activity;
        likeAnimation = AnimationUtils.loadAnimation(context, R.anim.like_anim);
        r = new Random();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case IMAGE_VIEW_TYPE:
                View image = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_image_recycler_item, parent, false);
                return new ImageViewHolder(image);
            case GIF_VIEW_TYPE:
                View gif = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_gif_recycler_item, parent, false);
                return new GifViewHolder(gif);
            case VIDEO_VIEW_TYPE:
                View video = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_video_recycler_item, parent, false);
                return new VideoViewHolder(video);
            case AD_VIEW_TYPE:
                View ad = LayoutInflater.from(parent.getContext()).inflate(R.layout.admob_layout, parent, false);
                return new ADViewHolder(ad);
            default:
                View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recycler_item, parent, false);
                return new DefaultViewHolder(root);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case AD_VIEW_TYPE:
                final ADViewHolder adViewHolder = (ADViewHolder) holder;

                if(mNativeAds.size()>0) {
                    int pos = r.nextInt(mNativeAds.size());
                    populateContentAdView(mNativeAds.get(pos), (NativeContentAdView) holder.itemView);
                }
                break;
            case IMAGE_VIEW_TYPE:
                final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
                feedList.get(position).setLikesCount(new Random().nextInt(100));
                //Load data
                Glide.with(context).load(feedList.get(position).getFileUrl())
                        .apply(new RequestOptions().priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.DATA)).into(imageViewHolder.imageView);
                imageViewHolder.imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        gestureDetector.onTouchEvent(motionEvent);
                        return true;
                    }

                    private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            if (feedList.get(position).isLiked()) {
                                dislike(feedList.get(position).getLikesCount(), position, imageViewHolder.likeImageView, imageViewHolder.likeCountTextView);
                            } else {
                                like(feedList.get(position).getLikesCount(), position, imageViewHolder.likeImageView, imageViewHolder.likeCountTextView, imageViewHolder.bigLikeImage);
                            }
                            return super.onDoubleTap(e);
                        }
                    });
                });
                //Like/Unlike listeners

                if (feedList.get(position).isLiked()) {
                    imageViewHolder.likeImageView.setImageResource(R.drawable.like_red);
                } else {
                    imageViewHolder.likeImageView.setImageResource(R.drawable.like);
                }
                imageViewHolder.likeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (feedList.get(position).isLiked()) {
                            dislike(feedList.get(position).getLikesCount(), position, imageViewHolder.likeImageView, imageViewHolder.likeCountTextView);
                        } else {
                            like(feedList.get(position).getLikesCount(), position, imageViewHolder.likeImageView, imageViewHolder.likeCountTextView, imageViewHolder.bigLikeImage);
                        }

                    }
                });
                // Like count
                imageViewHolder.likeCountTextView.setVisibility(View.VISIBLE);

                imageViewHolder.likeCountTextView.setText(String.valueOf(feedList.get(position).getLikesCount()));

                //Title
                if (feedList.get(position).getTitle() == null) {
                    imageViewHolder.titleTextView.setVisibility(View.GONE);
                } else {
                    imageViewHolder.titleTextView.setVisibility(View.VISIBLE);
                    imageViewHolder.titleTextView.setText(feedList.get(position).getTitle());
                }
                //Publish date
                long timestamp = Long.parseLong(String.valueOf(feedList.get(position).getPublishedAtTimestamp())) * 1000L;
                imageViewHolder.pubDateTextView.setReferenceTime(timestamp);
                //Tag
                imageViewHolder.tagGroup.setTags(feedList.get(position).getTags());
                break;
            case GIF_VIEW_TYPE:
                final GifViewHolder gifViewHolder = (GifViewHolder) holder;
                feedList.get(position).setLikesCount(new Random().nextInt(100));
                // Load data
                Glide.with(context).asGif().load(feedList.get(position).getFileUrl()).thumbnail(Glide.with(context).asGif().load(R.drawable.loading))
                        .apply(new RequestOptions().priority(Priority.HIGH).diskCacheStrategy(DiskCacheStrategy.DATA)).into(gifViewHolder.imageView);
                gifViewHolder.imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        gestureDetector.onTouchEvent(motionEvent);
                        return true;
                    }

                    private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            if (feedList.get(position).isLiked()) {
                                dislike(feedList.get(position).getLikesCount(), position, gifViewHolder.likeImageView, gifViewHolder.likeCountTextView);
                            } else {
                                like(feedList.get(position).getLikesCount(), position, gifViewHolder.likeImageView, gifViewHolder.likeCountTextView, gifViewHolder.bigLikeImage);
                            }
                            return super.onDoubleTap(e);
                        }
                    });
                });
                // Like\Unlike listeners
                if (feedList.get(position).isLiked()) {
                    gifViewHolder.likeImageView.setImageResource(R.drawable.like_red);

                } else {
                    gifViewHolder.likeImageView.setImageResource(R.drawable.like);

                }
                gifViewHolder.likeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (feedList.get(position).isLiked()) {
                            dislike(feedList.get(position).getLikesCount(), position, gifViewHolder.likeImageView, gifViewHolder.likeCountTextView);
                        } else {
                            like(feedList.get(position).getLikesCount(), position, gifViewHolder.likeImageView, gifViewHolder.likeCountTextView, gifViewHolder.bigLikeImage);
                        }

                    }
                });
                // Like count
                gifViewHolder.likeCountTextView.setText(String.valueOf(feedList.get(position).getLikesCount()));
                // Title
                if (feedList.get(position).getTitle() == null) {
                    gifViewHolder.titleTextView.setVisibility(View.GONE);
                } else {
                    gifViewHolder.titleTextView.setVisibility(View.VISIBLE);
                    gifViewHolder.titleTextView.setText(feedList.get(position).getTitle());
                }
                // Publish date
                long timestamp2 = Long.parseLong(String.valueOf(feedList.get(position).getPublishedAtTimestamp())) * 1000L;
                gifViewHolder.pubDateTextView.setReferenceTime(timestamp2);
                // Tag
                gifViewHolder.tagGroup.setTags(feedList.get(position).getTags());
                break;
            case VIDEO_VIEW_TYPE:
                final VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
                feedList.get(position).setLikesCount(new Random().nextInt(100));
                //Load video
                videoViewHolder.videoView.setSource(Uri.parse(feedList.get(position).getFileUrl()));
                videoViewHolder.videoView.setLoop(true);
                if (videoViewHolder.videoView.isControlsShown())
                    videoViewHolder.videoView.disableControls();
                videoViewHolder.videoView.setCallback(new EasyVideoCallback() {
                    @Override
                    public void onStarted(EasyVideoPlayer player) {

                    }

                    @Override
                    public void onPaused(EasyVideoPlayer player) {
                        player.pause();
                    }

                    @Override
                    public void onPreparing(EasyVideoPlayer player) {

                    }

                    @Override
                    public void onPrepared(EasyVideoPlayer player) {
                    }

                    @Override
                    public void onBuffering(int percent) {

                    }

                    @Override
                    public void onError(EasyVideoPlayer player, Exception e) {

                    }

                    @Override
                    public void onCompletion(EasyVideoPlayer player) {

                    }

                    @Override
                    public void onRetry(EasyVideoPlayer player, Uri source) {

                    }

                    @Override
                    public void onSubmit(EasyVideoPlayer player, Uri source) {

                    }
                });
                isVisibleControls = true;
                videoViewHolder.playPauseImageView.setVisibility(View.VISIBLE);
                videoViewHolder.playPauseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isPlaying) {
                            videoViewHolder.playPauseImageView.setImageResource(R.drawable.ic_play_circle_outline_white_48dp);
                            videoViewHolder.videoView.pause();
                            isPlaying = false;
                        } else {
                            videoViewHolder.playPauseImageView.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp);
                            videoViewHolder.videoView.start();
                            isPlaying = true;
                        }
                    }
                });
                // Title
                if (feedList.get(position).getTitle() == null) {
                    videoViewHolder.titleTextView.setVisibility(View.GONE);
                } else {
                    videoViewHolder.titleTextView.setVisibility(View.VISIBLE);
                    videoViewHolder.titleTextView.setText(feedList.get(position).getTitle());
                }
                videoViewHolder.likeCountTextView.setText(String.valueOf(feedList.get(position).getLikesCount()));
                // Like/Unlike listeners
                if (feedList.get(position).isLiked()) {
                    videoViewHolder.likeImageView.setImageResource(R.drawable.like_red);
                } else {
                    videoViewHolder.likeImageView.setImageResource(R.drawable.like);
                }
                videoViewHolder.likeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (feedList.get(position).isLiked()) {
                            dislike(feedList.get(position).getLikesCount(), position, videoViewHolder.likeImageView, videoViewHolder.likeCountTextView);
                        } else {
                            like(feedList.get(position).getLikesCount(), position, videoViewHolder.likeImageView, videoViewHolder.likeCountTextView, videoViewHolder.bigLikeImage);
                        }

                    }
                });
                videoViewHolder.videoView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            if (isVisibleControls) {
                                videoViewHolder.playPauseImageView.setVisibility(View.GONE);
                                isVisibleControls = false;
                            } else {
                                videoViewHolder.playPauseImageView.setVisibility(View.VISIBLE);
                                isVisibleControls = true;
                            }
                        }
                        gestureDetector.onTouchEvent(motionEvent);
                        return true;
                    }

                    private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            if (feedList.get(position).isLiked()) {
                                dislike(feedList.get(position).getLikesCount(), position, videoViewHolder.likeImageView, videoViewHolder.likeCountTextView);
                            } else {
                                like(feedList.get(position).getLikesCount(), position, videoViewHolder.likeImageView, videoViewHolder.likeCountTextView, videoViewHolder.bigLikeImage);
                            }
                            return super.onDoubleTap(e);
                        }
                    });
                });
                // Publish date
                long timestamp3 = Long.parseLong(String.valueOf(feedList.get(position).getPublishedAtTimestamp())) * 1000L;
                videoViewHolder.pubDateTextView.setReferenceTime(timestamp3);
                // Tag
                videoViewHolder.tagGroup.setTags(feedList.get(position).getTags());
                // Share

                break;
        }
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (feedList.get(position).getType()) {
            case "image":
                return IMAGE_VIEW_TYPE;
            case "gif":
                return GIF_VIEW_TYPE;
            case "video":
                return VIDEO_VIEW_TYPE;
            case "ad":
                return AD_VIEW_TYPE;
            default:
                return 0;
        }
    }

    public void addData(ResponseDataModel feedResponseDataModel) {
        feedList.add(feedResponseDataModel);
    }

    public void clearData() {
        feedList.clear();
        notifyDataSetChanged();
    }

    private void dislike(int likeCount, final int position, final ImageView likeImageView, final TextView likeCountTextView) {
        likeImageView.setImageResource(R.drawable.like);
        likeCountTextView.setText(String.valueOf(likeCount - 1));
        feedList.get(position).setLiked(false);
        feedList.get(position).setLikesCount(likeCount - 1);
    }

    private void like(int likeCount, final int position, final ImageView likeImageView, final TextView likeCountTextView, final ImageView bigLikeImage) {
        bigLikeImage.setVisibility(View.VISIBLE);
        bigLikeImage.startAnimation(likeAnimation);
        likeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bigLikeImage.setVisibility(View.GONE);
                    }
                }, 1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        likeImageView.setImageResource(R.drawable.like_red);
        likeCountTextView.setText(String.valueOf(likeCount + 1));
        feedList.get(position).setLiked(true);
        feedList.get(position).setLikesCount(likeCount + 1);
    }


    private class DefaultViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView = null;

        DefaultViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.feed_image_item);
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView = null, likeImageView = null;
        private TextView likeCountTextView = null;
        private RelativeTimeTextView pubDateTextView = null;
        private ImageView bigLikeImage = null;
        private TagContainerLayout tagGroup = null;
        private TextView titleTextView = null;
        private LinearLayout likeLayout = null;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.feed_image_item);
            likeImageView = itemView.findViewById(R.id.feed_like_image_view);
            bigLikeImage = itemView.findViewById(R.id.feed_image_like_item);
            pubDateTextView = itemView.findViewById(R.id.feed_pub_date_text_view);
            likeCountTextView = itemView.findViewById(R.id.feed_like_count_text_view);
            tagGroup = itemView.findViewById(R.id.feed_tag_text_view);
            titleTextView = itemView.findViewById(R.id.header_item_title_text_view);
            likeLayout = itemView.findViewById(R.id.like_layout);
        }
    }

    private class GifViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView = null, likeImageView = null;
        private ImageView bigLikeImage = null;
        private TextView likeCountTextView = null;
        private RelativeTimeTextView pubDateTextView = null;
        private TagContainerLayout tagGroup = null;
        private TextView titleTextView = null;
        private LinearLayout likeLayout = null;


        GifViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.feed_image_item);
            likeImageView = itemView.findViewById(R.id.feed_like_image_view);
            bigLikeImage = itemView.findViewById(R.id.feed_image_like_item);
            pubDateTextView = itemView.findViewById(R.id.feed_pub_date_text_view);
            likeCountTextView = itemView.findViewById(R.id.feed_like_count_text_view);

            tagGroup = itemView.findViewById(R.id.feed_tag_text_view);
            titleTextView = itemView.findViewById(R.id.header_item_title_text_view);
            likeLayout = itemView.findViewById(R.id.like_layout);
        }
    }

    private class VideoViewHolder extends RecyclerView.ViewHolder {
        private EasyVideoPlayer videoView = null;
        private ImageView likeImageView = null;
        private ImageView bigLikeImage = null;
        private TextView likeCountTextView = null;
        private RelativeTimeTextView pubDateTextView = null;
        private TagContainerLayout tagGroup = null;
        private TextView titleTextView = null;
        private CircleImageView playPauseImageView = null;
        private LinearLayout likeLayout = null;

        VideoViewHolder(View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.feed_video_item);
            likeImageView = itemView.findViewById(R.id.feed_like_image_view);
            bigLikeImage = itemView.findViewById(R.id.feed_image_like_item);
            pubDateTextView = itemView.findViewById(R.id.feed_pub_date_text_view);
            likeCountTextView = itemView.findViewById(R.id.feed_like_count_text_view);
            tagGroup = itemView.findViewById(R.id.feed_tag_text_view);
            titleTextView = itemView.findViewById(R.id.header_item_title_text_view);
            playPauseImageView = itemView.findViewById(R.id.play_pause_video_image_view);
            likeLayout = itemView.findViewById(R.id.like_layout);
        }
    }

    private class ADViewHolder extends RecyclerView.ViewHolder {


        ADViewHolder(View itemView) {
            super(itemView);
            NativeContentAdView adView = (NativeContentAdView) itemView;

            // Register the view used for each individual asset.
            adView.setHeadlineView(adView.findViewById(R.id.contentad_headline));
            adView.setImageView(adView.findViewById(R.id.contentad_image));
            adView.setBodyView(adView.findViewById(R.id.contentad_body));
            adView.setCallToActionView(adView.findViewById(R.id.contentad_call_to_action));
            adView.setLogoView(adView.findViewById(R.id.contentad_logo));
            adView.setAdvertiserView(adView.findViewById(R.id.contentad_advertiser));
        }
    }

    class NativeAppInstallAdViewHolder extends RecyclerView.ViewHolder {
        NativeAppInstallAdViewHolder(View view) {
            super(view);
            NativeAppInstallAdView adView = (NativeAppInstallAdView) view;

            // Register the view used for each individual asset.
            // The MediaView will display a video asset if one is present in the ad, and the
            // first image asset otherwise.
            MediaView mediaView = (MediaView) adView.findViewById(R.id.appinstall_media);
            adView.setMediaView(mediaView);
            adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
            adView.setBodyView(adView.findViewById(R.id.appinstall_body));
            adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
            adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
            adView.setPriceView(adView.findViewById(R.id.appinstall_price));
            adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
            adView.setStoreView(adView.findViewById(R.id.appinstall_store));
        }
    }

    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd,
                                          NativeAppInstallAdView adView) {

        // Some assets are guaranteed to be in every NativeAppInstallAd.
        ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon()
                .getDrawable());
        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());

        // These assets aren't guaranteed to be in every NativeAppInstallAd, so it's important to
        // check before trying to display them.
        if (nativeAppInstallAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAppInstallAd.getPrice());
        }

        if (nativeAppInstallAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAppInstallAd.getStore());
        }

        if (nativeAppInstallAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAppInstallAd);
    }

    private void populateContentAdView(NativeContentAd nativeContentAd,
                                       NativeContentAdView adView) {
        // Some assets are guaranteed to be in every NativeContentAd.
        ((TextView) adView.getHeadlineView()).setText(nativeContentAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeContentAd.getBody());
        ((TextView) adView.getCallToActionView()).setText(nativeContentAd.getCallToAction());
        ((TextView) adView.getAdvertiserView()).setText(nativeContentAd.getAdvertiser());

        List<NativeAd.Image> images = nativeContentAd.getImages();

        if (images.size() > 0) {
            ((ImageView) adView.getImageView()).setImageDrawable(images.get(0).getDrawable());
        }

        // Some aren't guaranteed, however, and should be checked.
        NativeAd.Image logoImage = nativeContentAd.getLogo();

        if (logoImage == null) {
            adView.getLogoView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getLogoView()).setImageDrawable(logoImage.getDrawable());
            adView.getLogoView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeContentAd);
    }
}
