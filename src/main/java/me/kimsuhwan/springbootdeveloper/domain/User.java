package me.kimsuhwan.springbootdeveloper.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", unique = true)
    private String nickname;

    @Column(name = "provider", nullable = false)
    private String provider;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserLike> userLikes;

    public boolean hasLikedArticle(Article article) {
        return userLikes.stream().anyMatch(userLike -> userLike.getArticle().equals(article));
    }

    @Builder
    public User(String email, String password, String auth, String nickname, String provider) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.provider = provider;
    }

    public User update(String nickname) {
        this.nickname = nickname;

        return this;
    }


    //권한 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    //사용자의 패스워드 반환
    @Override
    public String getPassword() {
        return password;
    }

    //사용자의 id를 반환(고유한 값)
    @Override
    public String getUsername() {
        return email;
    }

    //계정 만료 여부 반환
    @Override
    public boolean isAccountNonExpired() {
        // 만료되었는지 확인하는 로직
        return true; // true -> 만료되지 않음
    }

    //계정 잠금 여부 반환
    @Override
    public boolean isAccountNonLocked() {
        //계정 잠금되었는지 확인하는 로직
        return true; // true -> 잠금되지 않았음
    }

    //패스워드의 만료 여부 반환
    @Override
    public boolean isCredentialsNonExpired() {
        // 패스워드가 만료되었는지 확인하는 로직
        return true; // true -> 만료되지않음
    }

    // 계정 사용 가능 여부 반환
    @Override
    public boolean isEnabled() {
        //계정이 사용가능한지 확인하는 로직
        return true;
    }
}
