package com.litesnap.open.player.utils;

import android.util.Log;

public class ScaleUtil {
    public static float matchingSize(float originalW, float originalH, float targetW, float targetH){
        originalW = originalW * 1.0f;
        originalH = originalH * 1.0f;
        targetW = targetW * 1.0f;
        targetH = targetH * 1.0f;
        if (targetW > originalW || targetH > originalH){
            float sx = originalW /  targetW;
            float sy = originalH / targetH;
            Log.i("Match max", "matchingSize: "+sx+" "+sy);
            return sx < sy ? sx : sy;
        }else if (targetW < originalW || targetH < originalH){
            float sx = originalW / targetW ;
            float sy = originalH / targetH;
            Log.i("Match min", "matchingSize: "+sx+" "+sy);
            return sx < sy ? sx : sy;
        }else {
            return 1.0f;
        }
    }
}
