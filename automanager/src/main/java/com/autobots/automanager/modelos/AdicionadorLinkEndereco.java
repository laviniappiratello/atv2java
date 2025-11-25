package com.autobots.automanager.modelos;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.entidades.Endereco;

@Component
public class AdicionadorLinkEndereco implements AdicionadorLink<Endereco> {

	@Override
	public void adicionarLink(List<Endereco> lista) {
		for (Endereco endereco : lista) {
			long id = endereco.getId();
            Link selfLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(EnderecoControle.class).obterEndereco(id))
                    .withSelfRel();
            Link allLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(EnderecoControle.class).obterEnderecos())
                    .withRel("enderecos");
            Link updateLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(EnderecoControle.class).atualizarEndereco(endereco))
                    .withRel("atualizar");
            Link deleteLink = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(EnderecoControle.class).excluirEndereco(endereco))
                    .withRel("excluir");
            endereco.add(selfLink, allLink, updateLink, deleteLink);
        }
    }

    @Override
    public void adicionarLink(Endereco objeto) {
        adicionarLink(List.of(objeto)); // reaproveita o método acima
    }
}