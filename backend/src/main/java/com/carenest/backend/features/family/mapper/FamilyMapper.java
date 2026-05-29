package com.carenest.backend.features.family.mapper;

import com.carenest.backend.features.auth.mapper.UserMapper;
import com.carenest.backend.features.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.features.family.dto.response.FamilyMemberResponse;
import com.carenest.backend.features.family.dto.response.FamilyResponse;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserMapper.class}, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface FamilyMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    FamilyResponse toFamilyResponse(Family family);

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(target = "members", ignore = true) // Sáº½ Ä‘Æ°á»£c map thá»§ cÃ´ng trong Service
    FamilyDetailResponse toFamilyDetailResponse(Family family);

    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    FamilyMemberResponse toFamilyMemberResponse(FamilyMember familyMember);
}
