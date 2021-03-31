package com.example.importapi.repositorios;


import com.example.importapi.dominios.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query(value = "select * from POST where entregador_id = :id",nativeQuery = true)
    List<Post> findByEntregadorId(@Param("id")int id);

}
