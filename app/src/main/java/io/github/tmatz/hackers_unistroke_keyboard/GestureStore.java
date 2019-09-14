package io.github.tmatz.hackers_unistroke_keyboard;

import android.content.Context;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Gesture;
import java.util.ArrayList;
import android.gesture.Prediction;

class GestureStore
{
    public static final int FLAG_GESTURE_ALPHABET = 1;
    public static final int FLAG_GESTURE_NUMBER = 2;
    public static final int FLAG_GESTURE_SPECIAL = 4;
    public static final int FLAG_GESTURE_CONTROL = 8;
    public static final int FLAG_STRICT = 16;

    private final float mPeriodTolerance;
    private final GestureLibrary mAlpabet;
    private final GestureLibrary mNumber;
    private final GestureLibrary mSpecial;
    private final GestureLibrary mControl;

    public GestureStore(Context context)
    {
        mPeriodTolerance = context.getResources().getDimension(R.dimen.period_tolerance);    
        mAlpabet = createGesture(context, R.raw.gestures_alphabet);
        mNumber = createGesture(context, R.raw.gestures_number);
        mSpecial = createGesture(context, R.raw.gestures_special);
        mControl = createGesture(context, R.raw.gestures_control);
    }

    private GestureLibrary createGesture(Context context, int rawId)
    {
        GestureLibrary store = GestureLibraries.fromRawResource(context, rawId);
        store.setOrientationStyle(8);
        store.load();
        return store;
    }

    public PredictionResult predict(Gesture gesture, int flags)
    {
        PredictionResult prediction = new PredictionResult();

        if ((flags & FLAG_GESTURE_CONTROL) != 0)
        {
            prediction = getPrediction(prediction, gesture, mControl, 0.7, flags);
        }

        if ((flags & FLAG_GESTURE_SPECIAL) != 0)
        {
            prediction = getPrediction(prediction, gesture, mSpecial, 1.0, flags);
        }

        if ((flags & FLAG_GESTURE_ALPHABET) != 0)
        {
            prediction = getPrediction(prediction, gesture, mAlpabet, 1.0, flags);
        }

        if ((flags & FLAG_GESTURE_NUMBER) != 0)
        {
            prediction = getPrediction(prediction, gesture, mNumber, 1.0, flags);
        }

        return prediction;
    }

    private PredictionResult getPrediction(PredictionResult previous, Gesture gesture, GestureLibrary store, double scale, int flags)
    {
        ArrayList<Prediction> predictions = store.recognize(gesture);
        if (predictions.size() == 0)
        {
            return previous;
        }

        if (gesture.getLength() < mPeriodTolerance)
        {
            return new PredictionResult("period", Double.NaN);
        }

        PredictionResult current = new PredictionResult(predictions.get(0)).mult(scale);
        if (previous.score > current.score)
        {
            return previous;
        }

        if ((flags & FLAG_STRICT) != 0)
        {
            if (current.score < 1.5)
            {
                return previous;
            }

            if (predictions.size() > 1)
            {
                PredictionResult next = new PredictionResult(predictions.get(1)).mult(scale);
                if (current.score < next.score + 0.2)
                {
                    return previous;
                }
            }
        }

        return current;
    }
}

