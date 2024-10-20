package com.Vk2TgParser.walleBot.Configurations;

import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Slf4j
public class PostData {
    private final String text;
    private final List<String> photoUrls;
    @Getter
    private final Integer postID;

    /* Переменные для будущей доработки */
//    private List<String> audioUrls;
//    private List<String> videoUrls;
//    private List<String> DocumentUrls;

    public PostData(String text, List<String> photoUrls, Integer postID) {
        this.text = text;
        this.photoUrls = photoUrls;
        this.postID = postID;
    }

    /* Методы для будущей доработки */
//  public List<String> getAudioUrls() {return audioUrls;}
//  public List<String> getVideoUrls() {return videoUrls;}
//  public List<String> getDocumentUrls() {return DocumentUrls;}


    @Override
    public String toString() {
        return "PostData{" +
                "text = '" + text + '\'' +
                ", photoUrls = " + photoUrls +
                ", postID = "+ postID +
                '}';
    }

    // Метод для извлечения ссылок на фото
    public static List<String> extractPhotoUrls(WallpostFull post) {

        List<String> photoUrls = new ArrayList<>();

        for (WallpostAttachment attachment : post.getAttachments()) {
            if ("photo".equals(attachment.getType().getValue())) {
                    List<PhotoSizes> sizes = attachment.getPhoto().getSizes();
                    URI url = sizes.stream()
                            .max(Comparator.comparingInt(size -> size.getHeight() * size.getWidth()))
                            .map(PhotoSizes::getUrl)
                            .orElse(null);
                // Записываем в логи ссылку на фото
                log.info("Ссылка на фото: {}", url);
                // Если ссылка на фото присутствует, то добавляем её в список фотографий
                if (url != null) {
                    photoUrls.add(String.valueOf(url));
                }
            }
        }
        return photoUrls;
    }

}
