package com.autobots.automanager.modelos;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.entidades.Telefone;

@Component
public class AdicionadorLinkTelefone implements AdicionadorLink<Telefone> {

	@Override
	public void adicionarLink(List<Telefone> lista) {
		for (Telefone telefone : lista) {
			long id = telefone.getId();
            Link selfLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(TelefoneControle.class).obterTelefone(id))
                    .withSelfRel();
            Link allLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(TelefoneControle.class).obterTelefones())
                    .withRel("telefones");
            Link updateLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(TelefoneControle.class).atualizarTelefone(telefone))
                    .withRel("atualizar");
            Link deleteLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(TelefoneControle.class).excluirTelefone(telefone))
                    .withRel("excluir");
            telefone.add(selfLink, allLink, updateLink, deleteLink);
        }
    }

    @Override
    public void adicionarLink(Telefone objeto) {
        adicionarLink(List.of(objeto)); // reaproveita o método acima
    }
}