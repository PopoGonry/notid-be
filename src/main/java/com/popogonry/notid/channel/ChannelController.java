package com.popogonry.notid.channel;

import com.popogonry.notid.channel.dto.ChannelCreateRequest;
import com.popogonry.notid.channel.dto.ChannelResponse;
import com.popogonry.notid.channel.dto.ChannelSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<Long> createChannel(
            @RequestBody @Valid ChannelCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        Long channelId = channelService.createChannel(request, userDetails.getUsername());
        return ResponseEntity.ok(channelId);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ChannelResponse>> searchChannels(
            @ModelAttribute @Valid ChannelSearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, userDetails.getUsername(), pageable);
        return ResponseEntity.ok(channelResponses);
    }
}
