package com.autobots.automanager.modelos;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.entidades.Documento;

@Component
public class AdicionadorLinkDocumento implements AdicionadorLink<Documento> {

	@Override
	public void adicionarLink(List<Documento> lista) {
		for (Documento documento : lista) {
			long id = documento.getId();
            Link selfLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(DocumentoControle.class).obterDocumento(id))
                    .withSelfRel();
            Link allLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(DocumentoControle.class).obterDocumentos())
                    .withRel("documentos");
            Link updateLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(DocumentoControle.class).atualizarDocumento(documento))
                    .withRel("atualizar");
            Link deleteLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(DocumentoControle.class).excluirDocumento(documento))
                    .withRel("excluir");
            documento.add(selfLink, allLink, updateLink, deleteLink);
        }
    }

    @Override
    public void adicionarLink(Documento objeto) {
        adicionarLink(List.of(objeto)); // reaproveita o método acima
    }
}