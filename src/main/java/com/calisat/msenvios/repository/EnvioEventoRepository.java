package com.calisat.msenvios.repository;

import com.calisat.msenvios.model.EnvioEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnvioEventoRepository extends JpaRepository<EnvioEvento, Long> {

    List<EnvioEvento> findByEnvioIdOrderByFechaEventoDesc(UUID envioId);
}
