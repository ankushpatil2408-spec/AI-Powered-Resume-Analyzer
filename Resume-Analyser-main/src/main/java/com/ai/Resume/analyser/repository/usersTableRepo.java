package com.ai.Resume.analyser.repository;


import com.ai.Resume.analyser.model.usersTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface usersTableRepo extends JpaRepository<usersTable,String> {
   Optional<usersTable> findByEmail(String email);
}
