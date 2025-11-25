package com.autobots.automanager.controles;

// importa listas
import java.util.List;

// importa anotações e classes do spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// importa entidades e modelos do projeto
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelos.AdicionadorLinkDocumento;
import com.autobots.automanager.modelos.DocumentoAtualizador;
import com.autobots.automanager.modelos.DocumentoSelecionador;
import com.autobots.automanager.repositorios.DocumentoRepositorio;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController // indica que essa classe é um controlador rest do spring
public class DocumentoControle {
    @Autowired // injeta o repositório de documento
    private DocumentoRepositorio repositorio;
    @Autowired // injeta o selecionador de documento
    private DocumentoSelecionador selecionador;
    @Autowired // injeta o adicionador de links hateoas
    private AdicionadorLinkDocumento adicionadorLink;
    @Autowired // injeta o repositório de cliente
    private ClienteRepositorio clienteRepositorio;

    @GetMapping("/documento/{id}") // endpoint para buscar documento por id (do documento)
    public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
        List<Documento> documentos = repositorio.findAll(); // pega todos os documentos do banco
        Documento documento = selecionador.selecionar(documentos, id); // seleciona o documento pelo id
        if (documento == null) { // se não achar, retorna 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLink.adicionarLink(documento); // adiciona links hateoas
            return new ResponseEntity<>(documento, HttpStatus.OK); // retorna o documento e status 200
        }
    }

    @GetMapping("/documentos/cliente/{id}") //endpoint pra buscar documentos por id (do cliente)
    public ResponseEntity<List<Documento>> listarDocumentosPorCliente(@PathVariable Long id) {
        var clienteOpt = clienteRepositorio.findById(id); // busca o cliente pelo id
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            List<Documento> documentos = cliente.getDocumentos(); // pega a lista de documentos do cliente
            if (documentos.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                adicionadorLink.adicionarLink(documentos); // adiciona links hateoas
                return new ResponseEntity<>(documentos, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // cliente não encontrado
    }

    @GetMapping("/documentos") // endpoint para listar todos os documentos
    public ResponseEntity<List<Documento>> obterDocumentos() {
        List<Documento> documentos = repositorio.findAll(); // pega todos os documentos
        if (documentos.isEmpty()) { // se não tiver nenhum, retorna 404
            ResponseEntity<List<Documento>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return resposta;
        } else {
            adicionadorLink.adicionarLink(documentos); // adiciona links hateoas
            return new ResponseEntity<>(documentos, HttpStatus.OK); // retorna lista e status 200
        }
    }

    @PostMapping("/documento/cadastro/cliente/{id}") //endpoint para cadastrar  (pro cliente)
    public ResponseEntity<?> cadastrarDocumento(@PathVariable long id, @RequestBody Documento documento) {
        var clienteOpt= clienteRepositorio.findById(id); //busca cliente pelo id
        if (clienteOpt.isPresent()){ //cliente optional
            Cliente cliente = clienteOpt.get();
            cliente.getDocumentos().add(documento); //adiciona documento na lista de documentos do cliente
            clienteRepositorio.save(cliente); //salva o cliente com o novo documento
            return new ResponseEntity<>(HttpStatus.CREATED); // retorna status 201
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // se não achar o cliente, retorna bad request
    }

    @PutMapping("/documento/atualizar") // endpoint para atualizar documento (pelo id do documento)
    public ResponseEntity<?> atualizarDocumento(@RequestBody Documento atualizacao) {
        HttpStatus status = HttpStatus.CONFLICT; // status padrão é conflito
        Documento documento = repositorio.getById(atualizacao.getId()); // busca documento pelo id
        if (documento != null) { // se achar, atualiza
            DocumentoAtualizador atualizador = new DocumentoAtualizador(); // cria atualizador
            atualizador.atualizar(documento, atualizacao); // atualiza dados
            repositorio.save(documento); // salva no banco
            status = HttpStatus.OK; // muda status para ok
        } else {
            status = HttpStatus.BAD_REQUEST; // se não achar, retorna bad request
        }
        return new ResponseEntity<>(status); // retorna status
    }

    @DeleteMapping("/documento/excluir") // endpoint para excluir documento pelo id do documento
    public ResponseEntity<?> excluirDocumento(@RequestBody Documento exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // status padrão é bad request
        if (exclusao.getId() != null) { // só tenta excluir se o id não for nulo
            var documentoOpt = repositorio.findById(exclusao.getId()); // busca documento pelo id
            if (documentoOpt.isPresent()) { // se achar o documento
                Documento documento = documentoOpt.get();
                // remove o documento da lista de todos os clientes que possuem ele
                List<Cliente> clientes = clienteRepositorio.findAll();
                for (Cliente cliente : clientes) {
                    if (cliente.getDocumentos().removeIf(d -> d.getId().equals(documento.getId()))) {
                        clienteRepositorio.save(cliente); // salva cliente sem o documento
                    }
                }
                repositorio.delete(documento); // deleta o documento do banco
                status = HttpStatus.OK; // muda status para ok
            }
        }
        return new ResponseEntity<>(status); // retorna status
    }
}