package com.moises.turism.service;

import com.moises.turism.common.ConstantesCatalogo;
import com.moises.turism.common.exception.ApiException;
import com.moises.turism.domain.Anfitrion;
import com.moises.turism.domain.Interes;
import com.moises.turism.domain.Rol;
import com.moises.turism.domain.Usuario;
import com.moises.turism.dto.auth.LoginRequest;
import com.moises.turism.dto.auth.RegistroUsuarioRequest;
import com.moises.turism.dto.auth.UsuarioResponse;
import com.moises.turism.enums.EstadoCuenta;
import com.moises.turism.enums.EstadoValidacionAnfitrion;
import com.moises.turism.repository.AnfitrionRepository;
import com.moises.turism.repository.InteresRepository;
import com.moises.turism.repository.RolRepository;
import com.moises.turism.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AnfitrionRepository anfitrionRepository;
    private final InteresRepository interesRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionService notificacionService;
    private final SesionService sesionService;

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest request) {
        if (usuarioRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese email.");
        }

        String tipoCuenta = normalizarTipoCuenta(request.tipoCuenta());
        validarDatosEspecificosAnfitrion(tipoCuenta, request);

        Rol rol = rolRepository.findByNombreRolIgnoreCase(tipoCuenta)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No existe el rol configurado para " + tipoCuenta + "."));

        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setTelefono(request.telefono().trim());
        usuario.setCorreoVerificado(false);
        usuario.setEstadoCuenta(EstadoCuenta.PENDIENTE_VERIFICACION);
        usuario.setIntereses(obtenerIntereses(request.interesesIds()));
        usuarioRepository.save(usuario);

        Anfitrion anfitrion = null;
        if (ConstantesCatalogo.ROL_ANFITRION.equals(tipoCuenta)) {
            anfitrion = new Anfitrion();
            anfitrion.setUsuario(usuario);
            anfitrion.setDocumentoIdentidad(request.documentoIdentidad().trim());
            anfitrion.setDescripcion(request.descripcionAnfitrion().trim());
            anfitrion.setEstadoValidacion(EstadoValidacionAnfitrion.PENDIENTE);
            anfitrionRepository.save(anfitrion);
        }

        return UsuarioResponse.from(usuario, anfitrion);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas."));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas.");
        }

        Anfitrion anfitrion = anfitrionRepository.findByUsuarioIdUsuario(usuario.getIdUsuario()).orElse(null);
        String sessionToken = sesionService.crearSesion(usuario.getIdUsuario());
        return UsuarioResponse.from(usuario, anfitrion, sessionToken);
    }



    @Transactional
    public UsuarioResponse registrarAnfitrionDesdeAdmin(RegistroUsuarioRequest request) {
        RegistroUsuarioRequest normalized = new RegistroUsuarioRequest(
                request.nombres(),
                request.apellidos(),
                request.email(),
                request.password(),
                request.telefono(),
                ConstantesCatalogo.ROL_ANFITRION,
                request.documentoIdentidad(),
                request.descripcionAnfitrion(),
                request.interesesIds()
        );

        UsuarioResponse creado = registrar(normalized);
        return verificarCorreo(creado.idUsuario());
    }

    @Transactional
    public UsuarioResponse verificarCorreo(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        if (Boolean.TRUE.equals(usuario.getCorreoVerificado())) {
            Anfitrion anfitrionExistente = anfitrionRepository.findByUsuarioIdUsuario(idUsuario).orElse(null);
            return UsuarioResponse.from(usuario, anfitrionExistente);
        }

        usuario.setCorreoVerificado(true);
        Anfitrion anfitrion = anfitrionRepository.findByUsuarioIdUsuario(idUsuario).orElse(null);

        if (esViajero(usuario)) {
            usuario.setEstadoCuenta(EstadoCuenta.ACTIVA);
            notificacionService.notificarUsuario(
                    usuario,
                    "Cuenta activada",
                    "Tu correo fue verificado y tu cuenta de viajero está activa."
            );
        } else if (esAnfitrion(usuario)) {
            usuario.setEstadoCuenta(EstadoCuenta.EN_REVISION);
            notificacionService.notificarUsuario(
                    usuario,
                    "Perfil en revisión",
                    "Tu correo fue verificado. El perfil de anfitrión pasó a revisión administrativa."
            );
            notificacionService.notificarAdministradores(
                    "Nuevo anfitrión por revisar",
                    "El anfitrión " + usuario.getEmail() + " verificó su correo y requiere validación."
            );
        }

        usuarioRepository.save(usuario);
        return UsuarioResponse.from(usuario, anfitrion);
    }

    private Set<Interes> obtenerIntereses(Set<Integer> interesesIds) {
        if (interesesIds == null || interesesIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return new LinkedHashSet<>(interesRepository.findAllById(interesesIds));
    }

    private void validarDatosEspecificosAnfitrion(String tipoCuenta, RegistroUsuarioRequest request) {
        if (!ConstantesCatalogo.ROL_ANFITRION.equals(tipoCuenta)) {
            return;
        }

        if (!StringUtils.hasText(request.documentoIdentidad())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "documentoIdentidad es requerido para un anfitrión.");
        }
        if (!StringUtils.hasText(request.descripcionAnfitrion())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "descripcionAnfitrion es requerida para un anfitrión.");
        }
    }

    private String normalizarTipoCuenta(String tipoCuenta) {
        String valor = tipoCuenta.trim().toUpperCase(Locale.ROOT);
        if (!ConstantesCatalogo.ROL_VIAJERO.equals(valor) && !ConstantesCatalogo.ROL_ANFITRION.equals(valor)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "tipoCuenta debe ser VIAJERO o ANFITRION.");
        }
        return valor;
    }

    private boolean esViajero(Usuario usuario) {
        return ConstantesCatalogo.ROL_VIAJERO.equalsIgnoreCase(usuario.getRol().getNombreRol());
    }

    private boolean esAnfitrion(Usuario usuario) {
        return ConstantesCatalogo.ROL_ANFITRION.equalsIgnoreCase(usuario.getRol().getNombreRol());
    }
}
