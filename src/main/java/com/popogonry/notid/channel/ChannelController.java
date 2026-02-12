package com.popogonry.notid.channel;

import com.popogonry.notid.channel.dto.*;
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
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ChannelResponse> channelResponses = channelService.searchChannels(request, pageable);
        return ResponseEntity.ok(channelResponses);
    }

    @PatchMapping("/{channelId}")
    public ResponseEntity<Long> updateChannel(
            @PathVariable Long channelId,
            @RequestBody @Valid ChannelUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long updatedChannelId = channelService.updateChannel(channelId, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedChannelId);
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<Long> deleteChannel(
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long deletedChannelId = channelService.deleteChannel(channelId, userDetails.getUsername());
        return ResponseEntity.ok(deletedChannelId);
    }

    @PostMapping("/{channelId}/join")
    public ResponseEntity<Long> joinChannel(
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long joinedChannelId = channelService.joinChannel(channelId, userDetails.getUsername());
        return ResponseEntity.ok(joinedChannelId);
    }

    @PostMapping("/{channelId}/leave")
    public ResponseEntity<Long> leaveChannel(
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long leavedChannelId = channelService.leaveChannel(channelId, userDetails.getUsername());
        return ResponseEntity.ok(leavedChannelId);
    @DeleteMapping("/{channelId}/members/{targetUserId}")
    public ResponseEntity<Long> kickMember(
            @PathVariable Long channelId,
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long kickedUserId = channelService.kickMember(channelId, targetUserId, userDetails.getUsername());
        return ResponseEntity.ok(kickedUserId);
    }

    @GetMapping("/popular")
    public ResponseEntity<Page<ChannelResponse>> getChannelsOrderByMemberCount(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(channelService.getChannelsOrderByMemberCount(pageable));
    }

    @GetMapping
    public ResponseEntity<Page<ChannelResponse>> getChannels(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(channelService.getChannels(pageable));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ChannelResponse> getChannel(
            @PathVariable Long channelId
    ) {
        Channel channel = channelService.getChannel(channelId);
        return ResponseEntity.ok(ChannelResponse.from(channel));
    }

    @GetMapping("/{channelId}/members")
    public ResponseEntity<Page<ChannelMemberResponse>> getMembers(
            @PathVariable Long channelId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ChannelMemberResponse> members = channelService.getMembers(channelId, pageable);
        return ResponseEntity.ok(members);
    }

    @PatchMapping("/{channelId}/members/{targetMemberId}")
    public ResponseEntity<Long> updateMemberGrade(
            @PathVariable Long channelId,
            @PathVariable Long targetMemberId,
            @RequestBody @Valid MemberGradeUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long updatedMemberId = channelService.updateMemberGrade(channelId, targetMemberId, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedMemberId);
    }
}
