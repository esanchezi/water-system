package com.mx.uvas.watersystem.services.impl;

import com.mx.uvas.watersystem.dto.AdeudoLuzUsuarioDto;
import com.mx.uvas.watersystem.model.CatalogOptionsEntity;
import com.mx.uvas.watersystem.model.PersonEntity;
import com.mx.uvas.watersystem.model.WaterHouseEntity;
import com.mx.uvas.watersystem.model.WaterReceiptPaymentEntity;
import com.mx.uvas.watersystem.model.WaterUserChargeEntity;
import com.mx.uvas.watersystem.model.WaterUserEntity;
import com.mx.uvas.watersystem.repositories.ICatalogOptionsRepository;
import com.mx.uvas.watersystem.repositories.IFeeAmountRepository;
import com.mx.uvas.watersystem.repositories.IWaterReceiptPaymentRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserAnnualPaymentRepository;
import com.mx.uvas.watersystem.repositories.IRenunciaTemporalRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserChargeRepository;
import com.mx.uvas.watersystem.repositories.IWaterUserRepository;
import com.mx.uvas.watersystem.services.IValorGeneralService;
import com.mx.uvas.watersystem.utils.ValorGeneralClave;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

// Calcula, por usuario y por año, cuota anual vs. lo pagado ÚNICAMENTE por
// la aportación de Luz/CFE (concepto_id = 6) -- es la misma regla que ya
// usa DeudorService (año marcado como pagado en agua_usuario_pago_anual
// gana sobre cualquier otro cálculo; si no, se suma lo abonado con ese
// concepto en el año), pero aquí recorre VARIOS años (2021 en adelante)
// en vez de uno solo, y NO mezcla cargos/multas -- la carta de adeudo deja
// explícito que ese adeudo es aparte ("si además tiene adeudo de
// mantenimiento, se le informará el detalle al presentarse").
@Service
@AllArgsConstructor
public class AdeudoLuzService {

    private static final Integer CONCEPTO_LUZ_ID = 6;

    // Primer año que se tiene capturado en el sistema -- igual que en todas
    // las consultas SQL que ya se usaban a mano para este mismo cálculo.
    private static final int ANIO_INICIO = 2021;

    // v8 (sept. 2026, pedido de Ely): antes había una regla fija aquí mismo
    // ("no es candidato si abonó algo hace menos de 3 meses, aunque su
    // saldo siga positivo") que descartaba por completo a quien pagara
    // poco pero seguido, ocultando casos donde ya debía más de una cuota
    // completa aunque hubiera abonado hace poco. Esa regla se quitó de
    // aquí -- ver mesesSinPagoLuz más abajo -- y ahora vive como un filtro
    // AJUSTABLE en la pantalla de candidatos (AvisoAdeudoListComponent),
    // para no tener que pedir un cambio de código cada vez que el criterio
    // cambie. Este valor centinela es el que se usa cuando el usuario
    // NUNCA ha pagado (no hay fecha de la cual contar meses) -- así
    // cualquier filtro de "mínimo de meses sin abonar" lo sigue incluyendo,
    // sin tener que tratar null como caso aparte en el frontend.
    private static final int MESES_SIN_PAGO_NUNCA_PAGADO = 9999;

    // v6 de la carta (sept. 2026, pedido explícito de Ely): "Multa acumulada
    // a la fecha" ya NO es una lista fija de nombres de concepto -- ahora
    // cuenta CUALQUIER cargo activo del panel "Cargos/Multas" de la ficha
    // del usuario (WaterUserChargeEntity), sin importar el nombre exacto que
    // tenga el concepto en el catálogo (antes se excluían por accidente
    // cargos reales como "Llave azul", "Mano de obra corte" o "Multa"
    // genérica, porque su nombre no coincidía exactamente con la lista fija
    // que existía antes -- Ely reportó varios usuarios con este problema).
    // El único que se excluye a propósito es "Mantenimiento de cajón"
    // (CONCEPTO_MANTENIMIENTO_NOMBRE, ver abajo): ese adeudo se muestra
    // aparte, en su propio renglón de la carta, con su propio desglose por
    // año (ver mantenimientoPendiente / AvisoAdeudoPdfService).
    private static final String CONCEPTO_MANTENIMIENTO_NOMBRE = "Mantenimiento de cajón";

    // v11 (sept. 2026, pedido explícito de Ely: "reemplazar todo lo que se
    // haya generado con ese concepto y usar Mtto -- para mí es como tenerlo
    // repetido"): el adeudo de mantenimiento ya NO se lee de cargos
    // formales creados aparte (WaterUserChargeEntity "Mantenimiento de
    // cajón") -- se calcula en vivo igual que la cuota anual de Luz, viendo
    // si el usuario ya dio la cooperación "Mtto" (concepto_id = 79, "Mtto
    // red", pago suelto dentro de un recibo normal) para cada año que debe.
    // Ver más abajo donde se arma mantenimientoPorAnio.
    //
    // CORRECCIÓN (confirmado por Ely con datos reales de catalogo_opciones):
    // el id correcto de "Mtto red" es 79, NO 108 -- 108 es "Multa por
    // corte", un concepto totalmente distinto. El 108 venía de un
    // comentario en un SQL guardado que resultó estar mal, nunca se validó
    // contra el catálogo real. Revisar con cuidado antes de confiar en
    // comentarios de queries viejos para IDs de catálogo.
    private static final Integer CONCEPTO_MTTO_ID = 79;
    private static final List<Integer> ANIOS_MANTENIMIENTO = List.of(2024, 2025);

    private final IWaterUserRepository waterUserRepository;
    private final IWaterUserAnnualPaymentRepository waterUserAnnualPaymentRepository;
    private final IFeeAmountRepository feeAmountRepository;
    private final IWaterReceiptPaymentRepository waterReceiptPaymentRepository;
    private final ICatalogOptionsRepository catalogOptionsRepository;
    private final IValorGeneralService valorGeneralService;
    private final IWaterUserChargeRepository waterUserChargeRepository;
    private final IRenunciaTemporalRepository renunciaTemporalRepository;

    // Todos los usuarios activos con adeudo de Luz > 0, desde ANIO_INICIO
    // hasta el año actual -- para la lista de "candidatos" a carta de adeudo.
    @Transactional(readOnly = true)
    public List<AdeudoLuzUsuarioDto> calcularParaTodos() {
        int anioActual = LocalDate.now().getYear();
        List<WaterUserEntity> usuarios = waterUserRepository.findAllActiveWithHouse()
                .stream()
                .filter(u -> u.getNoUsuario() != null && u.getNoUsuario() != 0)
                .collect(Collectors.toList());
        usuarios = excluirEnRenunciaTemporal(usuarios);
        return calcular(usuarios, ANIO_INICIO, anioActual);
    }

    // Solo para los usuarios indicados (para recalcular justo antes de
    // generar las cartas, con datos frescos -- nunca se confía en lo que
    // ya se le mostró antes a la pantalla).
    @Transactional(readOnly = true)
    public List<AdeudoLuzUsuarioDto> calcularParaUsuarios(List<Integer> aguaUsuarioIds) {
        int anioActual = LocalDate.now().getYear();
        List<WaterUserEntity> usuarios = waterUserRepository.findAllById(aguaUsuarioIds);
        return calcular(usuarios, ANIO_INICIO, anioActual);
    }

    // Solo los usuarios de una calle del catálogo -- para no calcular el
    // adeudo de TODOS los usuarios activos del sistema cada vez que se abre
    // la pantalla (lento: son varios años x todos los usuarios). Se filtra
    // ANTES de correr el cálculo, no después, para que el ahorro sea real.
    //
    // No todos los usuarios están asignados a una casa del catastro todavía
    // (censo en proceso) -- ver coincideCalle() para la regla exacta: si YA
    // tiene casa con calle de catálogo asignada, esa es la única fuente que
    // se usa (se respeta aunque no coincida); el texto libre de la
    // dirección (agua_direccion.calle) solo se usa como respaldo cuando NO
    // tiene casa asignada en absoluto. Antes se usaba el texto libre como
    // respaldo incluso teniendo casa, lo que causaba falsos positivos entre
    // zonas -- ver comentario de coincideCalle() (caso usuario 116).
    @Transactional(readOnly = true)
    public List<AdeudoLuzUsuarioDto> calcularParaCalle(Integer calleId) {
        int anioActual = LocalDate.now().getYear();
        CatalogOptionsEntity calle = catalogOptionsRepository.findById(calleId).orElse(null);
        String calleNombreLower = calle != null && calle.getNombre() != null ? calle.getNombre().toLowerCase() : null;

        List<WaterUserEntity> usuarios = waterUserRepository.findAllActiveWithHouse()
                .stream()
                .filter(u -> u.getNoUsuario() != null && u.getNoUsuario() != 0)
                .filter(u -> coincideCalle(u, calleId, calleNombreLower))
                .collect(Collectors.toList());
        usuarios = excluirEnRenunciaTemporal(usuarios);
        return calcular(usuarios, ANIO_INICIO, anioActual);
    }

    // Mientras un usuario tenga una renuncia temporal activa (Art. 6 Bis --
    // ver RenunciaTemporalEntity) no se le considera candidato a carta de
    // adeudo ni corte: dejó de usar el servicio, así que no debe seguir
    // acumulando avisos por falta de pago de luz durante ese periodo. Su
    // adeudo previo NO se borra -- solo se pausa la generación de cartas
    // nuevas hasta que se reconecte. No aplica a calcularParaUsuarios(), que
    // se usa para recalcular el adeudo de usuarios ya elegidos a mano (por
    // ejemplo, al registrar la renuncia misma, para el snapshot del PDF).
    private List<WaterUserEntity> excluirEnRenunciaTemporal(List<WaterUserEntity> usuarios) {
        if (usuarios.isEmpty()) {
            return usuarios;
        }
        List<Integer> ids = usuarios.stream().map(WaterUserEntity::getAguaUsuarioId).toList();
        Set<Integer> enRenuncia = renunciaTemporalRepository.findActivasPorUsuarios(ids)
                .stream()
                .map(r -> r.getWaterUser().getAguaUsuarioId())
                .collect(Collectors.toSet());
        if (enRenuncia.isEmpty()) {
            return usuarios;
        }
        return usuarios.stream()
                .filter(u -> !enRenuncia.contains(u.getAguaUsuarioId()))
                .collect(Collectors.toList());
    }

    // v9 (sept. 2026, bug reportado por Ely -- caso usuario 116, Jorge
    // Alberto Ramos Buzo): antes, si el usuario YA tenía casa con calle de
    // catálogo asignada pero esa calle NO era la buscada, el código de
    // todos modos caía a comparar contra el texto libre de
    // agua_direccion.calle -- y ese texto libre puede ser un registro viejo
    // que ya no refleja la realidad (Jorge Alberto tiene su casa bien
    // migrada en "Buenavista", pero le quedó una dirección libre vieja con
    // calle "Principal" y sección "Los Lopez" de antes de la migración).
    // Como también existe una calle "Principal" catalogada en la zona Los
    // López, hacía falso match y aparecía como candidato de una calle/zona
    // que ya no es la suya.
    //
    // Ahora la regla es: si el usuario YA tiene casa con calle de catálogo
    // asignada, esa es la fuente autoritativa y se respeta tal cual
    // (coincide o no, sin excepción) -- el texto libre SOLO se usa como
    // respaldo cuando el usuario no tiene casa asignada en absoluto (censo
    // todavía no migrado para él).
    //
    // v10 (sept. 2026, bug reportado por Ely -- "Principal" traía también
    // "Andador Principal"/"Andador principal"): el respaldo de texto libre
    // usaba contains(), que hace match parcial -- cualquier calle que
    // incluyera el texto buscado como substring entraba, aunque fuera una
    // calle distinta. Desde que el formulario de usuario dejó de tener
    // texto libre para "calle" (ahora es un select amarrado al catálogo, ver
    // new-user/details-user) y se normalizó agua_direccion.calle para que
    // coincidiera exacto con el catálogo, ya no hace falta (ni es correcto)
    // el match parcial: se compara exacto (solo mayúsculas/espacios, con
    // trim+lowercase, por datos capturados antes de la normalización).
    private boolean coincideCalle(WaterUserEntity user, Integer calleId, String calleNombreLower) {
        if (user.getWaterHouse() != null && user.getWaterHouse().getCatCalle() != null) {
            return calleId.equals(user.getWaterHouse().getCatCalle().getCatalogoOpcionesId());
        }
        if (calleNombreLower == null) {
            return false;
        }
        String direccionLibre = user.getAddress() != null ? user.getAddress().getCalle() : null;
        return direccionLibre != null && direccionLibre.trim().equalsIgnoreCase(calleNombreLower.trim());
    }

    private List<AdeudoLuzUsuarioDto> calcular(List<WaterUserEntity> usuarios, int anioDesde, int anioHasta) {
        // periodos adeudados y monto acumulado, por usuario -- se va llenando
        // año por año (igual patrón de precarga que DeudorService, pero
        // repetido para cada año del rango en vez de uno solo).
        Map<Integer, List<Integer>> periodosPorUsuario = new HashMap<>();
        Map<Integer, Double> totalPorUsuario = new HashMap<>();
        // Desglose año -> monto adeudado de ese año, por usuario -- para
        // poder mostrar "2023 - $840.00 | 2024 - $1,200.00 | ..." y así
        // comparar contra un cálculo manual y ubicar en qué año se está
        // desviando el total.
        Map<Integer, Map<Integer, Double>> montoPorAnioPorUsuario = new HashMap<>();
        // Pago de Luz más reciente por usuario (para "No. de recibo del
        // último pago" / "Fecha del último pago" en la carta) -- se va
        // actualizando según se recorre cada año, comparando por fecha de
        // pago real, no por el año del ciclo (un usuario puede adelantar o
        // atrasar pagos entre años).
        Map<Integer, WaterReceiptPaymentEntity> ultimoPagoPorUsuario = new HashMap<>();

        for (int anio = anioDesde; anio <= anioHasta; anio++) {
            Set<Integer> usuariosConAnioPagado = waterUserAnnualPaymentRepository
                    .findByAnioAndEstatus(anio, 1)
                    .stream()
                    .map(p -> p.getWaterUser().getAguaUsuarioId())
                    .collect(Collectors.toSet());

            List<WaterReceiptPaymentEntity> pagosLuzAnio = waterReceiptPaymentRepository
                    .findByConceptoAndAnio(CONCEPTO_LUZ_ID, anio);

            Map<Integer, Double> montoPagadoPorUsuario = pagosLuzAnio.stream()
                    .collect(Collectors.groupingBy(
                            p -> p.getWaterReceipt().getWaterUser().getAguaUsuarioId(),
                            Collectors.summingDouble(p -> p.getMontoAplicado() != null ? p.getMontoAplicado() : 0d)
                    ));

            for (WaterReceiptPaymentEntity pago : pagosLuzAnio) {
                Integer usuarioId = pago.getWaterReceipt().getWaterUser().getAguaUsuarioId();
                WaterReceiptPaymentEntity actual = ultimoPagoPorUsuario.get(usuarioId);
                if (actual == null || esPagoMasReciente(pago, actual)) {
                    ultimoPagoPorUsuario.put(usuarioId, pago);
                }
            }

            Map<Integer, Double> montoCuotaPorCuotaId = feeAmountRepository
                    .findByVigencia(anio)
                    .stream()
                    .filter(fa -> fa.getFee() != null)
                    .collect(Collectors.toMap(
                            fa -> fa.getFee().getCuotaId(),
                            fa -> fa.getCuota() != null ? fa.getCuota().doubleValue() : 0d,
                            (a, b) -> a
                    ));

            for (WaterUserEntity user : usuarios) {
                double montoCuotaAnio = user.getFee() != null
                        ? montoCuotaPorCuotaId.getOrDefault(user.getFee().getCuotaId(), 0d)
                        : 0d;
                if (montoCuotaAnio <= 0) {
                    continue;
                }

                boolean anioPagado = usuariosConAnioPagado.contains(user.getAguaUsuarioId());
                double montoDeuda;
                if (anioPagado) {
                    montoDeuda = 0d;
                } else {
                    double montoPagado = montoPagadoPorUsuario.getOrDefault(user.getAguaUsuarioId(), 0d);
                    montoDeuda = Math.max(0d, montoCuotaAnio - montoPagado);
                }

                if (montoDeuda > 0) {
                    periodosPorUsuario.computeIfAbsent(user.getAguaUsuarioId(), k -> new ArrayList<>()).add(anio);
                    totalPorUsuario.merge(user.getAguaUsuarioId(), montoDeuda, Double::sum);
                    montoPorAnioPorUsuario
                            .computeIfAbsent(user.getAguaUsuarioId(), k -> new TreeMap<>())
                            .put(anio, montoDeuda);
                }
            }
        }

        List<AdeudoLuzUsuarioDto> resultado = new ArrayList<>();
        for (WaterUserEntity user : usuarios) {
            List<Integer> periodos = periodosPorUsuario.get(user.getAguaUsuarioId());
            if (periodos == null || periodos.isEmpty()) {
                continue;
            }
            WaterHouseEntity casa = user.getWaterHouse();

            AdeudoLuzUsuarioDto dto = new AdeudoLuzUsuarioDto();
            dto.setAguaUsuarioId(user.getAguaUsuarioId());
            dto.setNoUsuario(user.getNoUsuario());
            dto.setNombreCompleto(buildNombreCompleto(user.getPerson()));
            dto.setCasaNo(casa != null ? casa.getCasaNo() : null);
            dto.setCasaNoTexto(buildCasaNoTexto(casa));
            // Si no tiene casa de catastro (censo en proceso), se cae en la
            // calle de su dirección libre para que igual se pueda ubicar
            // en la tabla y no salga en blanco.
            dto.setCalleNombre(casa != null && casa.getCatCalle() != null
                    ? casa.getCatCalle().getNombre()
                    : (user.getAddress() != null ? user.getAddress().getCalle() : null));
            dto.setEstatusComiteId(user.getEstatusComite() != null ? user.getEstatusComite().getCatalogoOpcionesId() : null);
            dto.setEstatusComiteNombre(user.getEstatusComite() != null ? user.getEstatusComite().getNombre() : null);
            dto.setDomicilioToma(buildDomicilio(user));
            dto.setPeriodosAdeudados(periodos);
            dto.setPeriodosAdeudadosTexto(formatearDesglosePorAnio(montoPorAnioPorUsuario.get(user.getAguaUsuarioId())));
            dto.setAdeudoTotal(totalPorUsuario.getOrDefault(user.getAguaUsuarioId(), 0d));

            WaterReceiptPaymentEntity ultimoPago = ultimoPagoPorUsuario.get(user.getAguaUsuarioId());
            LocalDateTime fechaUltimoPago = null;
            if (ultimoPago != null) {
                dto.setNoFolioUltimoPago(ultimoPago.getWaterReceipt().getNoFolio());
                fechaUltimoPago = ultimoPago.getFechaPago() != null
                        ? ultimoPago.getFechaPago()
                        : (ultimoPago.getWaterReceipt().getFecha() != null
                                ? ultimoPago.getWaterReceipt().getFecha().atStartOfDay()
                                : null);
                dto.setFechaUltimoPago(fechaUltimoPago);
            }

            // v8 (sept. 2026): ya no se descarta aquí a quien pagó hace
            // poco -- solo se calcula y se expone mesesSinPagoLuz, para que
            // la pantalla de candidatos decida con su propio filtro
            // ajustable (ver comentario de MESES_SIN_PAGO_NUNCA_PAGADO).
            dto.setMesesSinPagoLuz(fechaUltimoPago != null
                    ? (int) ChronoUnit.MONTHS.between(fechaUltimoPago.toLocalDate(), LocalDate.now())
                    : MESES_SIN_PAGO_NUNCA_PAGADO);

            // Interés moratorio (Art. 22): $ por día de atraso, calculado
            // automáticamente y sumado al adeudo total. Los días de atraso
            // se cuentan desde el último pago de luz registrado; si nunca ha
            // pagado, se cuentan desde el 1 de enero del primer año
            // adeudado (periodos ya viene ordenado ascendente). Es una
            // aproximación -- el reglamento cuenta el atraso desde el
            // vencimiento del plazo según la periodicidad de pago (Art. 11),
            // que no se tiene capturada por usuario; Ely debe validar contra
            // el expediente físico si el monto exacto importa para un caso.
            LocalDate fechaInicioAtraso = fechaUltimoPago != null
                    ? fechaUltimoPago.toLocalDate()
                    : LocalDate.of(periodos.get(0), 1, 1);
            long diasAtraso = Math.max(0, ChronoUnit.DAYS.between(fechaInicioAtraso, LocalDate.now()));
            double tasaInteresDia = valorGeneralService.getMontoVigente(ValorGeneralClave.INTERES_MORATORIO_DIA, anioHasta);
            double interesMoratorio = diasAtraso * tasaInteresDia;
            dto.setInteresMoratorio(interesMoratorio);
            dto.setAdeudoTotal(dto.getAdeudoTotal() + interesMoratorio);

            resultado.add(dto);
        }

        // Multa acumulada -- reutiliza el sistema de "Cargos/Multas" que ya
        // existe por usuario (WaterUserChargeEntity, panel en la ficha del
        // usuario), sumando el saldo pendiente (monto - pagado - condonado)
        // de TODOS los cargos activos excepto Mantenimiento de cajón (que
        // tiene su propio renglón aparte). Un solo query por lote en vez de
        // uno por usuario.
        if (!resultado.isEmpty()) {
            List<Integer> ids = resultado.stream().map(AdeudoLuzUsuarioDto::getAguaUsuarioId).toList();
            List<WaterUserChargeEntity> cargosActivos = waterUserChargeRepository
                    .findByEstatusAndWaterUser_AguaUsuarioIdIn(1, ids);

            Map<Integer, Double> multaAcumuladaPorUsuario = cargosActivos.stream()
                    .filter(c -> c.getConcepto() != null && !CONCEPTO_MANTENIMIENTO_NOMBRE.equals(c.getConcepto().getNombre()))
                    .filter(c -> c.getSaldo() > 0)
                    .collect(Collectors.groupingBy(
                            c -> c.getWaterUser().getAguaUsuarioId(),
                            Collectors.summingDouble(WaterUserChargeEntity::getSaldo)
                    ));
            for (AdeudoLuzUsuarioDto dto : resultado) {
                dto.setMultaAcumulada(multaAcumuladaPorUsuario.getOrDefault(dto.getAguaUsuarioId(), 0d));
            }

            // Desglose de "Multa acumulada" (v7 de la carta, sept. 2026,
            // pedido explícito de Ely): solo cuando hay 2 o más cargos
            // distintos sumando (con 1 solo cargo, el monto ya es
            // autoexplicativo y no vale la pena el detalle). Agrupa por
            // nombre de concepto, colapsando repetidos en "Concepto (xN)
            // $suma" pero sin perder el motivo (descripcion) de cada cargo
            // individual -- importante sobre todo para "Multa" genérica,
            // donde el monto solo no dice de qué se trata. Cuando el cargo
            // se marcó como aprobado por asamblea al capturarlo (ver
            // WaterUserChargeEntity.aprobadoAsamblea/fechaAsamblea), se le
            // agrega la leyenda "(aprobado por asamblea el dd/mm/aaaa)" --
            // o sin fecha si no se capturó una fecha exacta de esa asamblea.
            Map<Integer, List<WaterUserChargeEntity>> cargosMultaPorUsuario = cargosActivos.stream()
                    .filter(c -> c.getConcepto() != null && !CONCEPTO_MANTENIMIENTO_NOMBRE.equals(c.getConcepto().getNombre()))
                    .filter(c -> c.getSaldo() > 0)
                    .collect(Collectors.groupingBy(c -> c.getWaterUser().getAguaUsuarioId()));
            for (AdeudoLuzUsuarioDto dto : resultado) {
                List<WaterUserChargeEntity> cargosUsuario = cargosMultaPorUsuario.get(dto.getAguaUsuarioId());
                if (cargosUsuario != null && cargosUsuario.size() >= 2) {
                    dto.setMultaAcumuladaDesglose(buildMultaAcumuladaDesglose(cargosUsuario));
                }
            }

            // Mantenimiento de cajón, pendiente de pago -- calculado aparte
            // de multaAcumulada a propósito (v5 de la carta, sept. 2026): ya
            // no se suma al monto de "multa acumulada" de la carta, se
            // muestra junto en el mismo renglón pero con su propio desglose
            // por año (ver AvisoAdeudoPdfService), igual que el desglose de
            // adeudo de Luz.
            //
            // v11 (sept. 2026): ya NO se lee de cargos formales -- se
            // calcula en vivo, año por año, checando si el usuario ya dio
            // la cooperación "Mtto" (pago suelto en un recibo normal) para
            // cada año de ANIOS_MANTENIMIENTO que tenga en su desglose de
            // adeudo. Si ya existe ese pago, se da por cubierto; si no,
            // debe el monto vigente de ese año. Mismo criterio que la cuota
            // anual de Luz (findByConceptoAndAnio), sin cargo de por medio.
            for (AdeudoLuzUsuarioDto dto : resultado) {
                Map<Integer, Double> mantenimientoPorAnio = new TreeMap<>();
                List<Integer> periodos = dto.getPeriodosAdeudados();
                for (Integer anio : ANIOS_MANTENIMIENTO) {
                    if (periodos == null || !periodos.contains(anio)) {
                        continue;
                    }
                    boolean yaPagoMtto = waterReceiptPaymentRepository
                            .existsByConceptoAndAnioAndUsuario(CONCEPTO_MTTO_ID, anio, dto.getAguaUsuarioId());
                    if (yaPagoMtto) {
                        continue;
                    }
                    double monto = valorGeneralService.getMontoVigente(ValorGeneralClave.MANTENIMIENTO, anio);
                    if (monto > 0) {
                        mantenimientoPorAnio.put(anio, monto);
                    }
                }
                dto.setMantenimientoPorAnio(mantenimientoPorAnio);
                dto.setMantenimientoPendiente(mantenimientoPorAnio.values().stream().mapToDouble(Double::doubleValue).sum());
            }
        }

        resultado.sort((a, b) -> {
            String ca = a.getCalleNombre() != null ? a.getCalleNombre() : "";
            String cb = b.getCalleNombre() != null ? b.getCalleNombre() : "";
            int cmp = ca.compareTo(cb);
            if (cmp != 0) return cmp;
            Integer na = a.getCasaNo() != null ? a.getCasaNo() : 0;
            Integer nb = b.getCasaNo() != null ? b.getCasaNo() : 0;
            return na.compareTo(nb);
        });
        return resultado;
    }

    // Extrae el año del final de la descripción de un cargo de
    // mantenimiento ("Mantenimiento de cajón 2025" -> 2025) -- ese formato
    // exacto lo genera AvisoAdeudoService.crearCargoMantenimiento(), único
    // lugar donde se crea este concepto. Si algún día la descripción viene
    // distinta (ej. capturada a mano desde Cargos/Multas), se agrupa como
    // año 0 en vez de tronar, para no perder el monto del desglose.
    // Ver comentario donde se llama (calcular()) para el porqué de cada
    // regla. Orden estable (por fecha del cargo, luego por id) para que la
    // carta no cambie el orden de los conceptos entre una generación y otra
    // si nada cambió.
    private String buildMultaAcumuladaDesglose(List<WaterUserChargeEntity> cargos) {
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<WaterUserChargeEntity> ordenados = cargos.stream()
                .sorted(Comparator
                        .comparing(WaterUserChargeEntity::getFecha, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WaterUserChargeEntity::getAguaUsuarioCargoId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        Map<String, List<WaterUserChargeEntity>> porConcepto = new LinkedHashMap<>();
        for (WaterUserChargeEntity cargo : ordenados) {
            String nombre = cargo.getConcepto() != null && cargo.getConcepto().getNombre() != null
                    ? cargo.getConcepto().getNombre()
                    : "Cargo";
            porConcepto.computeIfAbsent(nombre, k -> new ArrayList<>()).add(cargo);
        }

        List<String> partes = new ArrayList<>();
        for (Map.Entry<String, List<WaterUserChargeEntity>> entry : porConcepto.entrySet()) {
            String nombre = entry.getKey();
            List<WaterUserChargeEntity> grupo = entry.getValue();
            double suma = grupo.stream().mapToDouble(WaterUserChargeEntity::getSaldo).sum();

            if (grupo.size() == 1) {
                WaterUserChargeEntity unico = grupo.get(0);
                String parte = nombre + " " + formatoMoneda.format(suma);
                String nota = notaAsamblea(unico, fechaFormatter);
                if (nota != null) {
                    parte += " " + nota;
                }
                partes.add(parte);
            } else {
                // Repetidos del mismo concepto (típicamente "Multa" genérica):
                // se colapsan en un solo renglón "(xN) $suma" pero cada
                // motivo (descripcion) se conserva, para no perder de qué
                // fue cada uno.
                String motivos = grupo.stream()
                        .map(cargo -> {
                            String motivo = cargo.getDescripcion() != null && !cargo.getDescripcion().isBlank()
                                    ? cargo.getDescripcion()
                                    : nombre;
                            String nota = notaAsamblea(cargo, fechaFormatter);
                            return nota != null ? motivo + " " + nota : motivo;
                        })
                        .collect(Collectors.joining("; "));
                partes.add(nombre + " (x" + grupo.size() + ") " + formatoMoneda.format(suma) + ": " + motivos);
            }
        }
        return String.join(" | ", partes);
    }

    // "(aprobado por asamblea el dd/mm/aaaa)" -- o sin fecha si el cargo se
    // marcó como aprobado pero no se capturó la fecha exacta de esa
    // asamblea. Null si el cargo no se marcó como aprobado por asamblea.
    private String notaAsamblea(WaterUserChargeEntity cargo, DateTimeFormatter fechaFormatter) {
        if (!Boolean.TRUE.equals(cargo.getAprobadoAsamblea())) {
            return null;
        }
        if (cargo.getFechaAsamblea() != null) {
            return "(aprobado por asamblea el " + cargo.getFechaAsamblea().format(fechaFormatter) + ")";
        }
        return "(aprobado por asamblea)";
    }

    // "2023 - $840.00 | 2024 - $1,200.00 | ..." -- desglose del adeudo total
    // por año, para poder comparar contra un cálculo manual y ver en qué
    // año se está desviando. Se reutiliza la misma columna que antes solo
    // traía la lista de años (periodosAdeudadosTexto), ahora con el monto
    // de cada uno.
    private String formatearDesglosePorAnio(Map<Integer, Double> montoPorAnio) {
        if (montoPorAnio == null || montoPorAnio.isEmpty()) {
            return "";
        }
        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        return montoPorAnio.entrySet().stream()
                .map(e -> e.getKey() + " - " + formatoMoneda.format(e.getValue()))
                .collect(Collectors.joining(" | "));
    }

    // "2-D" / "2-I" -- número de casa + lado (Derecho/Izquierdo), cuando la
    // casa lo tiene capturado (WaterHouseEntity.lado, catálogo libre 'D'/'I'
    // que ya usa el módulo de Casas). Si no hay lado, se deja solo el número.
    private String buildCasaNoTexto(WaterHouseEntity casa) {
        if (casa == null || casa.getCasaNo() == null) {
            return "";
        }
        String lado = casa.getLado();
        return lado != null && !lado.isBlank() ? casa.getCasaNo() + "-" + lado : String.valueOf(casa.getCasaNo());
    }

    // "Calle #número" -- mismo criterio que ya usa WaterUserBasicDto
    // (CONCAT(a.calle, ' #', a.numero)) para armar un domicilio de una
    // sola línea. Si el usuario no tiene dirección libre capturada, se cae
    // en la calle/número de la casa del catastro como respaldo.
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

    // true si "candidato" es más reciente que "actual" -- por fecha de pago
    // real; si a alguno le falta la fecha (pagos históricos capturados sin
    // ese dato), se cae en la fecha del recibo como respaldo.
    private boolean esPagoMasReciente(WaterReceiptPaymentEntity candidato, WaterReceiptPaymentEntity actual) {
        LocalDateTime fechaCandidato = fechaEfectiva(candidato);
        LocalDateTime fechaActual = fechaEfectiva(actual);
        if (fechaCandidato == null) return false;
        if (fechaActual == null) return true;
        return fechaCandidato.isAfter(fechaActual);
    }

    private LocalDateTime fechaEfectiva(WaterReceiptPaymentEntity pago) {
        if (pago.getFechaPago() != null) {
            return pago.getFechaPago();
        }
        if (pago.getWaterReceipt() != null && pago.getWaterReceipt().getFecha() != null) {
            return pago.getWaterReceipt().getFecha().atStartOfDay();
        }
        return null;
    }

    private String buildNombreCompleto(PersonEntity person) {
        if (person == null) return "";
        return String.join(" ",
                        nullToEmpty(person.getNombre()), nullToEmpty(person.getNombre2()),
                        nullToEmpty(person.getApp()), nullToEmpty(person.getApm()))
                .replaceAll("\\s+", " ").trim();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
