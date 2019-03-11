package de.tudarmstadt.informatik.pet.pet2018client.audio;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPatternRecognizer
{
    private long timespan, lastSwitch;
    private double magnitude;
    private boolean[] pattern;
    private boolean recording;

    private int magCounter;
    private double[] totalMags;
    private ArrayList<Boolean> lastBits;
    private LinkedList<Boolean> recSamples;

    public final AtomicBoolean recognized;

    public AudioPatternRecognizer(boolean[] pattern, int timespan, double magnitude)
    {
        this.timespan = timespan;
        this.magnitude = magnitude;

        this.pattern = pattern.clone();
        this.totalMags = new double[2];

        this.magCounter = 0;
        this.recording = false;
        this.lastBits = new ArrayList<>();
        this.recognized = new AtomicBoolean(false);

        this.recSamples = new LinkedList<>();
    }

    public void update(long timestamp, double power0, double power1)
    {
        if(!this.recording && ((!this.pattern[0] && power0 > this.magnitude) || (this.pattern[0] && power1 > this.magnitude)))
        {
            Log.i("APR", "Start recording...");
            this.recording = true;
            this.lastSwitch = timestamp;
        }

        if(this.recording)
        {
            if(timestamp - this.lastSwitch >= this.timespan)
                this.flush(timestamp);

            this.recSamples.add((power0 > power1) ? false : true);

            this.totalMags[0] += power0;
            this.totalMags[1] += power1;
            this.magCounter++;
        }
    }

    private void flush(long timestamp)
    {
        /*if(this.totalMags[0] > this.totalMags[1])
            this.lastBits.add(false);
        else
            this.lastBits.add(true);*/

        int c0 = 0, c1 = 0;
        while(this.recSamples.size() > 0)
        {
            if(this.recSamples.removeFirst())
                c1++;
            else
                c0++;
        }
        this.lastBits.add((c0 > c1) ? false : true);

        //Log.i("APR[" + timestamp + "]", "Found: " + (this.lastBits.get(this.lastBits.size() - 1) ? "1" : "0"));

        if(this.checkPattern())
        {
            this.recognized.set(true);
            Log.i("APR", "Pattern found!");
        }

        this.magCounter = 0;
        this.totalMags[0] = 0;
        this.totalMags[1] = 0;

        this.lastSwitch += this.timespan;
        //Log.i("APR", "<===============FLUSH===============>");
    }

    private boolean checkPattern()
    {
        //Log.i("APR", "Pattern length: " + this.lastBits.size());
        if(this.lastBits.size() == this.pattern.length)
        {
            boolean patternRec = true;
            for(int i = 0; i < this.pattern.length; i++)
            {
                if(this.pattern[i] != this.lastBits.get(i))
                {
                    patternRec = false;
                    break;
                }
            }
            this.lastBits.remove(0);

            return patternRec;
        }
        else
            return false;
    }
}