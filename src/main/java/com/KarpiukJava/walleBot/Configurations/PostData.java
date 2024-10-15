package com.KarpiukJava.walleBot.Configurations;

import com.vk.api.sdk.objects.photos.PhotoSizes;
import com.vk.api.sdk.objects.wall.WallpostAttachment;
import com.vk.api.sdk.objects.wall.WallpostFull;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
public class PostData {
    private final String text;
    private final List<String> photoUrls;

    /* Переменные для будущей доработки */
//    private List<String> audioUrls;
//    private List<String> videoUrls;
//    private List<String> DocumentUrls;

    public PostData(String text, List<String> photoUrls) {
        this.text = text;
        this.photoUrls = photoUrls;
    }

    /* Методы для будущей доработки */
//  public List<String> getAudioUrls() {return audioUrls;}
//  public List<String> getVideoUrls() {return videoUrls;}
//  public List<String> getDocumentUrls() {return DocumentUrls;}


    @Override
    public String toString() {
        return "PostData{" +
                "text='" + text + '\'' +
                ", photoUrls=" + photoUrls +
                '}';
    }

    // Метод для извлечения ссылок на фото
    public static List<String> extractPhotoUrls(WallpostFull post) {

        List<String> photoUrls = new ArrayList<>();

        for (WallpostAttachment attachment : post.getAttachments()) {
            if ("photo".equals(attachment.getType().getValue())) {
                //Ссылка на фото с шириной 2560px
                String url = String.valueOf(attachment.getPhoto().getPhoto256());
                // Если ссылки нет, находим фото с самым большим размером
                if (url == null) {
                    List<PhotoSizes> sizes = attachment.getPhoto().getSizes();
                    url = String.valueOf(sizes.stream()
                            .max(Comparator.comparingInt(size -> size.getHeight() * size.getWidth()))
                            .map(PhotoSizes::getUrl)
                            .orElse(null));
                    }
                // Если ссылка на фото присутствует, то добавляем её в список фотографий
                if (url != null) {
                    photoUrls.add(url);
                }
            }
        }
        return photoUrls;
    }
}
