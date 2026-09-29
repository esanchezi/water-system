package com.mx.uvas.watersystem.services.impl;

import com.lowagie.text.DocumentException;
import com.mx.uvas.watersystem.dto.AdeudoLuzUsuarioDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoAtencionRequestDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoEntregaRequestDto;
import com.mx.uvas.watersystem.dto.AvisoAdeudoGenerarRequestDto;
import com.mx.uvas.watersystem.dto.UsuarioNoRegistradoDto;
import com.mx.uvas.watersystem.dto.UsuarioManualAdeudoDto;
import com.mx.uvas.watersystem.mapping.AvisoAdeudoMapper;
import com.mx.uvas.watersystem.model.AvisoAdeudoEntity;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterAgreementEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterReceiptEntity;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.IAvisoAdeudoRepository;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IWaterAgreementRepository;
import com.mx.uvas.watersystem.repositories.IWaterReceiptRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserChargeRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.response.AdeudoLuzUsuarioRestResponse;
import com.mx.uvas.watersystem.response.AvisoAdeudoRestResponse;
import com.mx.uvas.watersystem.services.IValorGeneralService;
import com.mx.uvas.watersystem.utils.Constants;
import com.mx.uvas.watersystem.utils.CurrentUserService;
import com.mx.uvas.watersystem.utils.ResponseHandler;
import com.mx.uvas.watersystem.utils.ValorGeneralClave;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Orquesta el flujo completo de "Cartas de adeudo": lista de candidatos
// (usuarios con adeudo de Luz pendiente, ver AdeudoLuzService), y
// generación del PDF con folio consecutivo asignado + registro histórico
// (ver AvisoAdeudoEntity). No expone nada por sí solo vía REST -- eso lo
// hace AvisoAdeudoController.
@Service
@Slf4j
@AllArgsConstructor
public class AvisoAdeudoService {

    private final AdeudoLuzService adeudoLuzService;
    private final AvisoAdeudoPdfService avisoAdeudoPdfService;
    private final IAvisoAdeudoRepository avisoAdeudoRepository;
    private final IWaterUserRepository waterUserRepository;
    private final IWaterReceiptRepository waterReceiptRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IWaterUserChargeRepository waterUserChargeRepository;
    private final IWaterAgreementRepository waterAgreementRepository;
    private final IValorGeneralService valorGeneralService;
    private final AvisoAdeudoMapper avisoAdeudoMapper;
    private final CurrentUserService currentUserService;

    // Catálogo CONCEPTO_CARGO_EXTRA (pantalla de Catálogos) y el nombre
    // exacto de la opción "Aviso" dentro de él -- ver crearCargoAviso().
    private static final String CATALOGO_CARGO_EXTRA_CLAVE = "CONCEPTO_CARGO_EXTRA";
    private static final String CONCEPTO_AVISO_NOMBRE = "Aviso";
    // Cooperación extraordinaria de mantenimiento de cajón (Art. 10) -- a
    // diferencia del Aviso, no depende de que se genere una carta de un
    // tipo u otro: es $100 por cada uno de estos años que el usuario deba,
    // una sola vez por año (ver crearCargoMantenimiento()). Agregar más
    // años aquí si el Comité decide cobrar otros períodos más adelante.
    private static final String CONCEPTO_MANTENIMIENTO_NOMBRE = "Mantenimiento de cajón";
    private static final List<Integer> ANIOS_MANTENIMIENTO = List.of(2024, 2025);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // NumberFormat no es thread-safe -- se crea una instancia nueva en cada
    // llamada a generar() en vez de un campo estático compartido.

    // Cuota fija anual para personas SIN usuario en el sistema (censo en
    // proceso). A diferencia de AdeudoLuzService, aquí no hay recibos que
    // consultar -- se asume que deben desde que existe el Comité, con la
    // cuota vigente de cada año (dato dado directamente por Ely, no viene
    // de fee_amount). Actualizar este mapa cuando cambie la cuota anual.
    private static final Map<Integer, Double> CUOTAS_FIJAS_NO_REGISTRADO = new TreeMap<>(Map.of(
            2021, 720d,
            2022, 800d,
            2023, 1000d,
            2024, 1100d,
            2025, 1100d,
            2026, 1200d
    ));

    @Transactional(readOnly = true)
    public ResponseEntity<AdeudoLuzUsuarioRestResponse> candidatos() {
        AdeudoLuzUsuarioRestResponse response = new AdeudoLuzUsuarioRestResponse();
        try {
            List<AdeudoLuzUsuarioDto> lista = adeudoLuzService.calcularParaTodos();
            enriquecerConUltimoAviso(lista);
            response.setData(lista);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Candidatos encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular los candidatos a carta de adeudo", e);
        }
    }

    // Solo los candidatos de una calle -- para la pantalla, que ahora pide
    // elegir zona y calle primero en vez de calcular el adeudo de TODOS los
    // usuarios activos de una sola vez (lento).
    @Transactional(readOnly = true)
    public ResponseEntity<AdeudoLuzUsuarioRestResponse> candidatosPorCalle(Integer calleId) {
        AdeudoLuzUsuarioRestResponse response = new AdeudoLuzUsuarioRestResponse();
        try {
            List<AdeudoLuzUsuarioDto> lista = adeudoLuzService.calcularParaCalle(calleId);
            enriquecerConUltimoAviso(lista);
            response.setData(lista);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Candidatos encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular los candidatos a carta de adeudo", e);
        }
    }

    // Para el botón "Generar Segundo aviso" directo desde una fila del
    // historial (un Primer aviso ya entregado) -- NUNCA se confía en lo
    // que diga esa fila del historial (es solo un snapshot de cuando se
    // generó esa carta); aquí se recalcula todo de cero, igual que antes
    // de generar cartas de verdad, para saber si a estas alturas
    // realmente le toca el Segundo aviso (pudo haber pagado mientras
    // tanto, ya tener un aviso más reciente, o haber entrado en convenio).
    // Regresa una lista de 0 o 1 elemento (mismo contrato que
    // candidatos()/candidatosPorCalle(), para reusar el mismo DTO/response
    // y los mismos campos requiereSegundoAviso/enConvenioVigente que ya
    // entiende el frontend).
    @Transactional(readOnly = true)
    public ResponseEntity<AdeudoLuzUsuarioRestResponse> candidatoUnico(Integer aguaUsuarioId) {
        AdeudoLuzUsuarioRestResponse response = new AdeudoLuzUsuarioRestResponse();
        try {
            List<AdeudoLuzUsuarioDto> lista = adeudoLuzService.calcularParaUsuarios(List.of(aguaUsuarioId));
            enriquecerConUltimoAviso(lista);
            response.setData(lista);
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Estado calculado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al calcular el estado del usuario", e);
        }
    }

    // Control de Primer/Segundo aviso: para cada candidato, busca su aviso
    // ACTIVO más reciente (si tiene) y marca si ya le toca el Segundo aviso
    // (su último aviso fue Primero y ya se entregó, pero sigue debiendo --
    // por eso sigue en la lista de candidatos). Una sola consulta por lote,
    // no una por usuario.
    private void enriquecerConUltimoAviso(List<AdeudoLuzUsuarioDto> lista) {
        if (lista.isEmpty()) {
            return;
        }
        List<Integer> aguaUsuarioIds = lista.stream().map(AdeudoLuzUsuarioDto::getAguaUsuarioId).toList();
        List<AvisoAdeudoEntity> avisos = avisoAdeudoRepository
                .findByWaterUser_AguaUsuarioIdInAndEstatusOrderByFolioNotificacionDesc(aguaUsuarioIds, 1);

        Map<Integer, AvisoAdeudoEntity> ultimoPorUsuario = new HashMap<>();
        for (AvisoAdeudoEntity aviso : avisos) {
            // La lista ya viene ordenada por folio (consecutivo global)
            // descendente, así que la primera vez que aparece cada usuario
            // es, precisamente, su aviso más reciente.
            ultimoPorUsuario.putIfAbsent(aviso.getWaterUser().getAguaUsuarioId(), aviso);
        }

        // Convenios activos con fecha comprometida de pago aún no vencida --
        // mientras el usuario esté dentro de ese plazo, se pausa el Segundo
        // aviso (ya vino a hacer un arreglo con el comité, no se le debe
        // empujar el siguiente aviso mientras cumple lo acordado). Si el
        // plazo pasa sin que haya pagado, deja de aparecer aquí (la consulta
        // ya filtra fechaCompromisoPago >= hoy) y vuelve a requerir el
        // Segundo aviso normalmente.
        Map<Integer, WaterAgreementEntity> convenioVigentePorUsuario = new HashMap<>();
        List<WaterAgreementEntity> convenios = waterAgreementRepository
                .findVigentesConCompromisoPorUsuarios(aguaUsuarioIds, LocalDate.now());
        for (WaterAgreementEntity convenio : convenios) {
            // Ya viene ordenado por fecha compromiso ascendente -- si por
            // algún motivo hay más de uno vigente, se queda con el más
            // próximo a vencer.
            convenioVigentePorUsuario.putIfAbsent(convenio.getWaterUser().getAguaUsuarioId(), convenio);
        }

        for (AdeudoLuzUsuarioDto dto : lista) {
            WaterAgreementEntity convenioVigente = convenioVigentePorUsuario.get(dto.getAguaUsuarioId());
            if (convenioVigente != null) {
                dto.setEnConvenioVigente(true);
                dto.setFechaCompromisoConvenio(convenioVigente.getFechaCompromisoPago());
            }

            AvisoAdeudoEntity ultimo = ultimoPorUsuario.get(dto.getAguaUsuarioId());
            if (ultimo == null) {
                continue;
            }
            dto.setUltimoTipoAviso(ultimo.getTipoAviso());
            boolean entregado = ultimo.getFechaEntrega() != null;
            dto.setUltimoAvisoEntregado(entregado);
            dto.setUltimoAvisoFechaEntrega(ultimo.getFechaEntrega());

            // Además del convenio formal (módulo Convenios), el mismo
            // aviso puede traer su propia fecha comprometida cuando se
            // marcó atendida con resultado CONVENIO (ver marcarAtendida()) --
            // no hace falta un convenio formal para pausar el Segundo
            // aviso. Si esa fecha ya venció sin generarse un aviso nuevo,
            // deja de contar aquí y vuelve a requerirse el Segundo
            // normalmente -- justo la regla: incumple -> Segundo aviso;
            // si cumple y en el futuro debe de nuevo, es un aviso nuevo
            // (Primero otra vez), no continúa este mismo.
            boolean convenioPorAviso = "CONVENIO".equalsIgnoreCase(ultimo.getResultadoAtencion())
                    && ultimo.getFechaCompromiso() != null
                    && !ultimo.getFechaCompromiso().isBefore(LocalDate.now());
            if (convenioPorAviso && convenioVigente == null) {
                dto.setEnConvenioVigente(true);
                dto.setFechaCompromisoConvenio(ultimo.getFechaCompromiso());
            }

            dto.setRequiereSegundoAviso(convenioVigente == null && !convenioPorAviso
                    && "PRIMERO".equalsIgnoreCase(ultimo.getTipoAviso()) && entregado);
        }
    }

    // Trae activas Y canceladas -- el frontend las distingue con el campo
    // "cancelada" del DTO y las oculta por default (mismo patrón que ya se
    // usa con los usuarios dados de baja en candidatos), pero permite
    // consultarlas si se filtra explícito por ellas.
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoAdeudoRestResponse> historial() {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            List<AvisoAdeudoEntity> avisos = avisoAdeudoRepository.findByEstatusInOrderByFolioNotificacionDesc(List.of(1, 0));
            response.setData(avisos.stream().map(avisoAdeudoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Historial encontrado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar el historial de avisos", e);
        }
    }

    // Registra que una carta ya generada fue entregada (o el intento de
    // entrega, si se negaron a recibir o no encontraron a nadie) -- replica
    // los mismos datos que se llenan a mano en la sección "RAZÓN DE
    // NOTIFICACIÓN" de la carta física.
    @Transactional
    public ResponseEntity<AvisoAdeudoRestResponse> marcarEntregada(Integer avisoAdeudoId, AvisoAdeudoEntregaRequestDto request) {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            Optional<AvisoAdeudoEntity> avisoOpt = avisoAdeudoRepository.findById(avisoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoAdeudoEntity aviso = avisoOpt.get();
            aviso.setFechaEntrega(request.getFechaEntrega() != null ? request.getFechaEntrega() : LocalDateTime.now());
            aviso.setTipoEntrega(request.getTipoEntrega());
            aviso.setNombreReceptor(request.getNombreReceptor());
            aviso.setParentescoReceptor(request.getParentescoReceptor());
            aviso.setNombreNotificador(request.getNombreNotificador());
            aviso.setNombreTestigo1(request.getNombreTestigo1());
            aviso.setNombreTestigo2(request.getNombreTestigo2());
            aviso.setComentarioEntrega(request.getComentarioEntrega());

            // Cuando el motivo es ABONO y se ligó el folio del recibo, ese
            // recibo YA es el soporte del pago -- se valida que exista y,
            // de paso, se cierra la alerta de "pendiente de atención" de
            // una vez (equivale a marcarla atendida con resultado PAGADO),
            // para no obligar a Ely a repetir el mismo folio en un segundo
            // paso.
            Integer folioRecibo = request.getFolioReciboVinculado();
            if (folioRecibo != null) {
                WaterReceiptEntity recibo = waterReceiptRepository.findByNoFolio(folioRecibo);
                if (recibo == null) {
                    response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                            "No se encontró ningún recibo con el folio " + folioRecibo);
                    return ResponseEntity.badRequest().body(response);
                }
                aviso.setFolioReciboVinculado(folioRecibo);
                if ("ABONO".equalsIgnoreCase(request.getTipoEntrega())) {
                    aviso.setResultadoAtencion("PAGADO");
                    aviso.setFechaAtencion(LocalDateTime.now());
                }
            }

            avisoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoAdeudoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Entrega registrada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al registrar la entrega", e);
        }
    }

    // Cancela una carta ya generada (ej. se emitió por error, o el usuario
    // se puso al corriente antes de entregarla) -- se oculta del historial
    // por default pero sigue pudiéndose consultar, no se borra.
    @Transactional
    public ResponseEntity<AvisoAdeudoRestResponse> cancelar(Integer avisoAdeudoId) {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            Optional<AvisoAdeudoEntity> avisoOpt = avisoAdeudoRepository.findById(avisoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoAdeudoEntity aviso = avisoOpt.get();
            aviso.setEstatus(0);
            aviso.setUserIdCancela(currentUserService.getCurrentUserId());
            aviso.setDateCancela(LocalDateTime.now());
            avisoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoAdeudoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso cancelado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al cancelar el aviso", e);
        }
    }

    // Deshace una cancelación hecha por error.
    @Transactional
    public ResponseEntity<AvisoAdeudoRestResponse> reactivar(Integer avisoAdeudoId) {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            Optional<AvisoAdeudoEntity> avisoOpt = avisoAdeudoRepository.findById(avisoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            AvisoAdeudoEntity aviso = avisoOpt.get();
            aviso.setEstatus(1);
            aviso.setUserIdCancela(null);
            aviso.setDateCancela(null);
            avisoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoAdeudoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Aviso reactivado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al reactivar el aviso", e);
        }
    }

    // Historial completo (activas + canceladas) de un usuario específico --
    // para el acordeón "Cartas generadas" en su ficha (details-user).
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoAdeudoRestResponse> porUsuario(Integer aguaUsuarioId) {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            List<AvisoAdeudoEntity> avisos = avisoAdeudoRepository
                    .findByWaterUser_AguaUsuarioIdAndEstatusInOrderByFolioNotificacionDesc(aguaUsuarioId, List.of(1, 0));
            response.setData(avisos.stream().map(avisoAdeudoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Cartas encontradas");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar las cartas del usuario", e);
        }
    }

    // Cartas ya entregadas a este usuario que aún no se marcan como
    // atendidas -- para la alerta al consultar la ficha del usuario, "a
    // razón de realizar el cobro correspondiente".
    @Transactional(readOnly = true)
    public ResponseEntity<AvisoAdeudoRestResponse> pendientesDeAtencion(Integer aguaUsuarioId) {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            List<AvisoAdeudoEntity> avisos = avisoAdeudoRepository.findPendientesDeAtencionPorUsuario(aguaUsuarioId);
            response.setData(avisos.stream().map(avisoAdeudoMapper::entityToDto).toList());
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Pendientes encontrados");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al consultar avisos pendientes de atención", e);
        }
    }

    // Categorías válidas para resultadoAtencion -- igual que tipoAviso/
    // tipoEntrega, es un catálogo chico fijo en código (no tabla), pero a
    // diferencia de esos sí se valida aquí porque alimenta reportes (contar
    // cuántas cartas terminaron pagadas vs condonadas).
    private static final List<String> RESULTADOS_ATENCION_VALIDOS = List.of("PAGADO", "CONDONADO", "CONVENIO", "OTRO");

    // Marca que, tras la entrega, ya se hizo el cobro (o el trámite que
    // corresponda) -- deja de aparecer la alerta al consultar al usuario.
    // Además de la fecha, ahora se captura CÓMO se resolvió (resultadoAtencion,
    // ver RESULTADOS_ATENCION_VALIDOS), un comentario libre opcional, y --
    // opcional también -- el folio del recibo real que corresponde al pago,
    // que se valida contra agua_recibo antes de guardarse como referencia
    // (el monto en sí no se duplica aquí, vive únicamente en el recibo).
    @Transactional
    public ResponseEntity<AvisoAdeudoRestResponse> marcarAtendida(Integer avisoAdeudoId, AvisoAdeudoAtencionRequestDto request) {
        AvisoAdeudoRestResponse response = new AvisoAdeudoRestResponse();
        try {
            Optional<AvisoAdeudoEntity> avisoOpt = avisoAdeudoRepository.findById(avisoAdeudoId);
            if (avisoOpt.isEmpty()) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE, "Aviso no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            String resultado = request != null ? request.getResultadoAtencion() : null;
            if (resultado == null || resultado.isBlank() || !RESULTADOS_ATENCION_VALIDOS.contains(resultado)) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                        "Indica cómo se resolvió (pagado, condonado, convenio u otro)");
                return ResponseEntity.badRequest().body(response);
            }

            // Un convenio sin fecha comprometida no sirve para pausar el
            // Segundo aviso (no habría con qué compararlo), así que se pide
            // aquí en vez de dejarla como un dato "para después".
            if ("CONVENIO".equals(resultado) && request.getFechaCompromiso() == null) {
                response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                        "Indica la fecha en que se comprometió a pagar");
                return ResponseEntity.badRequest().body(response);
            }

            AvisoAdeudoEntity aviso = avisoOpt.get();

            Integer folio = request.getFolioReciboVinculado();
            if (folio != null) {
                WaterReceiptEntity recibo = waterReceiptRepository.findByNoFolio(folio);
                if (recibo == null) {
                    response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                            "No se encontró ningún recibo con el folio " + folio);
                    return ResponseEntity.badRequest().body(response);
                }
                if (aviso.getWaterUser() != null && recibo.getWaterUser() != null
                        && !aviso.getWaterUser().getAguaUsuarioId().equals(recibo.getWaterUser().getAguaUsuarioId())) {
                    response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                            "El recibo con folio " + folio + " no corresponde a este usuario -- revisa el folio");
                    return ResponseEntity.badRequest().body(response);
                }
                aviso.setFolioReciboVinculado(folio);
            } else {
                aviso.setFolioReciboVinculado(null);
            }

            // Folio de convenio -- independiente del recibo, pueden venir
            // ambos a la vez (ver AvisoAdeudoAtencionRequestDto).
            Integer folioConvenio = request.getFolioConvenioVinculado();
            if (folioConvenio != null) {
                WaterAgreementEntity convenio = waterAgreementRepository.findFirstByNoFolio(folioConvenio);
                if (convenio == null) {
                    response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                            "No se encontró ningún convenio con el folio " + folioConvenio);
                    return ResponseEntity.badRequest().body(response);
                }
                if (aviso.getWaterUser() != null && convenio.getWaterUser() != null
                        && !aviso.getWaterUser().getAguaUsuarioId().equals(convenio.getWaterUser().getAguaUsuarioId())) {
                    response.addMetadata(Constants.ERROR_RESPONSE_MESSAGE, Constants.ERROR_RESPONSE_CODE,
                            "El convenio con folio " + folioConvenio + " no corresponde a este usuario -- revisa el folio");
                    return ResponseEntity.badRequest().body(response);
                }
                aviso.setFolioConvenioVinculado(folioConvenio);
            } else {
                aviso.setFolioConvenioVinculado(null);
            }

            aviso.setResultadoAtencion(resultado);
            aviso.setComentarioAtencion(request.getComentarioAtencion() != null && !request.getComentarioAtencion().isBlank()
                    ? request.getComentarioAtencion().trim() : null);
            // Solo se guarda cuando el resultado es CONVENIO -- si se
            // vuelve a marcar atendida con otro resultado más adelante, se
            // limpia (ya no debe seguir pausando el Segundo aviso).
            aviso.setFechaCompromiso("CONVENIO".equals(resultado) ? request.getFechaCompromiso() : null);
            aviso.setFechaAtencion(LocalDateTime.now());
            aviso.setUserIdAtencion(currentUserService.getCurrentUserId());
            avisoAdeudoRepository.save(aviso);

            response.setData(List.of(avisoAdeudoMapper.entityToDto(aviso)));
            response.addMetadata(Constants.OK_RESPONSE_MESSAGE, Constants.OK_RESPONSE_CODE, "Marcado como atendido");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseHandler.handleInternalServerError(response, "Error al marcar el aviso como atendido", e);
        }
    }

    // Resultado de generar el lote: el PDF ya armado, cuántas cartas trae
    // realmente, y qué usuarios se omitieron (recalculados justo antes de
    // generar, por si ya no tienen adeudo -- ej. pagaron entre que se armó
    // la lista en pantalla y se dio clic en "Generar").
    public record GenerarAvisosResultado(byte[] pdfBytes, int totalGeneradas, List<Integer> noUsuariosOmitidos) {
    }

    @Transactional
    public GenerarAvisosResultado generar(AvisoAdeudoGenerarRequestDto request) throws IOException, DocumentException {
        String tipoAviso = normalizarTipoAviso(request.getTipoAviso());
        List<Integer> aguaUsuarioIds = request.getAguaUsuarioIds() != null ? request.getAguaUsuarioIds() : new ArrayList<>();

        List<AdeudoLuzUsuarioDto> adeudos = adeudoLuzService.calcularParaUsuarios(aguaUsuarioIds);
        Map<Integer, AdeudoLuzUsuarioDto> adeudoPorUsuarioId = new HashMap<>();
        for (AdeudoLuzUsuarioDto dto : adeudos) {
            adeudoPorUsuarioId.put(dto.getAguaUsuarioId(), dto);
        }

        List<Integer> omitidos = new ArrayList<>();
        List<CartaAdeudoDatos> cartas = new ArrayList<>();
        List<AvisoAdeudoEntity> paraGuardar = new ArrayList<>();

        Integer siguienteFolio = Optional.ofNullable(avisoAdeudoRepository.findMaxFolio()).orElse(0) + 1;
        Integer userIdAdd = currentUserService.getCurrentUserId();
        LocalDateTime ahora = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        for (Integer aguaUsuarioId : aguaUsuarioIds) {
            AdeudoLuzUsuarioDto adeudo = adeudoPorUsuarioId.get(aguaUsuarioId);
            if (adeudo == null || adeudo.getAdeudoTotal() == null || adeudo.getAdeudoTotal() <= 0) {
                Optional<WaterUserEntity> u = waterUserRepository.findById(aguaUsuarioId);
                u.ifPresent(waterUserEntity -> omitidos.add(waterUserEntity.getNoUsuario()));
                continue;
            }

            Optional<WaterUserEntity> usuarioOpt = waterUserRepository.findById(aguaUsuarioId);
            if (usuarioOpt.isEmpty()) {
                continue;
            }
            WaterUserEntity usuario = usuarioOpt.get();

            Integer folio = siguienteFolio++;

            String fechaUltimoPagoTexto = adeudo.getFechaUltimoPago() != null
                    ? adeudo.getFechaUltimoPago().format(FORMATO_FECHA)
                    : "";

            // "2023 - $840.00 | 2024 - $1,200.00 | ... | Total $4,340.00" --
            // el desglose por año que ya se calculó (adeudo.periodosAdeudadosTexto)
            // más el total al final, para que el usuario pueda revisar la
            // carta contra su propio cálculo año por año.
            String desglosePorAnio = (adeudo.getPeriodosAdeudadosTexto() != null && !adeudo.getPeriodosAdeudadosTexto().isBlank()
                    ? adeudo.getPeriodosAdeudadosTexto() + " | "
                    : "") + "Total " + formatoMoneda.format(adeudo.getAdeudoTotal());

            // "577 - Juan Pérez" -- número de usuario al frente del nombre,
            // para poder ubicarlo fácil contra el sistema al revisar.
            String nombreConNumero = adeudo.getNoUsuario() + " - " + adeudo.getNombreCompleto();

            // Los cargos automáticos (Aviso, Mantenimiento) se crean ANTES
            // de armar la carta/el snapshot del historial, y su monto se
            // suma aquí a la multa acumulada -- si se crearan después (como
            // pasaba antes), el cargo de ESTE MISMO folio no aparecería en
            // la carta que se está imprimiendo justo ahora, sino hasta la
            // siguiente que se le genere a este usuario.
            double multaAcumulada = adeudo.getMultaAcumulada() != null ? adeudo.getMultaAcumulada() : 0d;

            // Art. 20 del Reglamento: "el primer y el segundo aviso tienen
            // un costo de $200.00 cada uno, cargado a la cuenta del
            // usuario" -- antes había que darlo de alta a mano en
            // Cargos/Multas cada vez; ahora se genera solo al imprimir la
            // carta, para un usuario registrado (no aplica a "no
            // registrados", que no tienen cuenta en el sistema).
            multaAcumulada += crearCargoAviso(usuario, tipoAviso, folio, userIdAdd, ahora);

            // Cooperación extraordinaria de mantenimiento de cajón (Art.
            // 10): $100 por cada año 2024/2025 que este usuario deba --
            // independiente del tipo de aviso, y solo una vez por año (ver
            // dedupe dentro del método). El cargo SÍ se sigue creando igual
            // (se cobra, queda en Cargos/Multas de su ficha), pero desde la
            // v5 de la carta (sept. 2026) su monto YA NO se suma a
            // "multaAcumulada" -- se calcula aparte, en su propio renglón
            // de la carta ("Adeudo de mantenimiento", ver
            // AvisoAdeudoPdfService), igual que adeudo.getMantenimientoPendiente()
            // ya trae lo pendiente de cartas anteriores.
            double mantenimientoPendiente = adeudo.getMantenimientoPendiente() != null ? adeudo.getMantenimientoPendiente() : 0d;
            Map<Integer, Double> mantenimientoPorAnio = new TreeMap<>();
            if (adeudo.getMantenimientoPorAnio() != null) {
                mantenimientoPorAnio.putAll(adeudo.getMantenimientoPorAnio());
            }
            Map<Integer, Double> mantenimientoRecienAgregado = crearCargoMantenimiento(usuario, adeudo.getPeriodosAdeudados(), userIdAdd, ahora);
            for (Map.Entry<Integer, Double> entry : mantenimientoRecienAgregado.entrySet()) {
                mantenimientoPendiente += entry.getValue();
                mantenimientoPorAnio.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
            // "$100.00 - 2024 | $100.00 - 2025" -- monto antes del año, a
            // diferencia de desglosePorAnio (año antes del monto), por
            // pedido explícito de Ely para este renglón combinado.
            String mantenimientoPorAnioTexto = mantenimientoPorAnio.entrySet().stream()
                    .map(e -> formatoMoneda.format(e.getValue()) + " - " + e.getKey())
                    .collect(Collectors.joining(" | "));

            // Solo para el Segundo aviso: reemplaza las notas genéricas de
            // mantenimiento/Art. 22 Bis por los datos de cuándo y a quién
            // se le entregó el Primer aviso (ver AvisoAdeudoPdfService).
            String notaEntregaPrimerAviso = "SEGUNDO".equalsIgnoreCase(tipoAviso)
                    ? construirNotaEntregaPrimerAviso(aguaUsuarioId)
                    : null;

            cartas.add(new CartaAdeudoDatos(
                    folio,
                    tipoAviso,
                    nombreConNumero,
                    adeudo.getCasaNoTexto(),
                    adeudo.getDomicilioToma(),
                    adeudo.getAdeudoTotal(),
                    adeudo.getNoFolioUltimoPago(),
                    fechaUltimoPagoTexto,
                    desglosePorAnio,
                    request.getFechaPresentacion(),
                    false,
                    multaAcumulada,
                    notaEntregaPrimerAviso,
                    mantenimientoPendiente,
                    mantenimientoPorAnioTexto,
                    null
            ));

            // El snapshot del historial guarda el nombre SIN el número al
            // frente -- el historial ya arma "noUsuario - nombre" él mismo
            // en pantalla (a partir de waterUser), para no duplicarlo.
            paraGuardar.add(AvisoAdeudoEntity.builder()
                    .folioNotificacion(folio)
                    .tipoAviso(tipoAviso)
                    .nombreUsuarioTitular(adeudo.getNombreCompleto())
                    .noCasa(adeudo.getCasaNo())
                    .noCasaTexto(adeudo.getCasaNoTexto())
                    .domicilioToma(adeudo.getDomicilioToma())
                    .periodosAdeudados(adeudo.getPeriodosAdeudadosTexto())
                    .adeudoTotal(adeudo.getAdeudoTotal())
                    .noFolioUltimoPago(adeudo.getNoFolioUltimoPago())
                    .fechaUltimoPago(adeudo.getFechaUltimoPago())
                    .multaAcumulada(multaAcumulada)
                    .fechaPresentacion(request.getFechaPresentacion())
                    .estatus(1)
                    .userIdAdd(userIdAdd)
                    .dateAdd(ahora)
                    .waterUser(usuario)
                    .build());
        }

        // Personas SIN usuario en el sistema -- no hay recibos que
        // consultar ni WaterUserEntity al cual ligar, así que se calculan
        // aparte (cuota fija por año) y NO se guardan en el historial
        // (agua_aviso_adeudo.usuario_id es NOT NULL).
        if (request.getNoRegistrados() != null) {
            for (UsuarioNoRegistradoDto noRegistrado : request.getNoRegistrados()) {
                if (noRegistrado == null || noRegistrado.getNombre() == null || noRegistrado.getNombre().isBlank()) {
                    continue;
                }

                Integer folio = siguienteFolio++;

                double total = CUOTAS_FIJAS_NO_REGISTRADO.values().stream().mapToDouble(Double::doubleValue).sum();
                String desglosePorAnio = CUOTAS_FIJAS_NO_REGISTRADO.entrySet().stream()
                        .map(e -> e.getKey() + " - " + formatoMoneda.format(e.getValue()))
                        .collect(Collectors.joining(" | "))
                        + " | Total " + formatoMoneda.format(total);

                cartas.add(new CartaAdeudoDatos(
                        folio,
                        tipoAviso,
                        noRegistrado.getNombre().trim(),
                        "",
                        noRegistrado.getDireccion() != null ? noRegistrado.getDireccion().trim() : "",
                        total,
                        null,
                        "",
                        desglosePorAnio,
                        request.getFechaPresentacion(),
                        true,
                        0d,
                        null,
                        0d,
                        "",
                        null
                ));
            }
        }

        // Usuarios YA registrados con monto de adeudo capturado a mano --
        // ver UsuarioManualAdeudoDto. A diferencia de noRegistrados, SÍ
        // tienen aguaUsuarioId real: se les genera el mismo cargo
        // automático de Aviso (Art. 20) que a cualquier otro, y la carta SÍ
        // queda en su historial. El monto de adeudo en sí (Luz/CFE) no se
        // recalcula -- se usa tal cual lo capturado, precisamente porque el
        // cálculo automático no aplica o no lo detecta (motivo que se pide
        // dejar en observacion).
        if (request.getUsuariosManuales() != null) {
            for (UsuarioManualAdeudoDto manual : request.getUsuariosManuales()) {
                if (manual == null || manual.getAguaUsuarioId() == null
                        || manual.getMontoAdeudo() == null || manual.getMontoAdeudo() <= 0) {
                    continue;
                }

                Optional<WaterUserEntity> usuarioOpt = waterUserRepository.findById(manual.getAguaUsuarioId());
                if (usuarioOpt.isEmpty()) {
                    continue;
                }
                WaterUserEntity usuario = usuarioOpt.get();

                Integer folio = siguienteFolio++;

                String nombreCompleto = buildNombreCompleto(usuario);
                String nombreConNumero = usuario.getNoUsuario() + " - " + nombreCompleto;
                String casaNoTexto = buildCasaNoTexto(usuario);
                String domicilioToma = buildDomicilio(usuario);

                double multaAcumulada = crearCargoAviso(usuario, tipoAviso, folio, userIdAdd, ahora);

                String observacion = manual.getObservacion() != null && !manual.getObservacion().isBlank()
                        ? manual.getObservacion().trim() : null;

                // "Monto capturado manualmente | Total $X" -- mismo criterio
                // visual que el desglose por año normal, pero dejando claro
                // que aquí no hay un cálculo año por año detrás.
                String desglosePorAnio = "Monto capturado manualmente | Total " + formatoMoneda.format(manual.getMontoAdeudo());

                String notaEntregaPrimerAviso = "SEGUNDO".equalsIgnoreCase(tipoAviso)
                        ? construirNotaEntregaPrimerAviso(manual.getAguaUsuarioId())
                        : null;

                cartas.add(new CartaAdeudoDatos(
                        folio,
                        tipoAviso,
                        nombreConNumero,
                        casaNoTexto,
                        domicilioToma,
                        manual.getMontoAdeudo(),
                        null,
                        "",
                        desglosePorAnio,
                        request.getFechaPresentacion(),
                        false,
                        multaAcumulada,
                        notaEntregaPrimerAviso,
                        0d,
                        "",
                        observacion
                ));

                paraGuardar.add(AvisoAdeudoEntity.builder()
                        .folioNotificacion(folio)
                        .tipoAviso(tipoAviso)
                        .nombreUsuarioTitular(nombreCompleto)
                        .noCasa(usuario.getWaterHouse() != null ? usuario.getWaterHouse().getCasaNo() : null)
                        .noCasaTexto(casaNoTexto)
                        .domicilioToma(domicilioToma)
                        .periodosAdeudados("Monto capturado manualmente" + (observacion != null ? " -- " + observacion : ""))
                        .adeudoTotal(manual.getMontoAdeudo())
                        .noFolioUltimoPago(null)
                        .fechaUltimoPago(null)
                        .multaAcumulada(multaAcumulada)
                        .fechaPresentacion(request.getFechaPresentacion())
                        .estatus(1)
                        .userIdAdd(userIdAdd)
                        .dateAdd(ahora)
                        .waterUser(usuario)
                        .build());
            }
        }

        if (cartas.isEmpty()) {
            return new GenerarAvisosResultado(new byte[0], 0, omitidos);
        }

        byte[] pdf = avisoAdeudoPdfService.generarLote(cartas);
        avisoAdeudoRepository.saveAll(paraGuardar);

        return new GenerarAvisosResultado(pdf, cartas.size(), omitidos);
    }

    // "Calle #número" -- mismo criterio que AdeudoLuzService.buildDomicilio()
    // (duplicado aquí porque ese método es privado y este flujo, a
    // diferencia del normal, no pasa por AdeudoLuzService para nada -- el
    // usuario no salió como candidato, por eso se está capturando a mano).
    private String buildDomicilio(WaterUserEntity user) {
        if (user.getAddress() != null && user.getAddress().getCalle() != null && !user.getAddress().getCalle().isBlank()) {
            String numero = user.getAddress().getNumero();
            return user.getAddress().getCalle() + (numero != null && !numero.isBlank() ? " #" + numero : "");
        }
        WaterHouseEntity casa = user.getWaterHouse();
        if (casa != null) {
            String calle = casa.getCatCalle() != null ? casa.getCatCalle().getNombre() : "";
            String casaNo = casa.getCasaNo() != null ? " #" + casa.getCasaNo() : "";
            return (calle + casaNo).trim();
        }
        return "";
    }

    // "2-D" / "2-I" -- mismo criterio que AdeudoLuzService.buildCasaNoTexto().
    private String buildCasaNoTexto(WaterUserEntity user) {
        WaterHouseEntity casa = user.getWaterHouse();
        if (casa == null || casa.getCasaNo() == null) {
            return "";
        }
        String lado = casa.getLado();
        return lado != null && !lado.isBlank() ? casa.getCasaNo() + "-" + lado : String.valueOf(casa.getCasaNo());
    }

    // Mismo criterio que AdeudoLuzService.buildNombreCompleto().
    private String buildNombreCompleto(WaterUserEntity user) {
        PersonEntity person = user.getPerson();
        if (person == null) return "";
        return String.join(" ",
                        nullToVacio(person.getNombre()), nullToVacio(person.getNombre2()),
                        nullToVacio(person.getApp()), nullToVacio(person.getApm()))
                .replaceAll("\\s+", " ").trim();
    }

    private String nullToVacio(String valor) {
        return valor != null ? valor : "";
    }

    private String normalizarTipoAviso(String valor) {
        if (valor != null && valor.equalsIgnoreCase("SEGUNDO")) {
            return "SEGUNDO";
        }
        return "PRIMERO";
    }

    // Genera automáticamente el cargo de $200 del Art. 20 (costo del
    // Primer/Segundo aviso, "cargado a la cuenta del usuario") -- antes se
    // tenía que capturar a mano en Cargos/Multas cada vez; el monto se toma
    // de Valores Generales (clave AVISO, vigente en el año) en vez de un
    // $200 fijo en código, para que si el Comité lo actualiza ahí, el
    // cargo automático también cambie. Si el concepto "Aviso" no existe en
    // el catálogo CONCEPTO_CARGO_EXTRA (se renombró o se borró desde
    // Catálogos), o si no hay un monto vigente configurado, no se crea el
    // cargo pero tampoco se detiene la generación de la carta -- solo se
    // deja constancia en el log para que se revise. Devuelve el monto
    // agregado (0 si no se creó nada) para que quien llama pueda sumarlo a
    // la "multa acumulada" que se imprime en ESTA MISMA carta -- si no, el
    // cargo recién creado no aparecería sino hasta la siguiente carta que
    // se le genere a este usuario.
    private double crearCargoAviso(WaterUserEntity usuario, String tipoAviso, Integer folio, Integer userIdAdd, LocalDateTime ahora) {
        Optional<CatalogOptionsEntity> conceptoOpt = catalogOptionsRepository
                .findByCatalog_ClaveAndNombreAndEstatus(CATALOGO_CARGO_EXTRA_CLAVE, CONCEPTO_AVISO_NOMBRE, 1);
        if (conceptoOpt.isEmpty()) {
            log.warn("No se encontró el concepto '{}' en el catálogo {} -- no se generó el cargo automático del folio {}",
                    CONCEPTO_AVISO_NOMBRE, CATALOGO_CARGO_EXTRA_CLAVE, folio);
            return 0d;
        }

        String tipoTexto = "SEGUNDO".equalsIgnoreCase(tipoAviso) ? "Segundo aviso" : "Primer aviso";
        // El folio es único por carta (siguienteFolio++), así que se usa
        // como identificador de "ya se generó este cargo" -- a diferencia
        // del nombre del concepto/tipo (que se repite cada vez que a un
        // mismo usuario le toca otro Primer/Segundo aviso, folios
        // distintos, y SÍ debe generar un cargo nuevo cada vez). Evita
        // duplicar el cargo si generar() se llega a invocar dos veces para
        // el mismo folio (doble clic, reintento de red, etc. -- bug
        // reportado por Ely: dos renglones idénticos "folio 38" en
        // agua_usuario_cargo para el mismo usuario).
        String descripcion = tipoTexto + " de adeudo -- folio " + folio;
        boolean yaExiste = waterUserChargeRepository.existsByWaterUser_AguaUsuarioIdAndConcepto_NombreAndDescripcionAndEstatus(
                usuario.getAguaUsuarioId(), CONCEPTO_AVISO_NOMBRE, descripcion, 1);
        if (yaExiste) {
            log.warn("Ya existe un cargo de '{}' con la descripción '{}' para el usuario {} -- no se generó de nuevo (evita duplicado)",
                    CONCEPTO_AVISO_NOMBRE, descripcion, usuario.getNoUsuario());
            return 0d;
        }

        double monto = valorGeneralService.getMontoVigente(ValorGeneralClave.AVISO, ahora.getYear());
        if (monto <= 0) {
            log.warn("El valor vigente de '{}' es $0.00 -- no se generó el cargo automático del folio {}", CONCEPTO_AVISO_NOMBRE, folio);
            return 0d;
        }

        WaterUserChargeEntity cargo = WaterUserChargeEntity.builder()
                .waterUser(usuario)
                .concepto(conceptoOpt.get())
                .descripcion(descripcion)
                .monto(monto)
                .fecha(ahora.toLocalDate())
                .comentario("Generado automáticamente al imprimir la carta (Art. 20 del Reglamento)")
                .estatus(1)
                .userIdAdd(userIdAdd)
                .dateAdd(ahora)
                .build();
        waterUserChargeRepository.save(cargo);
        return monto;
    }

    // Cooperación extraordinaria de mantenimiento de cajón (Art. 10): $100
    // por cada uno de ANIOS_MANTENIMIENTO que el usuario deba (se checa
    // contra el desglose por año ya calculado -- adeudo.periodosAdeudados).
    // Solo se crea si aún no existe un cargo con esa descripción exacta
    // para este usuario (evita duplicar el cargo si se le genera más de
    // una carta, ej. Primero y luego Segundo aviso). Igual que
    // crearCargoAviso(), si falta el concepto en el catálogo o el monto
    // vigente es $0.00, no se crea el cargo pero tampoco se detiene la
    // carta -- solo queda constancia en el log. Devuelve un mapa año->monto
    // con SOLO lo agregado en esta llamada (vacío si ya existía todo o no
    // se creó nada) -- a diferencia de antes (devolvía un double), así se
    // puede combinar con adeudo.getMantenimientoPorAnio() (lo ya existente
    // de cartas anteriores) para armar el desglose completo por año.
    private Map<Integer, Double> crearCargoMantenimiento(WaterUserEntity usuario, List<Integer> periodosAdeudados, Integer userIdAdd, LocalDateTime ahora) {
        Map<Integer, Double> agregadoPorAnio = new TreeMap<>();
        if (periodosAdeudados == null || periodosAdeudados.isEmpty()) {
            return agregadoPorAnio;
        }

        Optional<CatalogOptionsEntity> conceptoOpt = catalogOptionsRepository
                .findByCatalog_ClaveAndNombreAndEstatus(CATALOGO_CARGO_EXTRA_CLAVE, CONCEPTO_MANTENIMIENTO_NOMBRE, 1);
        if (conceptoOpt.isEmpty()) {
            log.warn("No se encontró el concepto '{}' en el catálogo {} -- no se generó el cargo de mantenimiento para el usuario {}",
                    CONCEPTO_MANTENIMIENTO_NOMBRE, CATALOGO_CARGO_EXTRA_CLAVE, usuario.getNoUsuario());
            return agregadoPorAnio;
        }

        for (Integer anio : ANIOS_MANTENIMIENTO) {
            if (!periodosAdeudados.contains(anio)) {
                continue;
            }

            String descripcion = CONCEPTO_MANTENIMIENTO_NOMBRE + " " + anio;
            boolean yaExiste = waterUserChargeRepository.existsByWaterUser_AguaUsuarioIdAndConcepto_NombreAndDescripcionAndEstatus(
                    usuario.getAguaUsuarioId(), CONCEPTO_MANTENIMIENTO_NOMBRE, descripcion, 1);
            if (yaExiste) {
                continue;
            }

            double monto = valorGeneralService.getMontoVigente(ValorGeneralClave.MANTENIMIENTO, anio);
            if (monto <= 0) {
                log.warn("El valor vigente de '{}' para {} es $0.00 -- no se generó el cargo para el usuario {}",
                        CONCEPTO_MANTENIMIENTO_NOMBRE, anio, usuario.getNoUsuario());
                continue;
            }

            WaterUserChargeEntity cargo = WaterUserChargeEntity.builder()
                    .waterUser(usuario)
                    .concepto(conceptoOpt.get())
                    .descripcion(descripcion)
                    .monto(monto)
                    .fecha(ahora.toLocalDate())
                    .comentario("Cooperación extraordinaria de mantenimiento (Art. 10) -- generado automáticamente al detectar adeudo de " + anio)
                    .estatus(1)
                    .userIdAdd(userIdAdd)
                    .dateAdd(ahora)
                    .build();
            waterUserChargeRepository.save(cargo);
            agregadoPorAnio.merge(anio, monto, Double::sum);
        }
        return agregadoPorAnio;
    }

    // Cuando esta carta es un Segundo aviso, arma un texto con cuándo y a
    // quién se le entregó el Primer aviso más reciente de este mismo
    // usuario (folio, fecha, tipo de entrega) -- para mostrarlo en la
    // carta en vez de las notas genéricas de mantenimiento/Art. 22 Bis
    // (ver agregarPaginaAviso() en AvisoAdeudoPdfService). Null si no hay
    // un Primer aviso entregado que mostrar (no debería pasar en el flujo
    // normal, ya que el Segundo aviso solo se ofrece cuando el Primero ya
    // se entregó -- ver enriquecerConUltimoAviso()).
    private String construirNotaEntregaPrimerAviso(Integer aguaUsuarioId) {
        List<AvisoAdeudoEntity> anteriores = avisoAdeudoRepository
                .findByWaterUser_AguaUsuarioIdAndEstatusOrderByFolioNotificacionDesc(aguaUsuarioId, 1);
        AvisoAdeudoEntity primero = anteriores.stream()
                .filter(a -> "PRIMERO".equalsIgnoreCase(a.getTipoAviso()) && a.getFechaEntrega() != null)
                .findFirst()
                .orElse(null);
        if (primero == null) {
            return null;
        }

        String fechaTexto = primero.getFechaEntrega().toLocalDate().format(FORMATO_FECHA);
        String comoSeEntrego;
        String tipoEntrega = primero.getTipoEntrega();
        if ("TITULAR".equalsIgnoreCase(tipoEntrega)) {
            comoSeEntrego = "al propio titular, quien firmó de recibido";
        } else if ("OTRA_PERSONA".equalsIgnoreCase(tipoEntrega)) {
            String receptor = primero.getNombreReceptor() != null && !primero.getNombreReceptor().isBlank()
                    ? primero.getNombreReceptor() : "otra persona";
            String parentesco = primero.getParentescoReceptor() != null && !primero.getParentescoReceptor().isBlank()
                    ? " (" + primero.getParentescoReceptor() + " del titular)" : "";
            comoSeEntrego = "a " + receptor + parentesco + ", quien firmó de recibido";
        } else if ("SE_NEGO".equalsIgnoreCase(tipoEntrega)) {
            comoSeEntrego = "sin que la persona que atendió aceptara recibir o firmar; se fijó copia en el domicilio, ante testigos";
        } else if ("NO_ENCONTRADO".equalsIgnoreCase(tipoEntrega)) {
            comoSeEntrego = "sin encontrar a persona alguna en el domicilio; se fijó copia, ante testigos";
        } else if ("ABONO".equalsIgnoreCase(tipoEntrega)) {
            comoSeEntrego = "no se dejó, ya que el usuario realizó un abono en el momento";
        } else {
            comoSeEntrego = "conforme consta en el acta de notificación respectiva";
        }

        return "El Primer aviso (folio " + primero.getFolioNotificacion() + ") fue entregado el " + fechaTexto + " " + comoSeEntrego
                + ". Al no haberse puesto al corriente, procede el presente Segundo aviso conforme al Artículo 20 del Reglamento Interno.";
    }
}
