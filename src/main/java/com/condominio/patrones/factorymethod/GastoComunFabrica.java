package com.condominio.patrones.factorymethod;

import com.condominio.modelo.financiero.GastoAgua;
import com.condominio.modelo.financiero.GastoComun;
import com.condominio.modelo.financiero.GastoLimpieza;
import com.condominio.modelo.financiero.GastoLuz;
import com.condominio.modelo.inmuebles.Condominio;
import java.time.LocalDate;

// Fabrica centralizada para la creacion de subtipos de GastoComun (Patron Factory Method - Semana 7)
public class GastoComunFabrica {

    public static GastoAgua crearGastoAgua(Long id, Double monto, LocalDate fecha, String descripcion,
                                          Condominio condominio, Double lecturaActual, Double lecturaAnterior) {
        return GastoAgua.builder()
                .id(id)
                .monto(monto)
                .fecha(fecha)
                .descripcion(descripcion)
                .condominio(condominio)
                .lecturaActual(lecturaActual)
                .lecturaAnterior(lecturaAnterior)
                .build();
    }

    public static GastoLuz crearGastoLuz(Long id, Double monto, LocalDate fecha, String descripcion,
                                        Condominio condominio, Double consumoKwh) {
        return GastoLuz.builder()
                .id(id)
                .monto(monto)
                .fecha(fecha)
                .descripcion(descripcion)
                .condominio(condominio)
                .consumoKwh(consumoKwh)
                .build();
    }

    public static GastoLimpieza crearGastoLimpieza(Long id, Double monto, LocalDate fecha, String descripcion,
                                                  Condominio condominio, String contratoEmpresa) {
        return GastoLimpieza.builder()
                .id(id)
                .monto(monto)
                .fecha(fecha)
                .descripcion(descripcion)
                .condominio(condominio)
                .contratoEmpresa(contratoEmpresa)
                .build();
    }

    public static GastoComun crearGasto(TipoGasto tipo, Long id, Double monto, LocalDate fecha,
                                       String descripcion, Condominio condominio) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de gasto no puede ser nulo");
        }
        return switch (tipo) {
            case AGUA -> crearGastoAgua(id, monto, fecha, descripcion, condominio, 0.0, 0.0);
            case LUZ -> crearGastoLuz(id, monto, fecha, descripcion, condominio, 0.0);
            case LIMPIEZA -> crearGastoLimpieza(id, monto, fecha, descripcion, condominio, "CONTRATO-GENERAL");
        };
    }
}
