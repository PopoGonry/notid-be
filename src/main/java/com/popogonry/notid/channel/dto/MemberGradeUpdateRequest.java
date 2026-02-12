package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channeluser.ChannelGrade;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberGradeUpdateRequest {

    @NotNull(message = "채널 권한은 필수입니다.")
    private ChannelGrade channelGrade;

}
