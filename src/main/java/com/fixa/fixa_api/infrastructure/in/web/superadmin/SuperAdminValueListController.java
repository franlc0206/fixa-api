package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.CategoriaRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/superadmin/valuelist")
public class SuperAdminValueListController {

    private final CategoriaRepositoryPort categoriaPort;
    private final UsuarioRepositoryPort usuarioPort;

    public SuperAdminValueListController(CategoriaRepositoryPort categoriaPort,
                                         UsuarioRepositoryPort usuarioPort) {
        this.categoriaPort = categoriaPort;
        this.usuarioPort = usuarioPort;
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<Categoria>> categorias(@RequestParam(value = "tipo", required = false) String tipo) {
        List<Categoria> all = categoriaPort.findAll();
        if (tipo != null && !tipo.isBlank()) {
            String t = tipo.toLowerCase(Locale.ROOT);
            all = all.stream().filter(c -> c.getTipo() != null && c.getTipo().toLowerCase(Locale.ROOT).equals(t)).collect(Collectors.toList());
        }
        return ResponseEntity.ok(all);
    }

    public static class PageResponse<T> {
        public List<T> content;
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<PageResponse<Usuario>> usuarios(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        String needle = q == null ? "" : q.toLowerCase(Locale.ROOT);
        List<Usuario> all = usuarioPort.findAll().stream()
                .filter(u -> needle.isEmpty() ||
                        (u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(needle)) ||
                        (u.getNombre() != null && u.getNombre().toLowerCase(Locale.ROOT).contains(needle)) ||
                        (u.getApellido() != null && u.getApellido().toLowerCase(Locale.ROOT).contains(needle)))
                .collect(Collectors.toList());
        int from = Math.max(0, page * size);
        int to = Math.min(all.size(), from + size);
        List<Usuario> slice = from >= all.size() ? List.of() : all.subList(from, to);
        PageResponse<Usuario> resp = new PageResponse<>();
        resp.content = slice;
        resp.page = page;
        resp.size = size;
        resp.totalElements = all.size();
        resp.totalPages = (int) Math.ceil(all.size() / (double) size);
        return ResponseEntity.ok(resp);
    }
}
