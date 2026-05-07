package com.carenest.backend.module.family.mapper;

import com.carenest.backend.module.auth.mapper.UserMapper;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyMemberResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.entity.FamilyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class}, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface FamilyMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    FamilyResponse toFamilyResponse(Family family);

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(target = "members", ignore = true) // Sẽ được map thủ công trong Service
    FamilyDetailResponse toFamilyDetailResponse(Family family);

    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    FamilyMemberResponse toFamilyMemberResponse(FamilyMember familyMember);
}
