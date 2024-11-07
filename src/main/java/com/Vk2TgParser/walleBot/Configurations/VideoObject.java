package com.Vk2TgParser.walleBot.Configurations;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class VideoObject {

    private final String title;
    private final String videoUrl;
    private final Integer videoId;

    public VideoObject(String title, String videoUrl, Integer videoId) {
        this.title = title != null ? title : "Untitled";
        this.videoUrl = videoUrl;
        this.videoId = videoId;
    }
}
