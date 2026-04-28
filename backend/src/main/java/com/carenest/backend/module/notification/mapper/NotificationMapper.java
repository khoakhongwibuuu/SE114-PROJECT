package com.carenest.backend.module.notification.mapper;

import com.carenest.backend.module.notification.dto.response.NotificationResponse;
import com.carenest.backend.module.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    NotificationResponse toResponse(Notification notification);
}
