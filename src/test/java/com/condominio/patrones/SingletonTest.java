
package com.condominio.patrones;

import com.condominio.patrones.singleton.ConfiguracionCondominio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class SingletonTest {

    @Test
    void debeRetornarSiempreLaMismaInstancia() {

        ConfiguracionCondominio primera =
                ConfiguracionCondominio.obtenerInstancia();

        ConfiguracionCondominio segunda =
                ConfiguracionCondominio.obtenerInstancia();

        assertSame(primera, segunda);
    }

    @Test
    void debeVerificarParametrosInmutablesDeCobranza() {
        ConfiguracionCondominio config = ConfiguracionCondominio.obtenerInstancia();
        org.junit.jupiter.api.Assertions.assertEquals(1.5, config.getTasaInteresMora());
        org.junit.jupiter.api.Assertions.assertEquals(3, config.getDiasGracia());
        org.junit.jupiter.api.Assertions.assertEquals(5, config.getDiasParaMora());
    }
}
