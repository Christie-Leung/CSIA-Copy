package com.example.CSIA.converter;

import javax.persistence.AttributeConverter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class RoleConverter implements AttributeConverter<String, Integer> {

    /**
     * This method converts a role name to a role ID when inserted into the user database
     *
     * @param roleName String of user's role
     * @return user role ID
     */
    @Override
    public Integer convertToDatabaseColumn(String roleName) {
        Hashtable<String, Integer> userRole = getUserRoleHashTable();
        return userRole.get(roleName);
    }

    /**
     * This method converts a role ID to a role name when pulling information from the user database
     *
     * @param roleId user's role ID
     * @return user role name
     */
    @Override
    public String convertToEntityAttribute(Integer roleId) {
        Optional<Map.Entry<String, Integer>> userRole = getUserRoleHashTable().entrySet().stream()
                .filter(stringIntegerEntry -> Objects.equals(stringIntegerEntry.getValue(), roleId)).findFirst();
        return userRole.map(Map.Entry::getKey).orElse(null);
    }

    /**
     * This method stores the HashTable for the conversion of user role name to role ID
     *
     * @return HashTable of role conversion
     */
    public static Hashtable<String, Integer> getUserRoleHashTable() {
        Hashtable<String, Integer> userRole = new Hashtable<>();
        userRole.put("PATIENT", 1);
        userRole.put("NURSE", 2);
        userRole.put("DOCTOR", 3);
        return userRole;
    }
}
