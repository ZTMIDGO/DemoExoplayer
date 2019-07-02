package com.litesnap.open.player;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.litesnap.open.player.bean.Video;
import com.litesnap.open.player.manager.DataManager;
import com.litesnap.open.player.manager.ThumableManager;
import com.litesnap.open.player.utils.BindImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListActivity extends AppCompatActivity {
    public static final String TAG = "ListActivity";
    private static final int REQUEST_CODE = 1;
    private ExecutorService mExec;
    private RecyclerView mRecyclerView;
    private ImageView mTargetView;

    private int mTargetIndex;
    private Handler mHandler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                names.clear();
                sharedElements.clear();

                View view = mTargetView;
                if (view != null){
                    String name = ViewCompat.getTransitionName(view);

                    names.add(name);
                    sharedElements.put(name, view);
                }
                super.onMapSharedElements(names, sharedElements);
            }
        });

        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onPreDraw() {
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return false;
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mExec = Executors.newCachedThreadPool();
        mHandler = new Handler();

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ListActivity.this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new VideoAdapter(DataManager.getDataList()));
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        postponeEnterTransition();
        long index = data.getLongExtra(VideoActivity.CURRENT, 0);
        DataManager.getDataList().get(mTargetIndex).setCurrent(index);

        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onPreDraw() {
                if (ThumableManager.getDrawable() != null){
                    mTargetView.setImageDrawable(ThumableManager.getDrawable());
                }
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return false;
            }
        });

        super.onActivityReenter(resultCode, data);
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.Holder>{
        private List<Video> mDataList;
        private LayoutInflater mInflater;

        public VideoAdapter(List<Video> list){
            mDataList = list;
            mInflater = LayoutInflater.from(ListActivity.this);
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.holder_video, viewGroup, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int i) {
            holder.bindHolder(mDataList.get(i));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        public class Holder extends RecyclerView.ViewHolder {
            private ImageView mImageView;
            public Holder(@NonNull View itemView) {
                super(itemView);
                mImageView = itemView.findViewById(R.id.image);
            }

            public void bindHolder(final Video bean){
                ViewCompat.setTransitionName(mImageView, String.valueOf(bean.getUUID()));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTargetView = mImageView;
                        mTargetIndex = mDataList.indexOf(bean);
                        Log.i(TAG, "onClick: "+bean.getCurrent());
                        ThumableManager.setDrawable(mImageView.getDrawable());
                        Intent intent = new Intent(ListActivity.this, VideoActivity.class);
                        intent.putExtra(VideoActivity.BEAN, bean);
                        Pair<View, String> pair = new Pair<View, String>(mImageView, String.valueOf(bean.getUUID()));
                        ActivityOptionsCompat optionsCompat =
                                ActivityOptionsCompat.makeSceneTransitionAnimation(ListActivity.this, pair);
                        startActivityForResult(intent, REQUEST_CODE, optionsCompat.toBundle());
                    }
                });

                mExec.execute(new Runnable() {
                    @Override
                    public void run() {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(bean.getUrl(), new HashMap<String, String>());
                        final Bitmap bitmap = retriever.getFrameAtTime();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mImageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }
        }
    }
}
