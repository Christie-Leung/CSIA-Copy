package com.example.CSIA.converter;

import javax.persistence.AttributeConverter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RoleConverter implements AttributeConverter<String, Integer> {

    @Override
    public Integer convertToDatabaseColumn(String roleName) {
        Hashtable<String, Integer> userRole = getUserRoleHashTable();
        return userRole.get(roleName);
    }

    @Override
    public String convertToEntityAttribute(Integer roleId) {
        Optional<Map.Entry<String, Integer>> userRole = getUserRoleHashTable().entrySet().stream()
                .filter(stringIntegerEntry -> Objects.equals(stringIntegerEntry.getValue(), roleId)).findFirst();
        return userRole.map(Map.Entry::getKey).orElse(null);
    }

    public static Hashtable<String, Integer> getUserRoleHashTable() {
        Hashtable<String, Integer> userRole = new Hashtable<>();
        userRole.put("PATIENT", 1);
        userRole.put("NURSE", 2);
        userRole.put("DOCTOR", 3);
        return userRole;
    }
}
