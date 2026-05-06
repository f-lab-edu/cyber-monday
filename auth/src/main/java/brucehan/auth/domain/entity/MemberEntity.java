package brucehan.auth.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "cybermonday", name = "members")
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id; // TODO 이름 바꾸기. pk라는 이름은 잘 안 씀

    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long memberId; // TODO memberId가 Long이 아닌 uuid로써 활용되는 방법 강구

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "savings")
    private Long savings;

    @Column(name = "role")
    private String role;

    // BaseEntity에 두기엔 가끔 나오는 컬럼이라 필요한 Entity에만 만들기로 함
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MemberEntity(Long id, String nickname, String email) {
        this.memberId = id;
        this.nickname = nickname;
        this.email = email;
    }
}



