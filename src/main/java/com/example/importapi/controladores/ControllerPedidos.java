package com.example.importapi.controladores;

import com.example.importapi.dominios.*;
import com.example.importapi.repositorios.*;
import com.example.importapi.visoes.UsuarioVisao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.badRequest;

@CrossOrigin()
@RestController
@RequestMapping("/pedidos")
public class ControllerPedidos {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/download")
    public ResponseEntity baixarArquivoGeral(@RequestBody Usuario usuarioAuth, HttpServletResponse response) throws IOException {
        if (usuarioAuth.getEmail().equals("admin@gmail.com") & usuarioAuth.getSenha().equals("admin")) {
            FileWriter fileWriter = new FileWriter("src/main/resources/static/Pedidos.csv");
            String headers = "idPedido;data_hora;nome_estabelecimento;post_id;" +
                    "taxa_entrega;valor_total_compras;nomeProduto1; valorProduto1;nomeProduto2; valorProduto2;nomeProduto3; valorProduto3;\n";

            fileWriter.write(headers);
            escreverArquivoDePedidos(pedidoRepository.findAll(), fileWriter);

            File file = new File("src/main/resources/static/Pedidos.csv");
            response.setHeader("Content-Disposition", String.format("attachment; filename=Pedidos.csv"));

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            }
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/entregador")
    public ResponseEntity baixarArquivoUnicoUsuario(@RequestBody Usuario usuarioAuth, HttpServletResponse response) throws IOException {
        UsuarioVisao usuarioEncontrado = repository.findByEmailESenha(usuarioAuth.getEmail(), usuarioAuth.getSenha());
        if (usuarioEncontrado != null) {
            String uniqueID = UUID.randomUUID().toString();
            String nomeArquivo = String.format("%s-%s-%s", uniqueID.substring(5, 12), usuarioEncontrado.getNomeCompleto().substring(0, 10), LocalDate.now());
            String caminhoArquivo = String.format("src/main/resources/static/%s.csv", nomeArquivo);
            FileWriter fileWriter = new FileWriter(caminhoArquivo);
            List<Pedido> pedidos = new ArrayList<Pedido>();
            File file = new File(caminhoArquivo);

            String headers = "idPedido;data_hora;nome_estabelecimento;post_id;" +
                    "taxa_entrega;valor_total_compras;nomeProduto1; valorProduto1;nomeProduto2; valorProduto2;nomeProduto3; valorProduto3;\n";
            fileWriter.write(headers);

            List<Post> posts = postRepository.findByEntregadorId(usuarioEncontrado.getIdUsuario());
            for (Post post : posts) {
                Optional<Pedido> pedido = pedidoRepository.findByPostId(post.getIdPost());
                if (pedido.isPresent())
                    pedidos.add(pedido.get());
            }

            if (pedidos.size() == 0) {
                return ResponseEntity.noContent().build();
            } else {
                escreverArquivoDePedidos(pedidos, fileWriter);
                response.setHeader("Content-Disposition", String.format("attachment; filename=%s", nomeArquivo));

                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                    FileCopyUtils.copy(inputStream, response.getOutputStream());
                }
                return ResponseEntity.ok().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public void escreverArquivoDePedidos(List<Pedido> pedidos, FileWriter fileWriter) throws IOException {
        for (Pedido pedido : pedidos) {
            String info = String.format("%s;%s;%s;%s;%s;%s;", pedido.getIdPedido(), pedido.getDataHora(),
                    pedido.getEstabelecimento(), pedido.getPostId(), pedido.getTaxaEntrega(),
                    pedido.getValorTotalCompras());
            fileWriter.write(info);

            String[] produtosIds = pedido.getProdutosIds().split(",");
            for (String id : produtosIds) {
                Produto produto = produtoRepository.findByIdProduto(Integer.parseInt(id));
                fileWriter.write(String.format("%s;%s;", produto.getNomeProduto(), produto.getValor()));
            }
            fileWriter.write("\n");
        }
        fileWriter.close();
    }
}