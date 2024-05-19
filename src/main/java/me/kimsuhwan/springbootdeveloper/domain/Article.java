package me.kimsuhwan.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@EntityListeners(AuditingEntityListener.class) //엔티티의 생성 및 수정시간을 자동으로 감시하고 기록
@Entity
@Getter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "author", nullable = false)
    private String author;

    @Builder
    public Article(String author, String title, String content){
        this.author = author;
        this.title = title;
        this.content = content;
    }

    @CreatedDate //엔티티가 생성될 때 생성 시간 지정
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate //엔티티가 수정될 때 수정 시간 저장
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // cascade = CascadeType.REMOVE : 부모 엔티티(Article)가 변경될때 자식 엔티티(Comment)에 전파하기 위한 방법 중 삭제에 관련된값
    // 글 삭제되면 댓글 모두 삭제
    @OneToMany(mappedBy = "article", cascade = CascadeType.REMOVE)
    @OrderBy("createdAt DESC") // Order comments by creation date in ascending order
    private List<Comment> comments;


    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

}
