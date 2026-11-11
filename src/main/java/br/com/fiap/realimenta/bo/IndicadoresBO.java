package br.com.fiap.realimenta.bo;

import br.com.fiap.realimenta.dao.IndicadoresDAO;
import br.com.fiap.realimenta.model.Indicadores;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndicadoresBO {

    private final IndicadoresDAO indicadoresDAO;

    public IndicadoresBO(IndicadoresDAO indicadoresDAO) {
        this.indicadoresDAO = indicadoresDAO;
    }

    @Transactional(readOnly = true)
    public Indicadores consultar() {
        return indicadoresDAO.consultar();
    }
}
