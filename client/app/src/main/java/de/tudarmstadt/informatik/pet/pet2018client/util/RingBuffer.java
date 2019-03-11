package de.tudarmstadt.informatik.pet.pet2018client.util;

import de.tudarmstadt.informatik.pet.pet2018client.motion.DataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RingBuffer<T extends Object> {

    private Object[] buffer;
    private int length;
    private int insertPosition;

    private List<WrapCallback> wrapCallbacks;

    public RingBuffer(int capacity) {
        this.length = capacity;
        this.wrapCallbacks = new ArrayList<>();
        this.reset(0);
    }

    public void reset(int offset) {
        this.buffer = new Object[length];
        for(int i = 0; i < offset; i++) {
            this.buffer[i] = new DataPoint(0, 0, 0);
        }
        this.insertPosition = offset;
    }

    public void addCallback(WrapCallback callback) {
        wrapCallbacks.add(callback);
    }

    public void enqueue(T element) {
        buffer[insertPosition++] = element;

        // is the buffer wrapping around?
        if(insertPosition >= length) {
            for (WrapCallback callback: wrapCallbacks) {
                callback.onBufferWrap();
            }
        }

        insertPosition = insertPosition % length;
    }

    public List<T> getElements() {
        return new ArrayList<T>(Arrays.asList((T[]) buffer));
    }
}