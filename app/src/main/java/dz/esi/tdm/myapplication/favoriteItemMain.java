package dz.esi.tdm.myapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class favoriteItemMain extends AppCompatActivity {
    private List<String> musicFiles;
    private MusicService musicService;
    private boolean isServiceBound;
    private Intent serviceIntent;
    private boolean startBoolean=false;
    private MaBaseManager mDatabase;
    private boolean play=false;
    private ImageView btnplay;
    private TextView songname;
    private ImageView Fav;
    private int clickedPosition;
    private final BroadcastReceiver stopMusicServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("STOP_MUSIC_SERVICE_")) {
                if (isServiceBound && musicService != null) {
                    play=false;
                    btnplay.setImageResource(R.drawable.play);
                    musicService.stop();
                }
            }
        }
    };
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            musicService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnplay=findViewById(R.id.pause);
        Fav=findViewById(R.id.fav);
        Fav.setVisibility(View.INVISIBLE);
        IntentFilter intentFilter = new IntentFilter("STOP_MUSIC_SERVICE_");
        registerReceiver(stopMusicServiceReceiver, intentFilter);
        songname=findViewById(R.id.textView);
        clickedPosition=getIntent().getIntExtra("position",0);
        mDatabase = new MaBaseManager(this);
        retrieveMusicFiles();
    }


    private void retrieveMusicFiles() {
        // Retrieve music files from storage and populate the musicFiles list
        musicFiles = new ArrayList<>();
        musicFiles=getMusicTracks();
        serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putStringArrayListExtra("musicFiles", (ArrayList<String>) musicFiles);
        serviceIntent.putExtra("currentPosition",clickedPosition);
        //ContextCompat.startForegroundService(this, serviceIntent);
        String songName= musicFiles.get(clickedPosition);
        int lastSlashIndex =songName.lastIndexOf('/');
        String extractedString = songName.substring(lastSlashIndex + 1, songName.length() - 4);
        songname.setText(extractedString);
        // Bind to the MusicService
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        unregisterReceiver(stopMusicServiceReceiver);

    }

    public void onPlayClick(View view) {

        if (!play){
            btnplay.setImageResource(R.drawable.pause);
            if(!startBoolean) {
                ContextCompat.startForegroundService(favoriteItemMain.this, serviceIntent);
                startBoolean=true;
                play=true;

            }
            else {
                songname.setText(musicService.getSongName());
                if (isServiceBound && musicService != null) {
                    musicService.play();
                }
                play=true;
            }
        }else {
            btnplay.setImageResource(R.drawable.play);
            if (isServiceBound && musicService != null) {

                play=false;
                musicService.pause();

            }

        }
    }


    public void onNextClick(View view) {
        if (isServiceBound && musicService != null) {
            musicService.playNext();
            btnplay.setImageResource(R.drawable.pause);
            play=true;
            songname.setText(musicService.getSongName());
        }
    }

    public void onPreviousClick(View view) {
        if (isServiceBound && musicService != null) {
            musicService.playPrevious();
            btnplay.setImageResource(R.drawable.pause);
            songname.setText(musicService.getSongName());
            play=true;
        }
    }
    public void onorClick(View view) {

    }
    private ArrayList<String> getMusicTracks() {
        ArrayList<String> trackPaths = new ArrayList<>();
        Cursor cursor = mDatabase.getAllFav();
        if (cursor.moveToFirst()) {
            do {
                String sonnng=cursor.getString(1);
                trackPaths.add(sonnng);
            } while (cursor.moveToNext());
        }
        return trackPaths;
    }
}
