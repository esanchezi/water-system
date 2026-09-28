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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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

    // Regla para ser candidato a carta de adeudo: no basta con deber algo --
    // tiene que llevar al menos este número de meses SIN dar ningún pago de
    // luz (igual que ya dice el texto de la carta: "La multa por falta de
    // pago aplica cuando se tienen al menos 3 meses sin realizar el pago").
    // Si abonó algo hace menos de 3 meses (aunque su saldo siga positivo),
    // no se le manda carta todavía.
    private static final long MESES_SIN_PAGO_PARA_CANDIDATO = 3;

    // Conceptos del catálogo CONCEPTO_CARGO_EXTRA (mismo catálogo que usa
    // el panel "Cargos/Multas" de la ficha del usuario, ver
    // WaterUserChargeEntity) que cuentan como "multa acumulada" en la carta
    // de adeudo -- se identifican por nombre porque el catálogo lo
    // administra el Comité desde la pantalla de Catálogos, no hay un ID
    // fijo. Deben escribirse EXACTAMENTE igual a como se dieron de alta ahí
    // (ver CLAVES_VALOR_GENERAL en el frontend, que sugiere el mismo texto).
    // Excluye a propósito los conceptos ya existentes de mano de obra,
    // insumos, bastón, llave azul y reparaciones -- esos no son parte de
    // los montos que la carta de adeudo debe reflejar. "Mantenimiento de
    // cajón" también se excluye a propósito (v5 de la carta, sept. 2026):
    // ese adeudo ya NO se incluye en el monto de la carta -- se le informa
    // al usuario aparte cuando se presenta (ver nota1 en
    // AvisoAdeudoPdfService). El cargo se sigue generando igual (tarea
    // #153) y sigue contando para el total de otros módulos (ficha de
    // usuario, Deudores) -- este Set solo controla qué entra en el número
    // que se imprime en ESTA carta específica.
    private static final Set<String> CONCEPTOS_MULTA_ACUMULADA = Set.of(
            "Multa por falta de pago", "Corte y reconexión", "Aviso", "Multa por manipular válvulas"
    );

    // Mismo nombre exacto que usa AvisoAdeudoService.CONCEPTO_MANTENIMIENTO_NOMBRE
    // al crear el cargo -- se calcula aparte de CONCEPTOS_MULTA_ACUMULADA
    // para mostrarse en su propio renglón de la carta (ver mantenimientoPendiente).
    private static final String CONCEPTO_MANTENIMIENTO_NOMBRE = "Mantenimiento de cajón";

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
    // (censo en proceso) -- por eso un usuario cuenta como de esta calle si
    // CUALQUIERA de estos dos aplica (mismo criterio que ya usa el módulo de
    // Usuarios para su cascada Sección->Calle): su casa tiene esta calle de
    // catálogo asignada, O su dirección libre (agua_direccion.calle, texto
    // capturado a mano) contiene el nombre de esta calle.
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

    private boolean coincideCalle(WaterUserEntity user, Integer calleId, String calleNombreLower) {
        boolean porCasa = user.getWaterHouse() != null
                && user.getWaterHouse().getCatCalle() != null
                && calleId.equals(user.getWaterHouse().getCatCalle().getCatalogoOpcionesId());
        if (porCasa) {
            return true;
        }
        if (calleNombreLower == null) {
            return false;
        }
        String direccionLibre = user.getAddress() != null ? user.getAddress().getCalle() : null;
        return direccionLibre != null && direccionLibre.toLowerCase().contains(calleNombreLower);
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

            // Regla: solo es candidato a carta si lleva al menos
            // MESES_SIN_PAGO_PARA_CANDIDATO meses sin dar NINGÚN pago de luz.
            // Si nunca ha pagado (fechaUltimoPago == null), sí califica de
            // inmediato -- no hay pago reciente que lo excuse.
            if (fechaUltimoPago != null) {
                long mesesSinPago = ChronoUnit.MONTHS.between(fechaUltimoPago.toLocalDate(), LocalDate.now());
                if (mesesSinPago < MESES_SIN_PAGO_PARA_CANDIDATO) {
                    continue;
                }
            }

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
        // usuario), filtrando solo los conceptos de CONCEPTOS_MULTA_ACUMULADA
        // y sumando el saldo pendiente (monto - pagado - condonado) de cada
        // uno. Un solo query por lote en vez de uno por usuario.
        if (!resultado.isEmpty()) {
            List<Integer> ids = resultado.stream().map(AdeudoLuzUsuarioDto::getAguaUsuarioId).toList();
            List<WaterUserChargeEntity> cargosActivos = waterUserChargeRepository
                    .findByEstatusAndWaterUser_AguaUsuarioIdIn(1, ids);

            Map<Integer, Double> multaAcumuladaPorUsuario = cargosActivos.stream()
                    .filter(c -> c.getConcepto() != null && CONCEPTOS_MULTA_ACUMULADA.contains(c.getConcepto().getNombre()))
                    .filter(c -> c.getSaldo() > 0)
                    .collect(Collectors.groupingBy(
                            c -> c.getWaterUser().getAguaUsuarioId(),
                            Collectors.summingDouble(WaterUserChargeEntity::getSaldo)
                    ));
            for (AdeudoLuzUsuarioDto dto : resultado) {
                dto.setMultaAcumulada(multaAcumuladaPorUsuario.getOrDefault(dto.getAguaUsuarioId(), 0d));
            }

            // Mantenimiento de cajón, pendiente de pago -- calculado aparte
            // de multaAcumulada a propósito (v5 de la carta, sept. 2026): ya
            // no se suma al monto de "multa acumulada" de la carta, se
            // muestra junto en el mismo renglón pero con su propio desglose
            // por año (ver AvisoAdeudoPdfService), igual que el desglose de
            // adeudo de Luz.
            List<WaterUserChargeEntity> cargosMantenimiento = cargosActivos.stream()
                    .filter(c -> c.getConcepto() != null && CONCEPTO_MANTENIMIENTO_NOMBRE.equals(c.getConcepto().getNombre()))
                    .filter(c -> c.getSaldo() > 0)
                    .toList();

            Map<Integer, Double> mantenimientoPorUsuario = cargosMantenimiento.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getWaterUser().getAguaUsuarioId(),
                            Collectors.summingDouble(WaterUserChargeEntity::getSaldo)
                    ));

            // Agrupa además por año (parseado del final de la descripción,
            // ej. "Mantenimiento de cajón 2025" -> 2025 -- así se generó en
            // AvisoAdeudoService.crearCargoMantenimiento(), es el único
            // lugar donde se crea este concepto) para armar el texto
            // "$100.00 - 2024 | $100.00 - 2025".
            Map<Integer, Map<Integer, Double>> mantenimientoPorAnioPorUsuario = cargosMantenimiento.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getWaterUser().getAguaUsuarioId(),
                            Collectors.groupingBy(this::anioDeDescripcionMantenimiento,
                                    TreeMap::new,
                                    Collectors.summingDouble(WaterUserChargeEntity::getSaldo))
                    ));

            for (AdeudoLuzUsuarioDto dto : resultado) {
                dto.setMantenimientoPendiente(mantenimientoPorUsuario.getOrDefault(dto.getAguaUsuarioId(), 0d));
                dto.setMantenimientoPorAnio(mantenimientoPorAnioPorUsuario.getOrDefault(dto.getAguaUsuarioId(), new TreeMap<>()));
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
    private Integer anioDeDescripcionMantenimiento(WaterUserChargeEntity cargo) {
        String descripcion = cargo.getDescripcion();
        if (descripcion == null || descripcion.length() < 4) {
            return 0;
        }
        String posibleAnio = descripcion.substring(descripcion.length() - 4);
        try {
            return Integer.parseInt(posibleAnio);
        } catch (NumberFormatException e) {
            return 0;
        }
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
