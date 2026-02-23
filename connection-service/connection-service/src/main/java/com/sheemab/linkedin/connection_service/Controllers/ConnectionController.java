package com.sheemab.linkedin.connection_service.Controllers;

import com.sheemab.linkedin.connection_service.Entities.Person;
import com.sheemab.linkedin.connection_service.Services.ConnectionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/core")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    @GetMapping("/first-degree")
    public ResponseEntity<List<Person>> getFirstDegreeConnections(){
        return ResponseEntity.ok(connectionService.getFirstDegreeConnections());
    }

    //Internal use only
    @GetMapping("/internal/{userId}/first-degree")
    public ResponseEntity<List<Person>>  getConnectionsByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(connectionService.getConnectionsByUserId(userId));
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<Boolean> sendConnectionRequest(@PathVariable Long userId){
        return ResponseEntity.ok(connectionService.sendConnectionRequest(userId));
    }

    @PostMapping("/accept/{userId}")
    public ResponseEntity<Boolean> acceptConnectionRequest(@PathVariable Long userId){
        return ResponseEntity.ok(connectionService.acceptConnectionRequest(userId));
    }

    @PostMapping("/reject/{userId}")
    public ResponseEntity<Boolean> rejectConnectionRequest(@PathVariable Long userId){
        return ResponseEntity.ok(connectionService.rejectConnectionRequest(userId));
    }
}
