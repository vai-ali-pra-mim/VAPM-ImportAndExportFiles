package com.example.importapi.controladores;

import com.example.importapi.dominios.Cartao;
import com.example.importapi.dominios.Usuario;
import com.example.importapi.repositorios.CartaoRepository;
import com.example.importapi.repositorios.UsuarioRepository;
import com.example.importapi.visoes.UsuarioVisao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.http.ResponseEntity.badRequest;

@CrossOrigin()
@RestController
@RequestMapping("/usuarios")
public class ControllerUsuario {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private CartaoRepository cartaoRepository;

    @GetMapping("/download")
    public ResponseEntity baixarArquivoGeral(@RequestBody Usuario usuarioAuth, HttpServletResponse response) throws IOException {
        if (usuarioAuth.getEmail().equals("admin@gmail.com") & usuarioAuth.getSenha().equals("admin")) {
            FileWriter fileWriter = new FileWriter("src/main/resources/static/Usuarios.csv");
            String headers = "idUsuario;nomeCompleto;dataNascimento;cpf;" +
                    "rg;email;senha;telefone;cep;complemento;coordenadas;pontoReferencia;" +
                    "saldo;fotoRG;fotoPerfil\n";

            fileWriter.write(headers);
            for (UsuarioVisao usuario : repository.findAllSimples()) {
                String info = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;\n", usuario.getIdUsuario(), usuario.getNomeCompleto(), usuario.getDataNascimento(),
                        usuario.getCPF(), usuario.getRG(), usuario.getEmail(), usuario.getSenha(), usuario.getTelefone(),
                        usuario.getCEP(), usuario.getComplemento(), usuario.getCoordenadas(), usuario.getPontoReferencia(),
                        usuario.getSaldo(), usuario.getFotoRG(), usuario.getFotoRG());
                fileWriter.write(info);
            }
            fileWriter.close();

            File file = new File("src/main/resources/static/Usuarios.csv");
            response.setHeader("Content-Disposition", String.format("attachment; filename=Usuarios.csv"));

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            }
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/usuario")
    public ResponseEntity baixarArquivoUnicoUsuario(@RequestBody Usuario usuarioAuth, HttpServletResponse response) throws IOException {
        UsuarioVisao usuarioEncontrado = repository.findByEmailESenha(usuarioAuth.getEmail(), usuarioAuth.getSenha());
        if (usuarioEncontrado != null) {
            String uniqueID = UUID.randomUUID().toString();
            String nomeArquivo = String.format("%s-%s-%s", uniqueID.substring(5,12), usuarioEncontrado.getNomeCompleto().substring(0, 10), LocalDate.now());
            String caminhoArquivo = String.format("src/main/resources/static/%s.csv", nomeArquivo);
            FileWriter fileWriter = new FileWriter(caminhoArquivo);
            String headers = "idUsuario;nomeCompleto;dataNascimento;cpf;" +
                    "rg;email;senha;telefone;cep;complemento;coordenadas;pontoReferencia;" +
                    "saldo;fotoRG;fotoPerfil\n";

            fileWriter.write(headers);
            String info = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;\n", usuarioEncontrado.getIdUsuario(), usuarioEncontrado.getNomeCompleto(), usuarioEncontrado.getDataNascimento(),
                    usuarioEncontrado.getCPF(), usuarioEncontrado.getRG(), usuarioEncontrado.getEmail(), "Unsafe to show", usuarioEncontrado.getTelefone(),
                    usuarioEncontrado.getCEP(), usuarioEncontrado.getComplemento(), usuarioEncontrado.getCoordenadas(), usuarioEncontrado.getPontoReferencia(),
                    usuarioEncontrado.getSaldo(), "Unsafe to show", "Unsafe to show");

            fileWriter.write(info);
            fileWriter.close();

            File file = new File(caminhoArquivo);
            response.setHeader("Content-Disposition", String.format("attachment; filename=Usuarios.csv"));

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            }
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity subirArquivo(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            return badRequest().body("Arquivo não enviado!");
        }

        System.out.println("Recebendo um arquivo do tipo" + arquivo.getContentType());
        String conteudo = new String(arquivo.getBytes(), "UTF-8");
        String[] guardar = conteudo.split("\n");

        for (int i = 1; i < (guardar.length - 1); i++) {

            String nomeCompleto = guardar[i].substring(02,61).trim();
            String CPF = guardar[i].substring(62,73);
            LocalDate dataNascimento =LocalDate.parse(guardar[i].substring(73,83).trim()) ;
            String email = guardar[i].substring(83,147).trim();
            String telefone = guardar[i].substring(148,158).trim();
            String CEP = guardar[i].substring(159,167).trim();
            String complemento = guardar[i].substring(168,186).trim();
            Double saldo =Double.parseDouble(guardar[i].substring(188,195).trim()) ;
            String RG = guardar[i].substring(195,204).trim();
            String pontoReferencia = guardar[i].substring(204,263).trim();
            String senha = guardar[i].substring(264,278).trim();
            String coordenadas = guardar[i].substring(279,308).trim();
            String fotoRG = guardar[i].substring(309,313).trim();
            String fotoPerfil = guardar[i].substring(313,317).trim();
            Integer ehConsumidor = Integer.parseInt(guardar[i].substring(317,318).trim());
            Integer idCartao = Integer.parseInt(guardar[i].substring(319,320).trim());

            Optional<Cartao> cartao = cartaoRepository.findById(idCartao);

            Usuario usuario = new Usuario(nomeCompleto, CPF, dataNascimento, email, telefone, CEP, complemento, saldo,
                    RG, pontoReferencia, senha, coordenadas, fotoRG, fotoPerfil, ehConsumidor, cartao.get());

            repository.save(usuario);
        }
        return ResponseEntity.ok().build();
    }
}