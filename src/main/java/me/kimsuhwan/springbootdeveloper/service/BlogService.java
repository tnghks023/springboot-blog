package me.kimsuhwan.springbootdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.kimsuhwan.springbootdeveloper.config.error.exception.ArticleNotFoundException;
import me.kimsuhwan.springbootdeveloper.domain.Article;
import me.kimsuhwan.springbootdeveloper.domain.Comment;
import me.kimsuhwan.springbootdeveloper.dto.AddArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.AddCommentRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateCommentRequest;
import me.kimsuhwan.springbootdeveloper.repository.BlogRepository;
import me.kimsuhwan.springbootdeveloper.repository.CommentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor // final이 붙거나 @NotNull이 붙은 필드의 생성자 추가
@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;

    public Article save(AddArticleRequest request, String userName) {
        return blogRepository.save(request.toEntity(userName));
    }

    public List<Article> findAll(){
        return blogRepository.findAll();
    }

    public Article findById(long id ) {
        return blogRepository.findById(id)
                .orElseThrow(ArticleNotFoundException::new);
    }

    public void delete(long id) {
        Article article = blogRepository.findById(id)
                        .orElseThrow( () -> new IllegalArgumentException("not found : " + id));

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



    //게시글을 작성한 유저인지 확인
    private static void authorizeArticleAuthor(Article article) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        if(!article.getAuthor().equals(userName)){
            throw new IllegalArgumentException("not authorized");
        }
    }

    //댓글을 작성한 유저인지 확인
    private static void authorizeCommentAuthor(Comment comment) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        if(!comment.getAuthor().equals(userName)){
            throw new IllegalArgumentException("not authorized");
        }
    }


}
