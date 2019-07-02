package com.litesnap.open.player.manager;

import com.litesnap.open.player.bean.Video;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static List<Video> mDataList;

    public static List<Video> getDataList() {
        if (mDataList == null){
            mDataList = new ArrayList<>();
            mDataList.add(new Video("https://download.ted.com/talks/AlGore_2006-480p.mp4?apikey=c692baa7dafbf74e4fbde1ac88b44cafc96cfe7a"));
            mDataList.add(new Video("https://download.ted.com/talks/AmySmith_2006-light.mp4?apikey=c692baa7dafbf74e4fbde1ac88b44cafc96cfe7a"));
        }
        return mDataList;
    }
}
