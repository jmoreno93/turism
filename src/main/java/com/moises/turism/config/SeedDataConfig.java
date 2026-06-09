package com.moises.turism.config;

import com.moises.turism.common.ConstantesCatalogo;
import com.moises.turism.domain.Anfitrion;
import com.moises.turism.domain.CategoriaExperiencia;
import com.moises.turism.domain.DisponibilidadExperiencia;
import com.moises.turism.domain.EstadoReserva;
import com.moises.turism.domain.Experiencia;
import com.moises.turism.domain.ExperienciaFoto;
import com.moises.turism.domain.Interes;
import com.moises.turism.domain.Rol;
import com.moises.turism.domain.Usuario;
import com.moises.turism.enums.EstadoCuenta;
import com.moises.turism.enums.EstadoPublicacionExperiencia;
import com.moises.turism.enums.EstadoValidacionAnfitrion;
import com.moises.turism.repository.AnfitrionRepository;
import com.moises.turism.repository.CategoriaExperienciaRepository;
import com.moises.turism.repository.DisponibilidadExperienciaRepository;
import com.moises.turism.repository.EstadoReservaRepository;
import com.moises.turism.repository.ExperienciaFotoRepository;
import com.moises.turism.repository.ExperienciaRepository;
import com.moises.turism.repository.InteresRepository;
import com.moises.turism.repository.RolRepository;
import com.moises.turism.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {

    private final RolRepository rolRepository;
    private final EstadoReservaRepository estadoReservaRepository;
    private final CategoriaExperienciaRepository categoriaExperienciaRepository;
    private final InteresRepository interesRepository;
    private final UsuarioRepository usuarioRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final ExperienciaRepository experienciaRepository;
    private final ExperienciaFotoRepository experienciaFotoRepository;
    private final DisponibilidadExperienciaRepository disponibilidadExperienciaRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedCatalogosBase() {
        return args -> {
            Rol rolViajero = crearRolSiNoExiste(ConstantesCatalogo.ROL_VIAJERO);
            Rol rolAnfitrion = crearRolSiNoExiste(ConstantesCatalogo.ROL_ANFITRION);
            Rol rolAdministrador = crearRolSiNoExiste(ConstantesCatalogo.ROL_ADMINISTRADOR);

            crearEstadoReservaSiNoExiste(ConstantesCatalogo.ESTADO_RESERVA_PENDIENTE);
            crearEstadoReservaSiNoExiste(ConstantesCatalogo.ESTADO_RESERVA_CONFIRMADA);
            crearEstadoReservaSiNoExiste(ConstantesCatalogo.ESTADO_RESERVA_RECHAZADA);

            CategoriaExperiencia gastronomia = crearCategoriaSiNoExiste("Gastronomía");
            CategoriaExperiencia aventura = crearCategoriaSiNoExiste("Aventura");
            CategoriaExperiencia cultura = crearCategoriaSiNoExiste("Cultura");
            CategoriaExperiencia naturaleza = crearCategoriaSiNoExiste("Naturaleza");
            CategoriaExperiencia historia = crearCategoriaSiNoExiste("Historia");

            crearInteresSiNoExiste("Gastronomía");
            crearInteresSiNoExiste("Aventura");
            crearInteresSiNoExiste("Cultura");
            crearInteresSiNoExiste("Fotografía");
            crearInteresSiNoExiste("Naturaleza");

            crearUsuarioAdminSiNoExiste(rolAdministrador);
            crearUsuarioViajeroDemoSiNoExiste(rolViajero);

            Anfitrion anfitrionDemo = crearAnfitrionDemoSiNoExiste(rolAnfitrion);
            sembrarExperienciasDemoSiHaceFalta(anfitrionDemo, List.of(gastronomia, aventura, cultura, naturaleza, historia));
        };
    }

    private Rol crearRolSiNoExiste(String nombreRol) {
        return rolRepository.findByNombreRolIgnoreCase(nombreRol)
                .orElseGet(() -> rolRepository.save(new Rol(nombreRol)));
    }

    private void crearEstadoReservaSiNoExiste(String nombreEstado) {
        estadoReservaRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseGet(() -> estadoReservaRepository.save(new EstadoReserva(nombreEstado)));
    }

    private CategoriaExperiencia crearCategoriaSiNoExiste(String nombreCategoria) {
        return categoriaExperienciaRepository.findByNombreCategoriaIgnoreCase(nombreCategoria)
                .orElseGet(() -> {
                    CategoriaExperiencia categoria = new CategoriaExperiencia();
                    categoria.setNombreCategoria(nombreCategoria);
                    return categoriaExperienciaRepository.save(categoria);
                });
    }

    private void crearInteresSiNoExiste(String nombreInteres) {
        interesRepository.findByNombreInteresIgnoreCase(nombreInteres)
                .orElseGet(() -> {
                    Interes interes = new Interes();
                    interes.setNombreInteres(nombreInteres);
                    return interesRepository.save(interes);
                });
    }

    private void crearUsuarioAdminSiNoExiste(Rol rolAdministrador) {
        if (usuarioRepository.existsByEmailIgnoreCase("admin@turism.local")) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setRol(rolAdministrador);
        admin.setNombres("Administrador");
        admin.setApellidos("Turism");
        admin.setEmail("admin@turism.local");
        admin.setTelefono("999999999");
        admin.setPasswordHash(passwordEncoder.encode("Admin12345"));
        admin.setCorreoVerificado(true);
        admin.setEstadoCuenta(EstadoCuenta.ACTIVA);
        usuarioRepository.save(admin);
    }

    private void crearUsuarioViajeroDemoSiNoExiste(Rol rolViajero) {
        if (usuarioRepository.existsByEmailIgnoreCase("viajero@turism.local")) {
            return;
        }

        Usuario viajero = new Usuario();
        viajero.setRol(rolViajero);
        viajero.setNombres("Viajero");
        viajero.setApellidos("Demo");
        viajero.setEmail("viajero@turism.local");
        viajero.setTelefono("988777666");
        viajero.setPasswordHash(passwordEncoder.encode("Viajero123"));
        viajero.setCorreoVerificado(true);
        viajero.setEstadoCuenta(EstadoCuenta.ACTIVA);
        usuarioRepository.save(viajero);
    }

    private Anfitrion crearAnfitrionDemoSiNoExiste(Rol rolAnfitrion) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase("operaciones@turism.local")
                .orElseGet(() -> {
                    Usuario anfitrionUsuario = new Usuario();
                    anfitrionUsuario.setRol(rolAnfitrion);
                    anfitrionUsuario.setNombres("Operaciones");
                    anfitrionUsuario.setApellidos("Turism");
                    anfitrionUsuario.setEmail("operaciones@turism.local");
                    anfitrionUsuario.setTelefono("977666555");
                    anfitrionUsuario.setPasswordHash(passwordEncoder.encode("Host12345"));
                    anfitrionUsuario.setCorreoVerificado(true);
                    anfitrionUsuario.setEstadoCuenta(EstadoCuenta.ACTIVA);
                    return usuarioRepository.save(anfitrionUsuario);
                });

        return anfitrionRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseGet(() -> {
                    Anfitrion anfitrion = new Anfitrion();
                    anfitrion.setUsuario(usuario);
                    anfitrion.setDocumentoIdentidad("HOST-DEMO-001");
                    anfitrion.setDescripcion("Operador turístico demo para cargar experiencias iniciales del sistema.");
                    anfitrion.setEstadoValidacion(EstadoValidacionAnfitrion.APROBADO);
                    anfitrion.setFechaValidacion(LocalDateTime.now());
                    return anfitrionRepository.save(anfitrion);
                });
    }

    private void sembrarExperienciasDemoSiHaceFalta(Anfitrion anfitrion, List<CategoriaExperiencia> categorias) {
        long publicadas = experienciaRepository.findAllByEstadoPublicacionOrderByFechaCreacionDesc(EstadoPublicacionExperiencia.PUBLICADA).size();
        if (publicadas >= 20) {
            return;
        }

        DemoExperiencia[] data = new DemoExperiencia[] {
                new DemoExperiencia("City tour histórico por Lima", "Recorrido guiado por casonas, plazas y calles tradicionales del Centro Histórico.", "Lima", "Historia", "120.00", 18, 3, "https://images.unsplash.com/photo-1531968455001-5c5272a41129?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Ruta gastronómica en Miraflores", "Degustación de platos peruanos, postres locales y bebidas tradicionales.", "Miraflores", "Gastronomía", "165.00", 12, 4, "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Sandboard en Huacachina", "Aventura en dunas con instructor, equipo básico y vista al oasis.", "Ica", "Aventura", "210.00", 10, 5, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Caminata por Barranco artístico", "Paseo cultural por murales, galerías, puente de los suspiros y cafés locales.", "Barranco", "Cultura", "90.00", 20, 3, "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Reserva natural de Paracas", "Visita panorámica a playas, miradores y ecosistemas costeros protegidos.", "Paracas", "Naturaleza", "185.00", 16, 6, "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Tour de café y cacao", "Aprende el proceso artesanal del café y cacao con degustación incluida.", "Chanchamayo", "Gastronomía", "175.00", 14, 4, "https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Trekking a cataratas", "Ruta moderada por senderos naturales hasta una caída de agua rodeada de vegetación.", "Tarapoto", "Naturaleza", "155.00", 15, 5, "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Noche cultural con danzas", "Presentación de danzas regionales con explicación de vestimenta, música y tradición.", "Cusco", "Cultura", "140.00", 24, 3, "https://images.unsplash.com/photo-1526392060635-9d6019884377?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Museos y huacas de Lima", "Visita guiada a espacios arqueológicos urbanos y museos representativos.", "Lima", "Historia", "130.00", 16, 4, "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Kayak en bahía tranquila", "Experiencia básica de kayak con guía y normas de seguridad para principiantes.", "Paracas", "Aventura", "195.00", 8, 3, "https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Mercados y sabores locales", "Compra guiada en mercado tradicional con explicación de insumos peruanos.", "Surquillo", "Gastronomía", "105.00", 12, 3, "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Ruta fotográfica costera", "Puntos panorámicos para fotografía urbana, litoral y arquitectura contemporánea.", "Costa Verde", "Cultura", "95.00", 10, 3, "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Tour histórico republicano", "Recorrido por edificios, plazas y relatos del Perú republicano.", "Pueblo Libre", "Historia", "110.00", 18, 3, "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Paseo ecológico en humedales", "Observación de aves, flora local y explicación de conservación ambiental.", "Ventanilla", "Naturaleza", "85.00", 20, 4, "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Clase de cocina peruana", "Prepara una entrada, fondo y bebida tradicional con guía paso a paso.", "San Isidro", "Gastronomía", "220.00", 10, 4, "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Bicicleta por malecón", "Ruta segura por ciclovías y miradores frente al mar.", "Miraflores", "Aventura", "75.00", 14, 2, "https://images.unsplash.com/photo-1485965120184-e220f721d03e?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Cerámica y arte local", "Taller participativo con artesanos para crear una pieza sencilla.", "Chulucanas", "Cultura", "150.00", 12, 4, "https://images.unsplash.com/photo-1493106819501-66d381c466f1?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Miradores andinos", "Visita a puntos elevados con explicación geográfica, cultural y fotográfica.", "Huaraz", "Naturaleza", "240.00", 10, 6, "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Circuito arqueológico norte", "Recorrido por complejos arqueológicos y relatos de culturas preincas.", "Trujillo", "Historia", "200.00", 18, 5, "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?auto=format&fit=crop&w=900&q=80"),
                new DemoExperiencia("Aventura en canopy", "Circuito de canopy con guía, equipo y medidas básicas de seguridad.", "Oxapampa", "Aventura", "190.00", 10, 3, "https://images.unsplash.com/photo-1522163182402-834f871fd851?auto=format&fit=crop&w=900&q=80")
        };

        for (DemoExperiencia item : data) {
            boolean existe = experienciaRepository.findAllByOrderByFechaCreacionDesc().stream()
                    .anyMatch(exp -> exp.getTitulo().equalsIgnoreCase(item.titulo()));
            if (!existe) {
                crearExperienciaDemo(anfitrion, buscarCategoria(categorias, item.categoria()), item);
            }
        }
    }

    private CategoriaExperiencia buscarCategoria(List<CategoriaExperiencia> categorias, String nombre) {
        return categorias.stream()
                .filter(categoria -> categoria.getNombreCategoria().equalsIgnoreCase(nombre))
                .findFirst()
                .orElse(categorias.get(0));
    }

    private void crearExperienciaDemo(Anfitrion anfitrion, CategoriaExperiencia categoria, DemoExperiencia item) {
        Experiencia experiencia = new Experiencia();
        experiencia.setAnfitrion(anfitrion);
        experiencia.setCategoria(categoria);
        experiencia.setTitulo(item.titulo());
        experiencia.setDescripcion(item.descripcion());
        experiencia.setUbicacion(item.ubicacion());
        experiencia.setPrecio(new BigDecimal(item.precio()));
        experiencia.setCapacidadMaxima(item.capacidadMaxima());
        experiencia.setDuracionHoras(item.duracionHoras());
        experiencia.setEstadoPublicacion(EstadoPublicacionExperiencia.PUBLICADA);
        experiencia.setFechaCreacion(LocalDateTime.now().minusDays(item.duracionHoras()));
        experienciaRepository.save(experiencia);

        ExperienciaFoto foto = new ExperienciaFoto();
        foto.setExperiencia(experiencia);
        foto.setUrlFoto(item.fotoUrl());
        experienciaFotoRepository.save(foto);

        for (int i = 0; i < 3; i++) {
            DisponibilidadExperiencia disponibilidad = new DisponibilidadExperiencia();
            disponibilidad.setExperiencia(experiencia);
            disponibilidad.setFechaDisponible(LocalDate.now().plusDays(7L + i * 5L + experiencia.getIdExperiencia() % 3));
            disponibilidad.setCuposDisponibles(Math.max(4, item.capacidadMaxima() - i * 2));
            disponibilidadExperienciaRepository.save(disponibilidad);
        }
    }

    private record DemoExperiencia(
            String titulo,
            String descripcion,
            String ubicacion,
            String categoria,
            String precio,
            Integer capacidadMaxima,
            Integer duracionHoras,
            String fotoUrl
    ) {
    }
}
