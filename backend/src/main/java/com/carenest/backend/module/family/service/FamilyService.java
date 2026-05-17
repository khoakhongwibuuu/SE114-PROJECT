package com.carenest.backend.module.family.service;

import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.JoinFamilyByCodeRequest;
import com.carenest.backend.module.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyInvitationResponse;
import com.carenest.backend.module.family.dto.response.FamilyJoinCodeResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;

import java.util.List;

public interface FamilyService {

    FamilyDetailResponse getMyFamily();

    FamilyResponse createFamily(CreateFamilyRequest request);

    FamilyDetailResponse getFamilyById(Long id);

    void inviteMember(Long familyId, InviteMemberRequest request);

    List<FamilyInvitationResponse> getReceivedInvitations();

    List<FamilyInvitationResponse> getSentInvitations();

    void handleInvitation(Long invitationId, UpdateInvitationRequest request);

    FamilyJoinCodeResponse getJoinCode();

    FamilyJoinCodeResponse rotateJoinCode();

    FamilyDetailResponse joinByCode(JoinFamilyByCodeRequest request);

    void updateMemberRole(Long familyId, Long memberId, UpdateRoleRequest request);
}
