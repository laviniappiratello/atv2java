package com.autobots.automanager.modelos; 

import java.util.List; 

import org.springframework.stereotype.Component; //importa annotation component do spring

import com.autobots.automanager.entidades.Documento; //importa a entidade documento

@Component //diz pro spring gerenciar essa classe como um bean
public class DocumentoSelecionador { //classe que eu uso pra escolher um documento de uma lista
    public Documento selecionar(List<Documento> documentos, long id) { //metodo que recebe uma lista de documentos e um id
        Documento selecionado = null; //crio uma variavel pra guardar o documento achado, começa nulo
        for (Documento documento : documentos) { // pra cada documento na lista
            if (documento.getId() == id) { // se o id do documento for igual ao id que recebi
                selecionado = documento; //guardo esse documento como selecionado
            }
        }
        return selecionado; //depois do for, retorno o documento achado ou nulo se não achou
    }
}