package com.carenest.backend.module.auth.mapper;

import com.carenest.backend.module.auth.dto.request.RegisterRequest;
import com.carenest.backend.module.auth.dto.response.UserInfoResponse;
import com.carenest.backend.module.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(source = "phoneNumber", target = "phone")
    User toEntity(RegisterRequest request);

    UserInfoResponse toUserInfoResponse(User user);
}
