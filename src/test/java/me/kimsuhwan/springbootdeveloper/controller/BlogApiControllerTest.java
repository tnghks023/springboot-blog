package me.kimsuhwan.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.kimsuhwan.springbootdeveloper.domain.Article;
import me.kimsuhwan.springbootdeveloper.dto.AddArticleRequest;
import me.kimsuhwan.springbootdeveloper.dto.UpdateArticleRequest;
import me.kimsuhwan.springbootdeveloper.repository.BlogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.as;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.context.WebApplicationContext;

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

    @BeforeEach
    public void mockMvcSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        blogRepository.deleteAll();;
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

        //when
        ResultActions result = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(requestBody));

        //then
        result.andExpect(status().isCreated());
        List<Article> articles = blogRepository.findAll();
        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo(title);
        assertThat(articles.get(0).getContent()).isEqualTo(content);
    }

    @DisplayName("findAllArticles: 블로그 글 목록 조회에 성공한다.")
    @Test
    public void findAllArticles() throws Exception{
        //given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";

        blogRepository.save(Article.builder().title(title).content(content).build());

        //when
        final ResultActions result = mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(content))
                .andExpect(jsonPath("$[0].title").value(title));
    }

    @DisplayName("findArticle: 블로그 글 조회에 성공한다.")
    @Test
    public void findArticle() throws Exception{
        //given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article saveArticle = blogRepository.save(Article.builder().title(title).content(content).build());

        //when
        final ResultActions result = mockMvc.perform(get(url, saveArticle.getId()));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @DisplayName("deleteArticle: 블로그 글 삭제에 성공한다.")
    @Test
    public void deleteArticle() throws Exception{
        //given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article saveArticle = blogRepository.save(Article.builder().title(title).content(content).build());

        //when
        mockMvc.perform(delete(url, saveArticle.getId())).andExpect(status().isOk());

        //then
        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle: 블로그 글 수정에 성공한다.")
    @Test
    public void updateArticle() throws Exception {
        //given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article saveArticle = blogRepository.save(Article.builder().title(title).content(content).build());

        final String newTitle = "new Title";
        final String newContent = "new content";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        //when
        ResultActions result = mockMvc.perform(put(url, saveArticle.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk());

        Article article = blogRepository.findById(saveArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }

}