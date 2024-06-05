package me.kimsuhwan.springbootdeveloper.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import me.kimsuhwan.springbootdeveloper.config.error.exception.ArticleNotFoundException;
import me.kimsuhwan.springbootdeveloper.config.jwt.TokenProvider;
import me.kimsuhwan.springbootdeveloper.domain.Article;
import me.kimsuhwan.springbootdeveloper.domain.Comment;
import me.kimsuhwan.springbootdeveloper.domain.User;
import me.kimsuhwan.springbootdeveloper.domain.UserLike;
import me.kimsuhwan.springbootdeveloper.dto.AddArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.AddCommentRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateCommentRequest;
import me.kimsuhwan.springbootdeveloper.repository.BlogRepository;
import me.kimsuhwan.springbootdeveloper.repository.CommentRepository;
import me.kimsuhwan.springbootdeveloper.repository.LikeRepository;
import me.kimsuhwan.springbootdeveloper.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor // final이 붙거나 @NotNull이 붙은 필드의 생성자 추가
@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public Article save(AddArticleRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }

    public Page<Article> findAll(Pageable pageable) {
        return blogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Article findById(long id) {
        return blogRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);
    }

    public void delete(long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);

        blogRepository.deleteById(id);
    }

    @Transactional
    public Article update(long id, UpdateArticleRequest request) {
        Article article = blogRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("not found : " + id));

        authorizeArticleAuthor(article);
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    @Transactional
    public void viewSave(Article article) {
        blogRepository.save(article);
    }


    public Comment addComment(AddCommentRequest request, String userName) {
        Article article = blogRepository.findById(request.getArticleId())
                .orElseThrow(ArticleNotFoundException::new);

        return commentRepository.save(request.toEntity(userName, article));
    }

    public void deleteComment(long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + commentId));

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        authorizeCommentAuthor(comment);

        commentRepository.deleteById(commentId);
    }

    @Transactional
    public Comment updateComment(long commentId, UpdateCommentRequest request) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("not found : " + commentId));

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        authorizeCommentAuthor(comment);
        comment.update(request.getContent());

        return comment;
    }

    @Transactional
    public void likeArticle(Long articleId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String provider = tokenProvider.getRegistrationId(getTokenInContextHolder());

        User user = userRepository.findByEmailAndProvider(userName, provider)
                .orElseThrow(()-> new IllegalArgumentException("User not found : " + userName));

        Article article = blogRepository.findById(articleId)
                .orElseThrow(()-> new IllegalArgumentException("Article not found : " + articleId));

        if(likeRepository.findByUserIdAndArticleId(user.getId(), articleId).isEmpty()) {
            UserLike userLike = new UserLike(user, article);
            article.addLike(userLike);
            likeRepository.save(userLike);
        } else {
            throw new IllegalArgumentException("User has already liked this article");
        }
    }

    public boolean likeArticleCheck(Long articleId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String provider = tokenProvider.getRegistrationId(getTokenInContextHolder());

        User user = userRepository.findByEmailAndProvider(userName, provider)
                .orElseThrow(()-> new IllegalArgumentException("User not found : " + userName));

        Article article = blogRepository.findById(articleId)
                .orElseThrow(()-> new IllegalArgumentException("Article not found : " + articleId));

        return likeRepository.findByUserIdAndArticleId(user.getId(), articleId).isPresent();
    }


    private String getTokenInContextHolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = "";
        if (authentication != null) {
            Object credentials = authentication.getCredentials();
            if (credentials instanceof String) {
                token = (String) credentials;
            }
        }
        return token;
    }

    //게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!article.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }

    //댓글을 작성한 유저인지 확인
    private static void authorizeCommentAuthor(Comment comment) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!comment.getAuthor().equals(userName)) {
            throw new IllegalArgumentException("not authorized");
        }
    }


}
