package brucehan.auth.infrastructure.kakao_client.dto;

import brucehan.auth.domain.OauthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
//    @Schema(title = "인가 코드", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String authCode,

//        @Schema(title = "OAuth 제공자", example = "KAKAO", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        OauthProvider provider

) {
}
