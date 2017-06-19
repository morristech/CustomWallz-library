package com.dm.wallpaper.board.items;

/*
 * Wallpaper Board
 *
 * Copyright (c) 2017 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class PlaylistItem {

    private final int mId;
    private final String mPlaylistName;
    private String mUrl;

    public PlaylistItem(int id, String playlistName) {
        mId = id;
        mPlaylistName = playlistName;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mPlaylistName;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;
        if (object != null && object instanceof PlaylistItem) {
            equals = mPlaylistName.equals(((PlaylistItem) object).getName());
        }
        return equals;
    }
}
