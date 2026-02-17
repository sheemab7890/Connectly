package com.sheemab.linkedin.connection_service.Repository;

import com.sheemab.linkedin.connection_service.Entities.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {

    Optional<Person> findByName(String name);
    boolean existsByUserId(Long userId);

    @Query("""
           MATCH (personA:Person {userId: $userId})-[:CONNECTED_TO]-(personB:Person)
           RETURN personB
       """)
    List<Person> getFirstDegreeConnections(@Param("userId") Long userId);


//    @Query("MATCH (p:Person)-[:CONNECTED_TO]-(connected) WHERE id(p) = $id RETURN connected")

    @Query("Match (p1:Person)-[r:REQUESTED_TO]->(p2:Person) " +
            "Where p1.userId = $senderId And p2.userId = $receiverId " +
            "Return count(r) > 0")
    boolean connectionRequestExists(Long senderId, Long receiverId);

    @Query("Match (p1:Person)-[r:CONNECTED_TO]->(p2:Person) " +
            "Where p1.userId = $senderId And p2.userId = $receiverId " +
            "Return count(r) > 0")
    boolean alreadyConnected(Long senderId, Long receiverId);

    @Query("Match (p1:Person),(p2:Person) " +
            "Where p1.userId = $senderId And p2.userId = $receiverId " +
            "Create (p1)-[r:REQUESTED_TO]->(p2)")   // requested to and connection request both are same
    void addConnectionRequest(Long senderId,Long receiverId);

    @Query("Match (p1:Person)-[r:REQUESTED_TO]->(p2:Person) " +
            "Where p1.userId = $senderId And p2.userId = $receiverId " +
            "Delete r " +
            "Create (p1)-[:CONNECTED_TO]->(p2)")
    void acceptConnectionRequest(Long senderId,Long receiverId);

    @Query("Match (p1:Person)-[r:REQUESTED_TO]->(p2:Person) " +
            "Where p1.userId = $senderId And p2.userId = $receiverId " +
            "Delete r")
    void rejectConnectionRequest(Long senderId,Long receiverId);


}
