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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.modelos.AdicionadorLinkEndereco;
import com.autobots.automanager.modelos.EnderecoAtualizador;
import com.autobots.automanager.modelos.EnderecoSelecionador;
import com.autobots.automanager.repositorios.EnderecoRepositorio;
import com.autobots.automanager.repositorios.ClienteRepositorio;

@RestController // indica que essa classe é um controlador rest do spring
public class EnderecoControle {
    @Autowired // injeta o repositório de endereço
    private EnderecoRepositorio repositorio;
    @Autowired // injeta o selecionador de endereço
    private EnderecoSelecionador selecionador;
    @Autowired // injeta o adicionador de links hateoas
    private AdicionadorLinkEndereco adicionadorLink;
    @Autowired // injeta o repositório de cliente
    private ClienteRepositorio clienteRepositorio;

    @GetMapping("/endereco/{id}") // endpoint para buscar endereço por id (do endereço)
    public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
        List<Endereco> enderecos = repositorio.findAll(); // pega todos os endereços do banco
        Endereco endereco = selecionador.selecionar(enderecos, id); // seleciona o endereço pelo id
        if (endereco == null) { // se não achar, retorna 404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            adicionadorLink.adicionarLink(endereco); // adiciona links hateoas
            return new ResponseEntity<>(endereco, HttpStatus.OK); // retorna o endereço e status 200
        }
    }

    @GetMapping("/endereco/cliente/{id}") //endpoint pra buscar endereços por id (do cliente)
    public ResponseEntity<List<Endereco>> listarEnderecosPorCliente(@PathVariable Long id, @RequestBody Endereco endereco) {
        var clienteOpt = clienteRepositorio.findById(id); // busca o cliente pelo id
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            cliente.setEndereco(endereco);
            clienteRepositorio.save(cliente);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // cliente não encontrado
    }

    @GetMapping("/enderecos") // endpoint para listar todos os endereços
    public ResponseEntity<List<Endereco>> obterEnderecos() {
        List<Endereco> enderecos = repositorio.findAll(); // pega todos os endereços
        if (enderecos.isEmpty()) { // se não tiver nenhum, retorna 404
            ResponseEntity<List<Endereco>> resposta = new ResponseEntity<>(HttpStatus.NOT_FOUND);
            return resposta;
        } else {
            adicionadorLink.adicionarLink(enderecos); // adiciona links hateoas
            return new ResponseEntity<>(enderecos, HttpStatus.OK); // retorna lista e status 200
        }
    }

    @PostMapping("/endereco/cadastro/cliente/{id}") // endpoint para cadastrar  (pro cliente)
    public ResponseEntity<?> cadastrarOuAtuaEndereco(@PathVariable long id, @RequestBody Endereco endereco) {
        //como o endereco eh unico o post vai atualiza caso exista ou cadastrar caso n exista
        var clienteOpt= clienteRepositorio.findById(id); // busca cliente pelo id
        if (clienteOpt.isPresent()){
            Cliente cliente = clienteOpt.get();
            cliente.setEndereco(endereco); // adiciona ou atualiza endereço 
            clienteRepositorio.save(cliente); // salva o cliente com o novo endereço
            return new ResponseEntity<>(HttpStatus.CREATED); // retorna status 201
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // se não achar o cliente, retorna bad request
    }

    @PutMapping("/endereco/atualizar") // endpoint para atualizar endereço (pelo id do endereço)
    public ResponseEntity<?> atualizarEndereco(@RequestBody Endereco atualizacao) {
        HttpStatus status = HttpStatus.CONFLICT; // status padrão é conflito
        Endereco endereco = repositorio.getById(atualizacao.getId()); // busca endereço pelo id
        if (endereco != null) { // se achar, atualiza
            EnderecoAtualizador atualizador = new EnderecoAtualizador(); // cria atualizador
            atualizador.atualizar(endereco, atualizacao); // atualiza dados
            repositorio.save(endereco); // salva no banco
            status = HttpStatus.OK; // muda status para ok
        } else {
            status = HttpStatus.BAD_REQUEST; // se não achar, retorna bad request
        }
        return new ResponseEntity<>(status); // retorna status
    }

    @DeleteMapping("/endereco/excluir") // endpoint para excluir endereço pelo id do endereço
    public ResponseEntity<?> excluirEndereco(@RequestBody Endereco exclusao) {
        HttpStatus status = HttpStatus.BAD_REQUEST; // status padrão é bad request
        if (exclusao.getId() != null) { // só tenta excluir se o id não for nulo
            var enderecoOpt = repositorio.findById(exclusao.getId()); // busca endereço pelo id
            if (enderecoOpt.isPresent()) { // se achar o endereço
                Endereco endereco = enderecoOpt.get();
                // remove o endereço da lista de todos os clientes que possuem ele
                List<Cliente> clientes = clienteRepositorio.findAll();
                for (Cliente cliente : clientes) {
                    if (cliente.getEndereco().getId().equals(endereco.getId())) {
                        cliente.setEndereco(null);
                        clienteRepositorio.save(cliente); // salva cliente sem o endereço
                    }
                }
                repositorio.delete(endereco); // deleta o endereço do banco
                status = HttpStatus.OK; // muda status para ok
            }
        }
        return new ResponseEntity<>(status); // retorna status
    }
}