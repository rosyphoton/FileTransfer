package de.tudarmstadt.informatik.pet.pet2018client.audio;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;

public class AudioConverter
{
    private Context context;
    private FFmpegExecuteResponseHandler erh;

    public AudioConverter(Context context)
    {
        this.context = context;

        this.erh = new FFmpegExecuteResponseHandler()
        {
            @Override
            public void onSuccess(String message)
            {
                Log.i("AudioConverter", "File successfully converted: " + message);
            }

            @Override
            public void onProgress(String message)
            {
                Log.i("AudioConverter", "Converting audio file: " + message);
            }

            @Override
            public void onFailure(String message)
            {
                Log.w("AudioConverter", "Failed to convert audio file: " + message);
            }

            @Override
            public void onStart()
            {

            }

            @Override
            public void onFinish()
            {

            }
        };
    }

    public boolean convert(String input, String output)
    {
        try
        {
            String[] cmd = new String[]{"-y", "-i", input, output};
            FFmpeg.getInstance(this.context).execute(cmd, this.erh);
            return true;
        }
        catch(Exception e)
        {
            Log.w("AudioConverter", "Failed to execute FFMPEG command!", e);
        }
        return false;
    }
}
