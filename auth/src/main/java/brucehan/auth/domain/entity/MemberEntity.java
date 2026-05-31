package brucehan.auth.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table( name = "members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "role")
    private Set<String> role = new HashSet<>();

    @Column(name = "provider")
    private String provider;

    @Column(name = "subject")
    private String subject;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MemberEntity(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public void changeProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public Collection<GrantedAuthority> getSimpleGrantedAuthorities() {
        role = new HashSet<>();
        role.add("ROLE_USER");
        return role.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public Map<String, Object> toAttributeMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("nickname", nickname);
        result.put("email", email);
        result.put("profile_url", profileUrl);
        result.put("role", role);
        return result;
    }

    public void updateProfileUrlIfAbsent(String profileUrl) {
        if (this.profileUrl == null || this.profileUrl.isBlank()) {
            changeProfileUrl(profileUrl);
        }
    }

    public MemberEntity(Long id, String nickname, String email, String provider, Set<String> role, String profileUrl) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.role = role;
        this.profileUrl = profileUrl;
    }

    public static MemberEntity toMemberEntityBy(OAuth2User oAuth2User) {
        Long id = oAuth2User.getAttribute("id");
        String nickname = oAuth2User.getAttribute("nickname");
        String email = oAuth2User.getAttribute("email");
        String provider = oAuth2User.getAttribute("provider");
        String profileUrl = oAuth2User.getAttribute("profileUrl");
        Set<String> roles = oAuth2User.getAttribute("roles");
        return new MemberEntity(id, nickname, email, provider, roles, profileUrl);
    }
}



