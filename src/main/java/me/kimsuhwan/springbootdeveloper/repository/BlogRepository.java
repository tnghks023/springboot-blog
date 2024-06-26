package me.kimsuhwan.springbootdeveloper.repository;

import me.kimsuhwan.springbootdeveloper.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Article, Long> {
    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Article> findAllByOrderByLikesDesc(Pageable pageable);

    Page<Article> findAllByOrderByViewsDesc(Pageable pageable);

}
