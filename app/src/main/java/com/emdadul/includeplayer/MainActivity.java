package com.emdadul.includeplayer;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.emdadul.EPlayer;
import com.emdadul.VideoInfoRepository;
import com.emdadul.listener.OnEventListener;
import com.emdadul.listener.ResponseListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private PlayListStorage playListStorage;
    EPlayer ePlayer;
    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private String[] videoIds;
    private int currentVideoIndex = 0; // বর্তমান ভিডিও ইনডেক্স ট্র্যাক করার জন্য
    private static final long ONE_DAY = 24 * 60 * 60 * 1000L;
    private static final String PLAYLIST_ID = "UUkKDpnzIG29MA--Sh6oOtWg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playListStorage = new PlayListStorage(this);
        ePlayer = findViewById(R.id.ePlayer);
        recyclerView = findViewById(R.id.recyclerVew);



        long lastFetch = playListStorage.getLastFetchTime();
        long now = System.currentTimeMillis();
        List<String> savedList = playListStorage.loadPlaylist();






        if ((now - lastFetch) < ONE_DAY && !savedList.isEmpty()) {
            // ক্যাশ থেকে লোড করো
            videoIds = savedList.toArray(new String[0]);
            setupRecyclerAndPlayer(videoIds);

            ePlayer.addOnEventListener(new OnEventListener() {
                @Override
                public void onPlayerReady() {
                    ePlayer.loadVideo(videoIds[0]);
                    currentVideoIndex = 0;
                }

                @Override
                public void onPlaybackStateChange(int playerState) {
                    if (videoIds == null && playerState == EPlayer.CUED) {
                        ePlayer.onReceiveVideoIds();
                    }
                    // ভিডিও শেষ হলে পরবর্তী ভিডিও চালু করো
                    if (playerState == EPlayer.ENDED) {
                        playNextVideo();
                    }
                }

                @Override
                public void onReceiveVideoIds(String[] videoIds) {
                    // ইমপ্লিমেন্টেশন যুক্ত করুন
                }
            });

        } else {
            // সার্ভার থেকে প্লেলিস্ট লোড করো
            fetchPlaylistFromServer();
        }



        getInfo();

    }//=================================================================

    // পরবর্তী ভিডিও চালু করার মেথড
    private void playNextVideo() {
        if (videoIds != null && videoIds.length > 0) {
            currentVideoIndex = (currentVideoIndex + 1) % videoIds.length;
            ePlayer.loadVideo(videoIds[currentVideoIndex]);
            // রিসাইক্লার ভিউ আপডেট করুন
            if (adapter != null) {
                adapter.updateCurrentPosition(currentVideoIndex);
                recyclerView.smoothScrollToPosition(currentVideoIndex);
            }
        }
    }

    private void setupRecyclerAndPlayer(String[] videoIds) {
        List<String> thumbnails = new ArrayList<>();
        for (String id : videoIds) thumbnails.add(id);
        adapter = new RecyclerAdapter(thumbnails, this, position -> {
            if (videoIds != null && position < videoIds.length) {
                currentVideoIndex = position;
                ePlayer.loadVideo(videoIds[position]);
                if (adapter != null) {
                    adapter.updateCurrentPosition(position);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        ePlayer.cueVideo(videoIds[0], 0);
        currentVideoIndex = 0;
    }

    private void fetchPlaylistFromServer() {
        ePlayer.addOnEventListener(new OnEventListener() {
            @Override
            public void onPlayerReady() {
                ePlayer.cueVideo(PLAYLIST_ID, 0);
            }

            @Override
            public void onPlaybackStateChange(int playerState) {
                if (videoIds == null && playerState == EPlayer.CUED) {
                    ePlayer.onReceiveVideoIds();
                }
                // ভিডিও শেষ হলে পরবর্তী ভিডিও চালু করো
                if (playerState == EPlayer.ENDED) {
                    playNextVideo();
                }
            }

            @Override
            public void onReceiveVideoIds(String[] ids) {
                videoIds = ids;
                List<String> list = new ArrayList<>();
                for (String id : ids) list.add(id);
                playListStorage.savePlaylist(list);
                playListStorage.saveLastFetchTime(System.currentTimeMillis());
                setupRecyclerAndPlayer(ids);
            }
        });
    }



    public void getInfo(){

        VideoInfoRepository infoRepository = new VideoInfoRepository(this);
        infoRepository.getInfo(PLAYLIST_ID, new ResponseListener() {
            @Override
            public void onResponse(@NonNull JSONObject jsonObject) {

                new AlertDialog.Builder(MainActivity.this).setMessage(jsonObject.toString()).show();
            }
        });

    }







}



