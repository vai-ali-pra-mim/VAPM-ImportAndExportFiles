package com.example.importapi.repositorios;
import com.example.importapi.dominios.Pedido;
import com.example.importapi.dominios.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    @Query(value = "select * from PEDIDO where post_id = :id",nativeQuery = true)
    Optional<Pedido> findByPostId(@Param("id")int id);
}
