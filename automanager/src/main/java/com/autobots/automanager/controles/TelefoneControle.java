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
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelos.AdicionadorLinkTelefone;
import com.autobots.automanager.modelos.TelefoneAtualizador;
import com.autobots.automanager.modelos.TelefoneSelecionador;
import com.autobots.automanager.repositorios.TelefoneRepositorio;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController // indica que essa classe é um controlador rest do spring
public class TelefoneControle {
    @Autowired // injeta o repositório de telefone
    private TelefoneRepositorio repositorio;
    @Autowired // injeta o selecionador de telefone
    private TelefoneSelecionador selecionador;
    @Autowired // injeta o adicionador de links hateoas
    private AdicionadorLinkTelefone adicionadorLink;
    @Autowired // injeta o repositório de cliente
    private ClienteRepositorio clienteRepositorio;

    @GetMapping("/telefone/{id}") // endpoint para buscar telefone por id (do telefone)
    public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
        List<Telefone> telefones = repositorio.findAll(); // pega todos os telefones do banco
        Telefone telefone = selecionador.selecionar(telefones, id); // seleciona o telefone pelo id
        if (telefone == null) { // se não achar, retorna 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLink.adicionarLink(telefones); // adiciona links hateoas
            return new ResponseEntity<>(telefone, HttpStatus.OK); // retorna o telefone e status 200
        }
    }

    @GetMapping("/telefones/cliente/{id}") //endpoint pra buscar telefone por id (do cliente)
    public ResponseEntity<List<Telefone>> listarTelefonesPorCliente(@PathVariable Long id) {
        var clienteOpt = clienteRepositorio.findById(id); // busca o cliente pelo id
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            List<Telefone> telefones = cliente.getTelefones(); // pega a lista de telefones do cliente
            if (telefones.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                adicionadorLink.adicionarLink(telefones); // adiciona links hateoas
                return new ResponseEntity<>(telefones, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // cliente não encontrado
    }

    @GetMapping("/telefones") // endpoint para listar todos os telefones
    public ResponseEntity<List<Telefone>> obterTelefones() {
        List<Telefone> telefones = repositorio.findAll(); // pega todos os telefones
        if (telefones.isEmpty()) { // se não tiver nenhum, retorna 404
            ResponseEntity<List<Telefone>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return resposta;
        } else {
            adicionadorLink.adicionarLink(telefones); // adiciona links hateoas
            return new ResponseEntity<>(telefones, HttpStatus.FOUND); // retorna lista e status 302
        }
    }

    @PostMapping("/telefone/cadastro/cliente/{id}") // endpoint para cadastrar  (pro cliente)
    public ResponseEntity<?> cadastrarTelefone(@PathVariable long id, @RequestBody Telefone telefone) {
        var clienteOpt= clienteRepositorio.findById(id); // busca cliente pelo id
        if (clienteOpt.isPresent()){
            Cliente cliente = clienteOpt.get();
            cliente.getTelefones().add(telefone); // adiciona telefone na lista de telefones do cliente
            clienteRepositorio.save(cliente); // salva o cliente com o novo telefone
            return new ResponseEntity<>(HttpStatus.CREATED); // retorna status 201
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // se não achar o cliente, retorna bad request
    }

    @PutMapping("/telefone/atualizar") // endpoint para atualizar telefone (pelo id do telefone)
    public ResponseEntity<?> atualizarTelefone(@RequestBody Telefone atualizacao) {
        HttpStatus status = HttpStatus.CONFLICT; // status padrão é conflito
        Telefone telefone = repositorio.getById(atualizacao.getId()); // busca telefone pelo id
        if (telefone != null) { // se achar, atualiza
            TelefoneAtualizador atualizador = new TelefoneAtualizador(); // cria atualizador
            atualizador.atualizar(telefone, atualizacao); // atualiza dados
            repositorio.save(telefone); // salva no banco
            status = HttpStatus.OK; // muda status para ok
        } else {
            status = HttpStatus.BAD_REQUEST; // se não achar, retorna bad request
        }
        return new ResponseEntity<>(status); // retorna status
    }

    @DeleteMapping("/telefone/excluir") // endpoint para excluir telefone pelo id do telefone
    public ResponseEntity<?> excluirTelefone(@RequestBody Telefone exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // status padrão é bad request
        if (exclusao.getId() != null) { // só tenta excluir se o id não for nulo
            var telefoneOpt = repositorio.findById(exclusao.getId()); // busca telefone pelo id
            if (telefoneOpt.isPresent()) { // se achar o telefone
                Telefone telefone = telefoneOpt.get();
                // remove o telefone da lista de todos os clientes que possuem ele
                List<Cliente> clientes = clienteRepositorio.findAll();
                for (Cliente cliente : clientes) {
                    if (cliente.getTelefones().removeIf(t -> t.getId().equals(telefone.getId()))) {
                        clienteRepositorio.save(cliente); // salva cliente sem o telefone
                    }
                }
                repositorio.delete(telefone); // deleta o telefone do banco
                status = HttpStatus.OK; // muda status para ok
            }
        }
        return new ResponseEntity<>(status); // retorna status
    }
}