package com.KarpiukJava.walleBot.services;

import com.KarpiukJava.walleBot.Configurations.PostData;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.GetFilter;
import com.vk.api.sdk.objects.wall.WallItem;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.queries.wall.WallGetQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class VKWallPostParser {

    @Value("${vk.tokenGroup}")
    private String tokenGroup;

    @Value("${vk.tokenApp}")
    private String tokenApp;

    @Value("${vk.domain}")
    private String domain;

    @Value("${vk.groupID}")
    private Long groupID;

    @Value("${request.filter}")
    private String filter;

    @Value("${request.count}")
    private int postCount;

    private final LastPostIdService lastPostIdService;
    private final VkApiClient vk;
    private final UserActor actor;

    public VKWallPostParser(LastPostIdService lastPostIdService) {
        this.lastPostIdService = lastPostIdService;

        HttpTransportClient httpClient = HttpTransportClient.getInstance();
        this.vk = new VkApiClient(httpClient);
        this.actor = new UserActor(groupID, tokenGroup);
    }

    public List<PostData> parsePosts() {
        try {
            Optional<Long> lastPostIDOptional = lastPostIdService.getLastPostId();
            long lastPostID = lastPostIDOptional.orElse(0L);
            log.info("Последний обработанный пост имел ID: {}", lastPostID);
            List<PostData> parsedPosts = new ArrayList<>();

            WallGetQuery query = vk.wall()
                    .get(actor)
                    .domain(domain)
                    .count(postCount)
                    .filter(GetFilter.valueOf(filter));

            List<WallItem> posts = query.execute().getItems();

            if (!posts.isEmpty()) {
                for (WallpostFull post : posts) {
                    if (post.getId() > lastPostID) {
                        parsedPosts.add(processPost(post));
                    }
                }

                long newLastPostID = posts.getFirst().getId();
                lastPostIdService.updateLastPostId(newLastPostID);

                return parsedPosts;
            }
        } catch (Exception e) {
            log.error("Ошибка при запросе к VK API", e);
        }
        return new ArrayList<>();
    }

    private PostData processPost(WallpostFull post) {
        log.info("Обрабатываем пост с ID: {}", post.getId());

        String postText = post.getText();
        List<String> postPhotoUrls = PostData.extractPhotoUrls(post);

        return new PostData(postText, postPhotoUrls);
    }
}
