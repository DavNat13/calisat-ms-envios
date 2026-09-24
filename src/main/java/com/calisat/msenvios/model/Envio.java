package com.calisat.msenvios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "envio", indexes = {
        @Index(name = "idx_envio_orden_id", columnList = "orden_id"),
        @Index(name = "idx_envio_usuario_sub", columnList = "usuario_sub")
})
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "ordenId es obligatorio")
    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Size(max = 128, message = "usuarioSub no puede superar 128 caracteres")
    @Column(name = "usuario_sub", length = 128)
    private String usuarioSub;

    @NotBlank(message = "numeroGuia es obligatorio")
    @Size(max = 32, message = "numeroGuia no puede superar 32 caracteres")
    @Column(name = "numero_guia", nullable = false, unique = true, length = 32)
    private String numeroGuia;

    @Size(max = 120, message = "transportista no puede superar 120 caracteres")
    @Column(name = "transportista", length = 120)
    private String transportista;

    @NotNull(message = "estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 32)
    private EstadoEnvio estado = EstadoEnvio.CREADO;

    @Size(max = 200, message = "direccionCalle no puede superar 200 caracteres")
    @Column(name = "direccion_calle", length = 200)
    private String direccionCalle;

    @Size(max = 120, message = "direccionCiudad no puede superar 120 caracteres")
    @Column(name = "direccion_ciudad", length = 120)
    private String direccionCiudad;

    @Size(max = 64, message = "direccionPais no puede superar 64 caracteres")
    @Column(name = "direccion_pais", length = 64)
    private String direccionPais;

    @Size(max = 16, message = "direccionCodigoPostal no puede superar 16 caracteres")
    @Column(name = "direccion_codigo_postal", length = 16)
    private String direccionCodigoPostal;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "fecha_entrega_estimada")
    private LocalDateTime fechaEntregaEstimada;

    @Column(name = "fecha_entrega_real")
    private LocalDateTime fechaEntregaReal;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        fechaActualizacion = ahora;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Envio() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrdenId() { return ordenId; }
    public void setOrdenId(UUID ordenId) { this.ordenId = ordenId; }

    public String getUsuarioSub() { return usuarioSub; }
    public void setUsuarioSub(String usuarioSub) { this.usuarioSub = usuarioSub; }

    public String getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }

    public String getTransportista() { return transportista; }
    public void setTransportista(String transportista) { this.transportista = transportista; }

    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }

    public String getDireccionCalle() { return direccionCalle; }
    public void setDireccionCalle(String direccionCalle) { this.direccionCalle = direccionCalle; }

    public String getDireccionCiudad() { return direccionCiudad; }
    public void setDireccionCiudad(String direccionCiudad) { this.direccionCiudad = direccionCiudad; }

    public String getDireccionPais() { return direccionPais; }
    public void setDireccionPais(String direccionPais) { this.direccionPais = direccionPais; }

    public String getDireccionCodigoPostal() { return direccionCodigoPostal; }
    public void setDireccionCodigoPostal(String direccionCodigoPostal) { this.direccionCodigoPostal = direccionCodigoPostal; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(LocalDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }

    public LocalDateTime getFechaEntregaEstimada() { return fechaEntregaEstimada; }
    public void setFechaEntregaEstimada(LocalDateTime fechaEntregaEstimada) { this.fechaEntregaEstimada = fechaEntregaEstimada; }

    public LocalDateTime getFechaEntregaReal() { return fechaEntregaReal; }
    public void setFechaEntregaReal(LocalDateTime fechaEntregaReal) { this.fechaEntregaReal = fechaEntregaReal; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
