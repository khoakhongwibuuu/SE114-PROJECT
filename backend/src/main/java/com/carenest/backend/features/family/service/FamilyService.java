package com.carenest.backend.features.family.service;

import com.carenest.backend.features.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.features.family.dto.request.InviteMemberRequest;
import com.carenest.backend.features.family.dto.request.JoinFamilyByCodeRequest;
import com.carenest.backend.features.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.features.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.features.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.features.family.dto.response.FamilyInvitationResponse;
import com.carenest.backend.features.family.dto.response.FamilyJoinCodeResponse;
import com.carenest.backend.features.family.dto.response.FamilyResponse;
import com.carenest.backend.features.family.dto.response.FamilySummaryResponse;
import com.carenest.backend.features.family.enums.FamilyRole;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FamilyService {

    FamilyDetailResponse getMyFamily();

    /** Returns all families the current user belongs to (multi-family support). */
    List<FamilySummaryResponse> getMyFamilies();

    FamilyResponse createFamily(CreateFamilyRequest request);

    FamilyDetailResponse getFamilyById(Long id);

    void inviteMember(Long familyId, InviteMemberRequest request);

    List<FamilyInvitationResponse> getReceivedInvitations();

    List<FamilyInvitationResponse> getSentInvitations();

    void handleInvitation(Long invitationId, UpdateInvitationRequest request);

    FamilyJoinCodeResponse getJoinCode();

    FamilyJoinCodeResponse rotateJoinCode();

    FamilyDetailResponse joinByCode(JoinFamilyByCodeRequest request);

    FamilyDetailResponse joinByQr(MultipartFile image, FamilyRole role);

    void updateMemberRole(Long familyId, Long memberId, UpdateRoleRequest request);
}
