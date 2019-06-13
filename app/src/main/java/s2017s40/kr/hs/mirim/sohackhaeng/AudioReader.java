package s2017s40.kr.hs.mirim.sohackhaeng;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AudioReader
{
    FirebaseDatabase database  = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getInstance().getReference();
    String Number;
    public static abstract class Listener
    {
        public static final int ERR_OK = 0;
        public static final int ERR_INIT_FAILED = 1;
        public static final int ERR_READ_FAILED = 2;
        public abstract void onReadComplete(int decibel);
        public abstract void onReadError(int error);
    }

    public int calculatePowerDb(short[] sdata, int off, int samples) {
        double sum = 0;
        double sqsum = 0;
        for (int i = 0; i < samples; i++) {
            final long v = sdata[off + i];
            sum += v;
            sqsum += v * v;
        }
        double power = (sqsum - sum * sum / samples) / samples;

        power /= MAX_16_BIT * MAX_16_BIT;

        double result = Math.log10(power) * 10f + FUDGE;
        return (int)result;
    }

    public AudioReader() {
    }
    public AudioReader(String Number) {
        Number = Number;
    }
    /**
     * Start this reader.
     * @param rate The audio sampling rate, in samples / sec. 그는 오디오 샘플링 속도 (샘플 / 초)
     * @param block Number of samples of input to read at a time. This is
     *      *            different from the system audio buffer size.
                     *              한 번에 읽을 입력 샘플 수입니다. 이것은
                     *      * * 시스템 오디오 버퍼 크기와 다릅니다.
     * @param listener Listener to be notified on each completed read.청취자는 완료된 각 읽기에 대해 통지를받습니다.
     */
    public void startReader(int rate, int block, Listener listener) {
        Log.i(TAG, "Reader: Start Thread");
        synchronized (this) {
            // Calculate the required I/O buffer size.
            //필요한 I / O 버퍼 크기를 계산하십시오.
            int audioBuf = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT) * 2;

            // Set up the audio input.
            audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, audioBuf);
            inputBlockSize = block;
            sleepTime = (long) (1000f / ((float) rate / (float) block));
            inputBuffer = new short[2][inputBlockSize];
            inputBufferWhich = 0;
            inputBufferIndex = 0;
            inputListener = listener;
            running = true;
            readerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    readerRun();
                }
            }, "Audio Reader");
            readerThread.start();
        }
    }

    public void stopReader(){
        Log.i(TAG, "Reader: Signal Stop");
        synchronized (this) {
            running = false;
        }
        try {
            if (readerThread != null){
                readerThread.join();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        readerThread = null;

        // Kill the audio input.
        synchronized (this) {
            if (audioInput != null) {
                audioInput.release();
                audioInput = null;
            }
        }

        Log.i(TAG, "Reader: Thread Stopped");
        //popup창
    }
    /**
     * Main loop of the audio reader. This runs in our own thread.
     * 오디오 리더의 메인 루프. 이것은 우리 자신의 스레드에서 실행됩니다.
     */
    private void readerRun() {
        Log.i(TAG, "readerRun");
        short[] buffer;
        int index, readSize;

        int timeout = 2000;
        try
        {
            Log.i(TAG, String.valueOf(timeout));
            while (timeout > 0 && audioInput.getState() != AudioRecord.STATE_INITIALIZED)
            {
                Thread.sleep(500);
                timeout -= 500;
                Log.i(TAG, String.valueOf(timeout));
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        if (audioInput.getState() != AudioRecord.STATE_INITIALIZED) {

            Log.e(TAG, "Audio reader failed to initialize");
            readError(Listener.ERR_INIT_FAILED);
            running = false;
            return;
        }

        try {
            Log.i(TAG, "Reader: Start Recording");
            audioInput.startRecording();

            while (running) {
                long stime = System.currentTimeMillis();

                if (!running)
                    break;

                readSize = inputBlockSize;
                int space = inputBlockSize - inputBufferIndex;
                if (readSize > space)
                    readSize = space;
                buffer = inputBuffer[inputBufferWhich];
                index = inputBufferIndex;

                synchronized (buffer) {
                    int nread = audioInput.read(buffer, index, readSize);

                    boolean done = false;
                    if (!running)
                        break;

                    if (nread < 0) {
                        Log.e(TAG, "Audio read failed: error " + nread);
                        readError(Listener.ERR_READ_FAILED);
                        running = false;
                        break;
                    }
                    int end = inputBufferIndex + nread;
                    if (end >= inputBlockSize) {
                        inputBufferWhich = (inputBufferWhich + 1) % 2;
                        inputBufferIndex = 0;
                        done = true;
                    }
                    else
                        inputBufferIndex = end;

                    if (done) {
                        readDone(buffer);
                        /* Because our block size is way smaller than the audio
                            buffer, we get blocks in bursts, which messes up
                            the audio analyzer. We don't want to be forced to
                            wait until the analysis is done, because if
                            the analysis is slow, lag will build up. Instead
                            wait, but with a timeout which lets us keep the
                            input serviced.

                            우리의 블록 크기가 오디오보다 작기 때문에
                            버퍼, 우리는 버스트로 블록을 얻습니다.
                            오디오 분석기. 우리는 강제되고 싶지 않다.
                            분석이 완료 될 때까지 기다리십시오. 왜냐하면 if
                            분석이 느리고 지연이 쌓일 것입니다. 대신
                            기다려라.하지만 타임 아웃으로 우리는
                            입력 서비스.
                            */
                        long etime = System.currentTimeMillis();
                        long sleep = sleepTime - (etime - stime);
                        if (sleep < 1000)//시간 바꾸는 곳
                            sleep = 1000;
                        try{
                            buffer.wait(sleep);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        } finally
        {
            Log.i(TAG, "Reader: Stop Recording");
            if (audioInput.getState() == AudioRecord.RECORDSTATE_RECORDING){
                Log.i(TAG, "Reader: Stop Recording");
            }
            audioInput.stop();
        }
    }
    /**
     * Notify the client that a read has completed.
     * @param buffer Buffer containing the data.
     *  읽기가 완료되었음을 클라이언트에게 알립니다.
     * 파라미터 : buffer - 데이터를 포함한 버퍼.
     */
    private void readDone(short[] buffer) {
        synchronized (this) {
            audioData = buffer;
            ++audioSequence;

            short[] buffer2 = null;
            if (audioData != null && audioSequence > audioProcessed) {
                audioProcessed = audioSequence;
                buffer2 = audioData;
            }
            if (buffer2 != null) {
                final int len = buffer2.length;
                inputListener.onReadComplete(calculatePowerDb(buffer2, 0, len));
                buffer2.notify();
            }
        }
    }

    private void readError(int code) {
        inputListener.onReadError(code);
    }
    private static final String TAG = "WindMeter";
    // Our audio input device.
    private AudioRecord audioInput;
    // Our audio input buffer, and the index of the next item to go in.
    private short[][] inputBuffer = null;
    private int inputBufferWhich = 0;
    private int inputBufferIndex = 0;
    // Size of the block to read each time.
    private int inputBlockSize = 0;
    // Time in ms to sleep between blocks, to meter the supply rate.
    // 블록 사이에서 잠자기 시간, 공급 속도를 측정하기위한 시간 (ms)
    private long sleepTime = 0;
    // Listener for input.
    private Listener inputListener = null;
    // Flag whether the thread should be running.
    //스레드가 실행되어야하는지 여부를 플래그 지정합니다.
    private boolean running = false;
    // The thread, if any, which is currently reading. Null if not running.
    // 현재 읽고있는 쓰레드 (있을 경우). 실행 중이 아닌 경우 Null입니다.
    private Thread readerThread = null;
    private short[] audioData;
    private long audioSequence = 0;
    private long audioProcessed = 0;
    private static final float MAX_16_BIT = 32768;
    private static final float FUDGE = 0.6f;
}