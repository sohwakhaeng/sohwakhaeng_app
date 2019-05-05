package s2017s40.kr.hs.mirim.sohackhaeng;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends Activity
{
    private AudioReader audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);


       Button startStopButton = (Button)findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStart(view);
            }
        });
    }

    public void doStart(View v)
    {
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener()
        {
            @Override
            public final void onReadComplete(int dB)
            {
                receiveDecibel(dB);
            }

            @Override
            public void onReadError(int error)
            {

            }
        });
    }

    private void receiveDecibel(final int dB)
    {
        Log.e("###", dB+" dB");
    }

    public void doStop(View v)
    {
        audioReader.stopReader();
    }
}
