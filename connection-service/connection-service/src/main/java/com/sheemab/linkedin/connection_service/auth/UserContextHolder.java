package com.sheemab.linkedin.connection_service.auth;

import org.springframework.stereotype.Component;

@Component
public class UserContextHolder {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    /*
    ThreadLocal is a Java class that allows you to store values specific to the current thread.
    Each thread accessing this variable gets its own, isolated copy.
     This is useful when dealing with user data in a multi-threaded environment like web applications.
    In this case, it's used to store the ID of the currently authenticated user for each thread.
     */

    //Getter
    public static Long getCurrentUserId(){
        return currentUserId.get();
    }

    //Setter
    static void setCurrentUserId(Long userId){
         currentUserId.set(userId);
    }

    //Remove
    static void clear(){
        currentUserId.remove();
    }


}
