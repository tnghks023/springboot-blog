package me.kimsuhwan.springbootdeveloper.repository;

import me.kimsuhwan.springbootdeveloper.domain.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<UserLike, Long> {
    Optional<UserLike> findByUserIdAndArticleId(Long userId, Long articleId);
}
