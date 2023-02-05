package com.example.weblog.util;

import com.example.weblog.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserHolder {
    private static final ThreadLocal<UserDTO> local = new ThreadLocal<>();

    public static UserDTO getUser() {
        return local.get();
    }

    public static void setUser(UserDTO user) {
        local.set(user);
    }

    public static void getLocal(){
        System.out.println(local + ", " + local.get());
    }

}
