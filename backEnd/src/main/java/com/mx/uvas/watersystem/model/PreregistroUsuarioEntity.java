package com.mx.uvas.watersystem.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Gente que vive en una casa pero todavía NO es un usuario de agua formal
// (no tiene cuota, no se le cobra, no tiene número de servicio). Antes de
// esta tabla se usaba el truco de darlos de alta como WaterUserEntity con
// noUsuario > 10,000 -- funcionaba pero mezclaba gente "real" (que se
// cobra) con gente que solo se está anotando para no perder el dato,
// dificultando saber por qué alguien sigue sin convertirse en usuario.
//
// El "convertir a usuario" NO se automatiza aquí: cuando se decide dar de
// alta formalmente, se sigue usando el flujo normal de "Nuevo usuario"
// (incluyendo "copiar usuario" si ya hay otro en la misma casa), y luego se
// marca este registro como Convertido, enlazándolo al usuario resultante.
// Automatizar la creación completa del usuario (persona, dirección, cuota,
// etc.) desde datos tan mínimos como los de un preregistro sería más
// riesgoso que útil.
@Entity
@Table(name = "agua_preregistro")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PreregistroUsuarioEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Estatus posibles (no catálogo -- son estados fijos del flujo, no
    // opciones libres que la usuaria vaya a querer editar).
    public static final int ESTATUS_PENDIENTE = 1;
    public static final int ESTATUS_CONVERTIDO = 2;
    public static final int ESTATUS_DESCARTADO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer preregistroId;

    private String nombre;
    private String telefono;
    private String observaciones;

    private LocalDate fechaRegistro;
    private Integer estatus;

    // Si se descarta (no se va a convertir en usuario) o queda pendiente
    // por mucho tiempo, aquí se anota por qué -- para no perder el
    // contexto meses después.
    private String motivoPendiente;

    // Se llena manualmente cuando se decide convertir: el aguaUsuarioId del
    // usuario real que se dio de alta a partir de esta persona.
    private Integer aguaUsuarioIdConvertido;

    private Integer userIdAdd;
    private LocalDateTime dateAdd;
    private Integer userIdUpdate;
    private LocalDateTime dateUpdate;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "casa_id")
    private WaterHouseEntity waterHouse;

    // Caso: un negocio que nunca va a tener su propio usuario de agua (ej.
    // una tiendita atendida por el mismo usuario que ya vive en la casa) --
    // igual se quiere contar en el censo de negocios, sin necesidad de
    // convertirlo en un WaterUserEntity aparte.
    private Boolean esNegocio;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "giro_negocio_id", nullable = true)
    private CatalogOptionsEntity giroNegocio;

    // Catálogo MOTIVO_NOUSUARIO -- por qué esta persona/negocio no se
    // considera (o no se va a convertir en) un usuario de agua formal.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "motivo_no_usuario_id", nullable = true)
    private CatalogOptionsEntity motivoNoUsuario;

    // Deuda aproximada -- solo es una estimación anotada a mano mientras la
    // persona no es usuario formal (no hay cargos reales que sumar todavía).
    // Se desglosa en aportaciones vs multas/recargos porque suelen tener
    // reglas distintas de condonación.
    private java.math.BigDecimal deudaAportaciones;
    private java.math.BigDecimal deudaMultasRecargos;
    private String deudaObservaciones;

    // Grupo al que probablemente se va a unir esta persona cuando se
    // convierta en usuario formal (ej. ya se sabe que va a compartir cuenta
    // con la familia de la casa de al lado) -- opcional, se puede asignar o
    // cambiar en cualquier momento mientras sigue en preregistro.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "grupo_id", nullable = true)
    private WaterGroupEntity waterGroup;

}
