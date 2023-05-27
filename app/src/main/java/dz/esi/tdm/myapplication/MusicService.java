package dz.esi.tdm.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    private static final int NOTIFICATION_ID = 1;
    private MediaPlayer mediaPlayer;
    private List<String> musicFiles;
    private int currentSongIndex;
    private final IBinder binder = new LocalBinder();
    private MyReceiver recv;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        musicFiles = new ArrayList<>();
        currentSongIndex = 0;
        recv=new MyReceiver();
        registerReceiver(recv,new IntentFilter("PlayPause"));
        registerReceiver(recv,new IntentFilter("next"));
        registerReceiver(recv,new IntentFilter("previous"));
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrieve the music files from the intent or any other source
        musicFiles = intent.getStringArrayListExtra("musicFiles");
        currentSongIndex =intent.getIntExtra("currentPosition",0);
        if (musicFiles != null && musicFiles.size() > 0) {
            playSong(currentSongIndex);
        }

        // Create and display the foreground notification
        //createNotification();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playNext();
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
    public void playNext() {
        if (currentSongIndex < musicFiles.size() - 1) {
            currentSongIndex++;
            playSong(currentSongIndex);
        } else {
            // Handle end of playlist
            stopSelf();
        }
    }

    public void playPrevious() {
        if (currentSongIndex > 0) {
            currentSongIndex--;
            playSong(currentSongIndex);
        }
    }
    public String getSongName(){
        String songName= musicFiles.get(currentSongIndex);
        int lastSlashIndex =songName.lastIndexOf('/');
        String extractedString = songName.substring(lastSlashIndex + 1, songName.length() - 4);
        return extractedString;
    }
    public String getSongPath(){
        return musicFiles.get(currentSongIndex);
    }
    private void playSong(int songIndex) {
        try {
            String songName= musicFiles.get(songIndex);
            int lastSlashIndex =songName.lastIndexOf('/');
            String extractedString = songName.substring(lastSlashIndex + 1, songName.length() - 4);
            createNotification(extractedString);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicFiles.get(songIndex));
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        unregisterReceiver(recv);
    }

    private void createNotification(String index) {
        //createNotification();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        PendingIntent pendingIntent= PendingIntent.getActivity(
                getApplicationContext(),
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT // setting the mutability flag
        );
        PendingIntent pPPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("PlayPause"),PendingIntent.FLAG_IMMUTABLE);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("next"),PendingIntent.FLAG_IMMUTABLE);
        PendingIntent previousPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("previous"),PendingIntent.FLAG_IMMUTABLE);
        // Notification Channel
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "my_channel_id";
        CharSequence channelName = "My Chan nel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new
                    NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(notificationChannel);}
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this,channelId)
                    .setContentTitle("Lecture en cours")
                    .setContentText(index)
                    .setSmallIcon(R.drawable.music_icon)
                    .addAction(R.drawable.pp, "previous", previousPendingIntent)
                    .addAction(R.drawable.pp, "Play/Pause", pPPendingIntent)
                    .addAction(R.drawable.pp, "next", nextPendingIntent)
                    .setContentIntent(pendingIntent)
                    .setPriority(Notification.PRIORITY_MAX)
                    .build();
        }
        startForeground(110, notification);
    }
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("PlayPause")) {
                if(mediaPlayer.isPlaying()) {mediaPlayer.pause();}
                else {mediaPlayer.start();}
            }
            if (action.equals("next")) {
                playNext();
            }
            if (action.equals("previous")) {
                playPrevious();
            }
        }

    }

}
