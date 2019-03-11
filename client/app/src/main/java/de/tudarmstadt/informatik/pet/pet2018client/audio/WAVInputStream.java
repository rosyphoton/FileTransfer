package de.tudarmstadt.informatik.pet.pet2018client.audio;

import android.util.Log;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WAVInputStream
{
    private File file;

    private int channels;
    private float sampleRate;
    private int sampleSize;

    public WAVInputStream(String file)
    {
        this.file = new File(file);
        this.readHeader();
    }

    private void readHeader()
    {
        try(FileInputStream fis = new FileInputStream(this.file))
        {
            byte[] buffer = new byte[2];
            fis.skip(22);
            fis.read(buffer);
            this.channels = ByteBuffer.allocate(2).put(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort(0);

            buffer = new byte[4];
            fis.read(buffer);
            this.sampleRate = ByteBuffer.allocate(4).put(buffer).order(ByteOrder.LITTLE_ENDIAN).getInt(0);

            buffer = new byte[2];
            fis.skip(6);
            fis.read(buffer);
            this.sampleSize = ByteBuffer.allocate(2).put(buffer).order(ByteOrder.LITTLE_ENDIAN).getShort(0);



            Log.i("WIS", "Channel: " + this.channels + ", sample size: " + this.sampleSize + ", sample rate: " + this.sampleRate);
        }
        catch(IOException e)
        {
            Log.e("WAVHeader", "Failed to open file '" + this.file.getAbsolutePath() + "'", e);
        }
    }

    public int getChannels()
    {
        return this.channels;
    }

    public int getSampleSize()
    {
        return this.sampleSize;
    }

    public float getSampleRate()
    {
        return this.sampleRate;
    }

    public UniversalAudioInputStream getInputStream() throws FileNotFoundException
    {
        return new UniversalAudioInputStream(new FileInputStream(this.file), new TarsosDSPAudioFormat(this.sampleRate, this.sampleSize, this.channels, true, false));
    }
}
