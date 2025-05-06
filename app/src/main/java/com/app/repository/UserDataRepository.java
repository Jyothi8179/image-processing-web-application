package com.app.repository;

import com.app.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDataRepository extends JpaRepository<UserData,Long> {
    UserData findByIp(@Param("ip") String ip);
}
