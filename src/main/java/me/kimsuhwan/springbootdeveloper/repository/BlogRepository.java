package me.kimsuhwan.springbootdeveloper.repository;

import me.kimsuhwan.springbootdeveloper.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByOrderByCreatedAtDesc();

}
