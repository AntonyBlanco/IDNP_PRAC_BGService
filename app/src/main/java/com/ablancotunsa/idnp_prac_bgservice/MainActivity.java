package com.ablancotunsa.idnp_prac_bgservice;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.SeekBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnPlayPause;
    private SeekBar audioSeekBar;

    private boolean isPlaying = false;
    private AudioService audioService;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            audioService = binder.getService();

            // Configurar SeekBar cuando el servicio esté listo
            audioSeekBar.setMax(audioService.getDuration());

            new Thread(() -> {
                while (true) {
                    if (isPlaying) {
                        runOnUiThread(() -> audioSeekBar.setProgress(audioService.getCurrentPosition()));
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        audioSeekBar = findViewById(R.id.audioSeekBar);

        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                audioService.pauseAudio();
                btnPlayPause.setText("Play");
            } else {
                audioService.playAudio();
                btnPlayPause.setText("Pause");
            }
            isPlaying = !isPlaying;
        });

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}