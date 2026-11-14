package com.condominio.patrones;

import com.condominio.patrones.strategy.CalculadoraGastos;
import com.condominio.patrones.strategy.ProrrateoEquitativo;
import com.condominio.patrones.strategy.ProrrateoPorAlicuota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StrategyTest {

    @Test
    @DisplayName("Debe calcular el prorrateo proporcional al área ocupada (Por Alícuota)")
    public void debeCalcularProrrateoPorAlicuota() {
        CalculadoraGastos calculadora = new CalculadoraGastos(new ProrrateoPorAlicuota());

        double gastoAguaTotal = 1000.0;
        double areaDepto = 100.0;
        double areaTotalCondominio = 1000.0; // 10%

        double cuotaCalculada = calculadora.ejecutarCalculo(gastoAguaTotal, areaDepto, areaTotalCondominio);

        assertEquals(100.0, cuotaCalculada, 0.001);
    }

    @Test
    @DisplayName("Debe calcular el prorrateo equitativo dividiendo entre el total de unidades")
    public void debeCalcularProrrateoEquitativo() {
        CalculadoraGastos calculadora = new CalculadoraGastos(new ProrrateoEquitativo());

        double gastoLimpiezaTotal = 2500.0;
        double totalDepartamentos = 10.0;

        double cuotaCalculada = calculadora.ejecutarCalculo(gastoLimpiezaTotal, 1.0, totalDepartamentos);

        assertEquals(250.0, cuotaCalculada, 0.001);
    }

    @Test
    @DisplayName("Debe permitir cambiar dinámicamente de estrategia de prorrateo en tiempo de ejecución")
    public void debeCambiarEstrategiaEnTiempoDeEjecucion() {
        CalculadoraGastos calculadora = new CalculadoraGastos(new ProrrateoPorAlicuota());

        double cuotaAlicuota = calculadora.ejecutarCalculo(1000.0, 80.0, 800.0); // 10% = 100.0
        assertEquals(100.0, cuotaAlicuota, 0.001);

        // Cambio a estrategia equitativa
        calculadora.setEstrategia(new ProrrateoEquitativo());
        double cuotaEquitativa = calculadora.ejecutarCalculo(1000.0, 1.0, 5.0); // 1000 / 5 = 200.0
        assertEquals(200.0, cuotaEquitativa, 0.001);
    }
}
