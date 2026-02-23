package com.sheemab.linkedin.notification_service.client;

import com.sheemab.linkedin.notification_service.Dto.PersonDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "connection-service" , path = "/connections/core")
public interface ConnectionsClient {


    @GetMapping("/internal/{userId}/first-degree")
    List<PersonDto> getConnectionsByUserId(
            @PathVariable("userId") Long userId);

}
