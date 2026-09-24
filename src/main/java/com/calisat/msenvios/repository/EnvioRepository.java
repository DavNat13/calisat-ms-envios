package com.calisat.msenvios.repository;

import com.calisat.msenvios.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, UUID> {

    List<Envio> findByOrdenId(UUID ordenId);

    List<Envio> findByUsuarioSub(String usuarioSub);

    Optional<Envio> findByNumeroGuia(String numeroGuia);

    boolean existsByNumeroGuia(String numeroGuia);

    boolean existsByOrdenId(UUID ordenId);
}
