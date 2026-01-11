package com.popogonry.notid.channel.dto;

import com.popogonry.notid.channel.SearchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelSearchRequest {

    @NotNull(message = "검색 조건은 필수입니다.")
    private SearchType searchType;

    @NotBlank(message = "검색어는 필수입니다.")
    private String keyword;
}
