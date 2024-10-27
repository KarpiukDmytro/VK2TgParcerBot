package com.Vk2TgParser.walleBot.Configurations;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.video.Video;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.wall.WallItem;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.queries.video.VideoGetQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@Slf4j
public class PostData {

    private String text;
    private List<String> photoUrls;
    private List<VideoObject> videoObjects;
    private final Integer postID;

    public PostData(String text, List<String> photoUrls, List<VideoObject> videoObjects, Integer postID) {
        this.text = text;
        this.photoUrls = photoUrls;
        this.videoObjects = videoObjects; // Инициализация ссылок на видео
        this.postID = postID;
    }

    @Override
    public String toString() {
        return "PostData{" +
                "text = '" + text + '\'' +
                ", photoUrls = " + photoUrls +
                ", videoObjects = " + videoObjects + // Добавлено для вывода ссылок на видео
                ", postID = "+ postID +
                '}';
    }

    // Извлечение ссылок на фото
    public static List<String> extractPhotoUrls(WallItem post) {
        List<String> photoUrls = new ArrayList<>();
        for (WallpostAttachment attachment : post.getAttachments()) {
            if (attachment.getPhoto() != null) {
                photoUrls.add(attachment.getPhoto().getSizes().stream()
                        .max(Comparator.comparingInt(PhotoSizes::getWidth))
                        .map(size -> size.getUrl().toString())
                        .orElse(null));
            }
        }
        return photoUrls;
    }

    // Извлечение ссылок на видео через video.get
    public static List<VideoObject> extractVideoObjects(WallItem post, VkApiClient vk, UserActor actor) {
        List<VideoObject> videoObjects = new ArrayList<>();

        if (post.getAttachments() != null) {
            post.getAttachments().forEach(attachment -> {
                if ("video".equals(attachment.getType().getValue())) {
                    Long ownerId = attachment.getVideo().getOwnerId();
                    Integer videoId = attachment.getVideo().getId();
                    String title = attachment.getVideo().getTitle();

                    try {
                        VideoGetQuery videoGetQuery = vk.video()
                                .get(actor)
                                .videos(ownerId + "_" + videoId);
                        List<VideoFull> videos = videoGetQuery.execute().getItems();

                        if (!videos.isEmpty()) {
                            Video video = videos.get(0);
                            String videoUrl = String.valueOf(video.getPlayer()); // Получаем ссылку на видео
                            videoObjects.add(new VideoObject(title,videoUrl));
                        } else {
                            log.warn("Не удалось получить видео для ID: {}", ownerId + "_" + videoId);
                        }
                    } catch (ApiException | ClientException e) {
                        log.error("Ошибка при получении видео по ID: {}", ownerId + "_" + videoId, e);
                    }
                }
            });
        }

        return videoObjects;
    }
}
