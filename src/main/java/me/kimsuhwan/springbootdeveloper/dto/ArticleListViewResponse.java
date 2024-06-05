package me.kimsuhwan.springbootdeveloper.dto;

import lombok.Getter;
import me.kimsuhwan.springbootdeveloper.domain.Article;

import java.time.LocalDateTime;

@Getter
public class ArticleListViewResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final int likes;
    private final int views;
    private final String author;
    private LocalDateTime createdAt;

    public ArticleListViewResponse(Article article){
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getTitle();
        this.createdAt = article.getCreatedAt();
        this.likes = article.getLikes();
        this.views = article.getViews();
        this.author = article.getAuthor();
    }
}
