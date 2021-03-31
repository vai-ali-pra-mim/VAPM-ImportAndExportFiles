package com.example.importapi.repositorios;

import com.example.importapi.dominios.Cartao;
import com.example.importapi.visoes.CartaoVisao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartaoRepository extends JpaRepository<Cartao, Integer> {

    @Query("select new com.example.importapi.visoes.CartaoVisao(c.idCartao, c.nomeTitular, c.numeroCartao, c.tipo, c.bandeira, c.CVV, c.CPF, c.dataValidade) from Cartao c")
    List<CartaoVisao> findAllSimples();

    @Query("select new com.example.importapi.visoes.CartaoVisao(c.idCartao, c.nomeTitular, c.numeroCartao, c.tipo, c.bandeira, c.CVV, c.CPF, c.dataValidade) from Cartao c where id_cartao = :id")
    CartaoVisao findByIdCartaoVisao(@Param("id")int id);
}
