package brucehan.member.domain.entity;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "profile_image")
    private String profileImage;

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

    public void changeProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Collection<GrantedAuthority> initSimpleGrantedAuthorities() {
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
        result.put("provider", provider);
        result.put("profile", profileImage);
        result.put("role", role);
        return result;
    }

    public void updateProfileImageIfAbsent(String profileImage) {
        if (this.profileImage == null || this.profileImage.isBlank()) {
            changeProfileImage(profileImage);
        }
    }

    public static MemberEntity toMemberEntityBy(OAuth2User oAuth2User) {
        Long id = oAuth2User.getAttribute("id");
        String nickname = oAuth2User.getAttribute("nickname");
        String email = oAuth2User.getAttribute("email");
        String provider = oAuth2User.getAttribute("provider");
        String profileImage = oAuth2User.getAttribute("profile_image");
        Set<String> roles = oAuth2User.getAttribute("roles");
        return MemberEntity.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .provider(provider)
                .role(roles)
                .profileImage(profileImage)
                .build();
    }
}



