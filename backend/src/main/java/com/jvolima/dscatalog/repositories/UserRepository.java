package com.jvolima.dscatalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jvolima.dscatalog.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}