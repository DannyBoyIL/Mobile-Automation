package com.mobileAutomation.dataproviders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileAutomation.model.UsersPayload;
import com.mobileAutomation.model.UserData;
import org.testng.annotations.DataProvider;

import java.io.InputStream;
import java.util.List;

public class UserDataProvider {

    @DataProvider(name = "usersData", parallel = false)
    public static Object[][] getUsersData() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        InputStream is = UserDataProvider.class
                .getClassLoader()
                .getResourceAsStream("users-data.json");

        if (is == null) {
            throw new RuntimeException("users-data.json not found");
        }

        UsersPayload payload = mapper.readValue(is, UsersPayload.class);
        List<UserData> users = payload.getUsers();

        Object[][] data = new Object[users.size()][2];
        for (int i = 0; i < users.size(); i++) {
            data[i][0] = users.get(i).getUsername();
            data[i][1] = users.get(i).getPassword();
        }

        return data;
    }
}
