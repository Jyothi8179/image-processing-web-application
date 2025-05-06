package com.app.service;

import com.app.entity.UserData;
import com.app.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDataService {

    @Autowired UserDataRepository userDataRespository;

    public UserData save(String ip){

        UserData user = getUserData(ip);
        if(user==null){
            user = new UserData();
            user.setIp(ip);
            user.setCount(1);
        }
        else{
            user.setCount(user.getCount()+1);
        }
        return userDataRespository.save(user);
    }

    public UserData getUserData(String ip){
        return userDataRespository.findByIp(ip);
    }

    public void deleteAll(){
        userDataRespository.deleteAll();
    }
}
