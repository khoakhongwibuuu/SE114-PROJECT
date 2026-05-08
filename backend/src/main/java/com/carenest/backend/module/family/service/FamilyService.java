package com.carenest.backend.module.family.service;

import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;

public interface FamilyService {

    FamilyDetailResponse getMyFamily();

    FamilyResponse createFamily(CreateFamilyRequest request);


    FamilyDetailResponse getFamilyById(Long id);

    void inviteMember(Long familyId, InviteMemberRequest request);

    void handleInvitation(Long invitationId, UpdateInvitationRequest request);

    void updateMemberRole(Long familyId, Long memberId, UpdateRoleRequest request);
}
