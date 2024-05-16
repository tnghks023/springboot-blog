package me.kimsuhwan.springbootdeveloper.repository;

import me.kimsuhwan.springbootdeveloper.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Article, Long> {
}
