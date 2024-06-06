package me.kimsuhwan.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.kimsuhwan.springbootdeveloper.config.SortOrder;
import me.kimsuhwan.springbootdeveloper.config.jwt.TokenProvider;
import me.kimsuhwan.springbootdeveloper.domain.Article;
import me.kimsuhwan.springbootdeveloper.dto.ArticleListViewResponse;
import me.kimsuhwan.springbootdeveloper.dto.ArticleResponse;
import me.kimsuhwan.springbootdeveloper.dto.ArticleViewResponse;
import me.kimsuhwan.springbootdeveloper.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class BlogViewController {

    private final BlogService blogService;
    private final TokenProvider tokenProvider;

    @GetMapping("/articles")
    public String getArticles(
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "5", name = "size") int size,
            @RequestParam(defaultValue = "LATEST", name = "order") String order,
            @RequestParam(name = "token", required = false) String token,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articlePage;
        SortOrder sortOrder = SortOrder.fromString(order);

        switch (sortOrder) {
            case LIKES:
                articlePage = blogService.findAllOrderByLikes(pageable);
                break;
            case VIEWS:
                articlePage = blogService.findAllOrderByViews(pageable);
                break;
            case LATEST:
            default:
                articlePage = blogService.findAll(pageable);
                break;
        }


        List<ArticleListViewResponse> articles = articlePage.stream()
                .map(ArticleListViewResponse::new)
                .toList();

        model.addAttribute("articles", articles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("order", sortOrder);
        model.addAttribute("size", size);

        return "articleList";
    }

    @GetMapping("/articles/{id}")
    public String getArticle(@PathVariable Long id,
                             @RequestParam(defaultValue = "0", name = "page") int page,
                             @RequestParam(defaultValue = "LATEST", name = "order") String order,
                             Model model){

        Article article = blogService.findById(id);
        article.incrementViews();
        blogService.viewSave(article);


        model.addAttribute("userLiked", blogService.likeArticleCheck(id));
        model.addAttribute("article", new ArticleViewResponse(article));
        model.addAttribute("page", page);
        model.addAttribute("order", order);

        return "article";
    }

    @GetMapping("/new-article")
    public String newArticle(@RequestParam(required = false) Long id, Model model) {
        if(id == null) {
            model.addAttribute("article", new ArticleViewResponse());
        } else {
            Article article = blogService.findById(id);
            model.addAttribute("article", new ArticleViewResponse(article));
        }

        return "newArticle";
    }

}
