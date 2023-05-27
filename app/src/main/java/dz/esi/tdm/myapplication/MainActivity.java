package dz.esi.tdm.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> musicFiles;
    private List<String> musicFilesFav;
    private MusicService musicService;
    private boolean isServiceBound;
    private Intent serviceIntent;
    private boolean startBoolean=false;
    private MaBaseManager mDatabase;
    private boolean play=false;
    private ImageView btnplay;
    private TextView songname;
    private ImageView fav;
    private boolean favoritesong=false;
    private final BroadcastReceiver stopMusicServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("STOP_MUSIC_SERVICE")) {
                if (isServiceBound && musicService != null) {
                    play=false;
                    btnplay.setImageResource(R.drawable.play);
                    musicService.stop();
                    //unbindService(serviceConnection);
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
        songname=findViewById(R.id.textView);
        fav=findViewById(R.id.fav);
        mDatabase = new MaBaseManager(this);
        IntentFilter intentFilter = new IntentFilter("STOP_MUSIC_SERVICE");
        registerReceiver(stopMusicServiceReceiver, intentFilter);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse){
                       retrieveMusicFiles();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }


    private void retrieveMusicFiles() {
        // Retrieve music files from storage and populate the musicFiles list
        musicFiles = new ArrayList<>();

        // Start the MusicService as a foreground service
        musicFiles=getMusicTracks();
        serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putStringArrayListExtra("musicFiles", (ArrayList<String>) musicFiles);
        //ContextCompat.startForegroundService(this, serviceIntent);
        String songName= musicFiles.get(0);
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
                ContextCompat.startForegroundService(this, serviceIntent);
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

    public void onPauseClick(View view) {
        if (isServiceBound && musicService != null) {
            musicService.pause();
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
    public void onFavorClick(View view) {
        if (startBoolean){
            mDatabase.addFavoris(musicService.getSongPath());
        }
        else mDatabase.addFavoris(musicFiles.get(0));
        if (!favoritesong) {
            fav.setImageResource(R.drawable.heartplein);
            favoritesong=true;

        }
        else{
            fav.setImageResource(R.drawable.heartvide);
            favoritesong=false;
        }

    }
    public ArrayList<File> fetchSongs(File file){
        ArrayList arrayList = new ArrayList();
        File[] songs = file.listFiles();
        if(songs !=null){
            for(File myFile: songs){
                if(!myFile.isHidden() && myFile.isDirectory()){
                    arrayList.addAll(fetchSongs(myFile));
                }
                else{
                    if(myFile.getName().endsWith(".mp3") && !myFile.getName().startsWith(".")){
                        arrayList.add(myFile);
                    }
                }
            }
        }
        return arrayList;
    }
    private ArrayList<String> getMusicTracks() {
        ArrayList<String> trackPaths = new ArrayList<>();
        ArrayList<File> mySongs = fetchSongs(Environment.getExternalStorageDirectory());
        for (int i = 0; i < mySongs.size(); i++) {
            trackPaths.add(mySongs.get(i).getAbsolutePath());
        }

        return trackPaths;
    }
    /**************************************   ActionBar configuration   **************************************/
    /********************************************************************************************************/
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.Mes_favoris:
                Intent intr= new Intent(MainActivity.this, favorisList.class);
                startActivity(intr);
                return true;
            case R.id.telecharge:
                finish();
                return(true);
        }
        return super.onOptionsItemSelected(item);
    }
}
