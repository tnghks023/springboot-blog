package me.kimsuhwan.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimsuhwan.springbootdeveloper.config.error.ErrorCode;
import me.kimsuhwan.springbootdeveloper.domain.Article;
import me.kimsuhwan.springbootdeveloper.domain.Comment;
import me.kimsuhwan.springbootdeveloper.domain.User;
import me.kimsuhwan.springbootdeveloper.dto.AddArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.AddCommentRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateCommentRequest;
import me.kimsuhwan.springbootdeveloper.repository.BlogRepository;
import me.kimsuhwan.springbootdeveloper.repository.CommentRepository;
import me.kimsuhwan.springbootdeveloper.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.as;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성 및 자동 구성
class BlogApiControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper; //직렬화, 역직렬화를 위한 클래스

    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    User user;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        blogRepository.deleteAll();
        commentRepository.deleteAll();
    }

    @BeforeEach
    void setSecurityContext() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .email("user@email.com")
                .password("test")
                .build());

        SecurityContext context = SecurityContextHolder.getContext();
        // 인증 객체를 저장하는 시큐리티 콘텍스트에 테스트 유저를 지정
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @DisplayName("addArticle: 블로그 글 추가에 성공한다")
    @Test
    public void addArticle() throws Exception {
        //given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        //객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        //when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(principal)
                .content(requestBody));

        //then
        result.andExpect(status().isCreated());
        List<Article> articles = blogRepository.findAll();
        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
    @Test
    public void findAllArticles() throws Exception {
        //given
        final String url = "/api/articles";
        Article savedArticle = createDefaultArticle();

        //when
        final ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$[0].title").value(savedArticle.getTitle()));
    }

    @DisplayName("findArticle: 블로그 글 조회에 성공한다.")
    @Test
    public void findArticle() throws Exception {
        //given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        //when
        final ResultActions result = mockMvc.perform(get(url, savedArticle.getId()));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(savedArticle.getContent()))
                .andExpect(jsonPath("$.title").value(savedArticle.getTitle()));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception {
        //given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        //when
        mockMvc.perform(delete(url, savedArticle.getId())).andExpect(status().isOk());

        //then
        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception {
        //given
        final String url = "/api/articles/{id}";
        Article savedArticle = createDefaultArticle();

        final String newTitle = "new Title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        //when
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }

    @DisplayName("addArticle : 아트클 추가할 때 title이 null이면 실패한다")
    @Test
    public void addArticleNullValidation() throws Exception{
        //given
        final String url = "/api/articles";
        final String title = null;
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        //when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody)
        );

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("addArticle : 아트클 추가할 때 title이 10자를 넘으면 실패한다")
    @Test
    public void addArticleSizeValidation() throws Exception{
        //given
        Faker faker = new Faker();
        final String url = "/api/articles";
        final String title = faker.lorem().characters(11);
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        //when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody)
        );

        //then
        resultActions.andExpect(status().isBadRequest());
    }

    @DisplayName("findArticle: 잘못된 HTTP 메서드로 아트클을 조회하려고 하면 조회에 실패한다,")
    @Test
    public void invalidHttpMethod() throws  Exception {
        //given
        final String url = "/api/articles/{id}";

        //when
        final ResultActions resultActions = mockMvc.perform(post(url, 1));

        //then
        resultActions
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value(ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @DisplayName("findArticle: 존재하지 않는 아티클을 조회하려고 하면 조회에 실패한다.")
    @Test
    public void findArticleInvalidArticle() throws Exception {
        //given
        final String url = "/api/articles/{id}";
        final long invalidId = 1;

        //when
        final ResultActions resultActions = mockMvc.perform(get(url, invalidId));

        //then
        resultActions
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.ARTICLE_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.code").value(ErrorCode.ARTICLE_NOT_FOUND.getCode()));

    }

    @DisplayName("addComment: 댓글 추가에 성공한다")
    @Test
    public void addComment() throws Exception {
        //given
        final String url = "/api/comments";

        Article savedArticle = createDefaultArticle();
        final Long articleId = savedArticle.getId();
        final String content = "content";
        final AddCommentRequest userRequest = new AddCommentRequest(articleId, content);
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("username");

        //when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .principal(principal)
                .content(requestBody)
        );

        //then
        resultActions.andExpect(status().isCreated());

        List<Comment> comments = commentRepository.findAll();

        assertThat(comments.size()).isEqualTo(1);
        assertThat(comments.get(0).getArticle().getId()).isEqualTo(articleId);
        assertThat(comments.get(0).getContent()).isEqualTo(content);

    }

    @DisplayName("deleteComment : 댓글 삭제에 성공한다")
    @Test
    void deleteComment() throws Exception{
        //given
        final String url = "/api/comments/{id}";
        Article savedArticle = createDefaultArticle();
        Comment comment = createDefaultComment(savedArticle);

        //when
        mockMvc.perform(delete(url, comment.getId())).andExpect(status().isOk());

        //then
        List<Comment> comments = commentRepository.findAll();

        assertThat(comments).isEmpty();
    }

    @DisplayName("updateComment : 댓글 수정에 성공한다")
    @Test
    void updateComment() throws Exception{
        //given
        final String url = "/api/comments/{id}";
        Article savedArticle = createDefaultArticle();
        Comment comment = createDefaultComment(savedArticle);

        String newContent = "newContent";
        UpdateCommentRequest request = new UpdateCommentRequest(newContent);

        //when
        ResultActions resultActions = mockMvc.perform(put(url, comment.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))
        );


        //then
        resultActions.andExpect(status().isOk());

        Comment updatedComment = commentRepository.findById(comment.getId()).get();

        assertThat(updatedComment.getContent()).isEqualTo(newContent);
    }

    private Article createDefaultArticle() {
        return blogRepository.save(Article.builder()
                .title("title")
                .author(user.getUsername())
                .content("content")
                .build());

    }

    private Comment createDefaultComment(Article article) {
        return commentRepository.save(Comment.builder()
                .article(article)
                .author(user.getUsername())
                .content("content")
                .build());

    }
}