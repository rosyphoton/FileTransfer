package de.tudarmstadt.informatik.pet.pet2018client.audio;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

public class AudioToTextConverter
{
    private long timespan, start, lastSwitch;

    private int magCounter, patternSize;
    private double totalMag1, totalMag2;
    private LinkedList<Double> avgMag1, avgMag2;
    private LinkedList<Boolean> recSamples, avgSample;

    public AudioToTextConverter(int timespan, long start, int pattermSize)
    {
        this.start = start;
        this.timespan = timespan;
        this.patternSize = pattermSize;
        this.lastSwitch = this.start;

        this.magCounter = 0;
        this.avgMag1 = new LinkedList<>();
        this.avgMag2 = new LinkedList<>();

        this.avgSample = new LinkedList<>();
        this.recSamples = new LinkedList<>();
    }

    public AudioToTextConverter()
    {
        this(1000, 0, 8);
    }

    public synchronized void update(long timestamp, double magnitude_1, double magnitude_2)
    {
        if(timestamp - this.lastSwitch >= this.timespan)
            this.flushBuffer(timestamp);

        //Log.i("ATTC", "[" + timestamp + "]: " + magnitude_1 + ", " + magnitude_2);

        /*this.totalMag1 += magnitude_1;
        this.totalMag2 += magnitude_2;
        this.magCounter++;*/

        this.recSamples.add(magnitude_1 > magnitude_2 ? false : true);
    }

    private void flushBuffer(long timestamp)
    {
        int c0 = 0, c1 = 0;
        while(this.recSamples.size() > 0)
        {
            if(this.recSamples.removeFirst())
                c1++;
            else
                c0++;
        }
        this.avgSample.add((c0 > c1) ? false : true);


        /*this.avgMag1.add(this.totalMag1 / this.magCounter);
        this.avgMag2.add(this.totalMag2 / this.magCounter);
        //Log.i("ATTC", "[Flush]: " + this.totalMag1 + ", " + this.totalMag2 + "; " + this.avgMag1.getLast() + ", " + this.avgMag2.getLast());
        this.totalMag1 = 0;
        this.totalMag2 = 0;
        this.magCounter = 0;*/
        this.lastSwitch += this.timespan;
        Log.i("ATTC", "<===============FLUSH===============>");
    }

    public synchronized String getText()
    {
        this.flushBuffer(this.start);

        /*double[] avg1 = new double[this.avgMag1.size() - this.patternSize];
        double[] avg2 = new double[this.avgMag2.size() - this.patternSize];
        //Log.i("AudioToTextConverter", "Recorded " + avg1.length + "/" + avg2.length + "bits");

        for(int i = 0; i < avg1.length; i++)
            avg1[i] = this.avgMag1.removeFirst();
        for(int i = 0; i < avg2.length; i++)
            avg2[i] = this.avgMag2.removeFirst();

        char c = 0b0;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < avg1.length; i++)
        {
            if(i % 8 == 0 && i != 0)
            {
                sb.append(c);
                c = 0b0;
            }

            c <<= 1;
            if(avg1[i] < avg2[i])
            {
                c ^= 0b1;
                //Log.i("ATTC", "Bit: 1 (" + avg1[i] + ", " + avg2[i] + ")");
            }
            else;
                //Log.i("ATTC", "Bit: 0 (" + avg1[i] + ", " + avg2[i] + ")");
        }
        sb.append(c);*/

        int i = 0;
        char c = 0b0;
        StringBuilder sb = new StringBuilder();
        while(this.avgSample.size() > 8)
        {
            if(i % 8 == 0 && i != 0)
            {
                sb.append(c);
                c = 0b0;
            }

            c <<= 1;
            if(this.avgSample.removeFirst())
            {
                c ^= 0b1;
                //Log.i("ATTC", "Bit: 1 (" + avg1[i] + ", " + avg2[i] + ")");
            }
            else;
            //Log.i("ATTC", "Bit: 0 (" + avg1[i] + ", " + avg2[i] + ")");
            i++;
        }
        //sb.append(c);

        return sb.toString();
    }
}
