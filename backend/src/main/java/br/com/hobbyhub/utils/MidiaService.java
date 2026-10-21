package br.com.hobbyhub.utils;

import br.com.hobbyhub.utils.RegraNegocioException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
public class MidiaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiaService.class);
    private static final String PREFIXO_URL = "/midias/";
    private static final String PADRAO_NOME = "[a-f0-9-]{36}\\.(jpg|jpeg|png|gif|webp|mp4|webm)";

    private final Path diretorio;
    private final long limiteBytes;

    public MidiaService(
            @Value("${hobbyhub.midias.diretorio:./data/midias}") String diretorio,
            @Value("${hobbyhub.midias.max-bytes:26214400}") long limiteBytes) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        this.limiteBytes = limiteBytes;
    }

    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }

        if (arquivo.getSize() > limiteBytes) {
            throw new RegraNegocioException("O arquivo deve ter no máximo 25 MB.");
        }

        String extensao = obterExtensao(arquivo.getOriginalFilename());
        Path temporario = null;

        try {
            validarAssinatura(arquivo, extensao);
            Files.createDirectories(diretorio);
            temporario = Files.createTempFile(diretorio, "upload-", ".tmp");

            try (InputStream entrada = arquivo.getInputStream()) {
                Files.copy(entrada, temporario, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            String nome = UUID.randomUUID() + "." + extensao;
            Files.move(temporario, diretorio.resolve(nome));
            return PREFIXO_URL + nome;
        } catch (IOException erro) {
            throw new RegraNegocioException(
                    "Não foi possível armazenar o arquivo. Tente novamente.");
        } finally {
            removerTemporario(temporario);
        }
    }

    public Resource carregar(String nome) {
        Path arquivo = resolverNome(nome);

        if (!Files.isRegularFile(arquivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado.");
        }

        try {
            return new UrlResource(arquivo.toUri());
        } catch (IOException erro) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado.");
        }
    }

    public MediaType obterTipoConteudo(String nome) {
        return switch (obterExtensao(nome)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "mp4" -> MediaType.parseMediaType("video/mp4");
            case "webm" -> MediaType.parseMediaType("video/webm");
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        };
    }

    public void excluir(String url) {
        if (url == null || !url.startsWith(PREFIXO_URL)) {
            return;
        }

        try {
            Files.deleteIfExists(resolverNome(url.substring(PREFIXO_URL.length())));
        } catch (IOException erro) {
            LOGGER.error(
                    "Não foi possível remover a mídia de uma postagem cancelada: {}", url, erro);
        }
    }

    private Path resolverNome(String nome) {
        if (nome == null || !nome.matches(PADRAO_NOME)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path arquivo = diretorio.resolve(nome).normalize();
        if (!arquivo.startsWith(diretorio)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return arquivo;
    }

    private String obterExtensao(String nome) {
        if (nome == null || !nome.contains(".")) {
            throw new RegraNegocioException(
                    "Selecione um arquivo JPG, JPEG, PNG, GIF, WEBP, MP4 ou WEBM.");
        }
        return nome.substring(nome.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void validarAssinatura(MultipartFile arquivo, String extensao) throws IOException {
        byte[] cabecalho;
        try (InputStream entrada = arquivo.getInputStream()) {
            cabecalho = entrada.readNBytes(32);
        }

        boolean valido =
                switch (extensao) {
                    case "jpg", "jpeg" -> comecaCom(cabecalho, 0xFF, 0xD8, 0xFF);
                    case "png" ->
                            comecaCom(cabecalho, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
                    case "gif" ->
                            texto(cabecalho, 0, 6).equals("GIF87a")
                                    || texto(cabecalho, 0, 6).equals("GIF89a");
                    case "webp" ->
                            texto(cabecalho, 0, 4).equals("RIFF")
                                    && texto(cabecalho, 8, 4).equals("WEBP");
                    case "mp4" -> texto(cabecalho, 4, 4).equals("ftyp");
                    case "webm" -> comecaCom(cabecalho, 0x1A, 0x45, 0xDF, 0xA3);
                    default -> false;
                };

        if (!valido) {
            throw new RegraNegocioException(
                    "Formato inválido. Use JPG, JPEG, PNG, GIF, WEBP, MP4 ou WEBM.");
        }
    }

    private boolean comecaCom(byte[] bytes, int... assinatura) {
        if (bytes.length < assinatura.length) {
            return false;
        }
        for (int indice = 0; indice < assinatura.length; indice++) {
            if ((bytes[indice] & 0xFF) != assinatura[indice]) {
                return false;
            }
        }
        return true;
    }

    private String texto(byte[] bytes, int inicio, int tamanho) {
        if (bytes.length < inicio + tamanho) {
            return "";
        }
        return new String(bytes, inicio, tamanho, StandardCharsets.US_ASCII);
    }

    private void removerTemporario(Path temporario) {
        if (temporario == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporario);
        } catch (IOException erro) {
            LOGGER.warn("Não foi possível remover o arquivo temporário {}", temporario, erro);
        }
    }
}
