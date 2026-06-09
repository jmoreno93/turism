package com.moises.turism.service;

import com.moises.turism.common.ConstantesCatalogo;
import com.moises.turism.domain.Notificacion;
import com.moises.turism.domain.Usuario;
import com.moises.turism.dto.notificacion.NotificacionResponse;
import com.moises.turism.repository.NotificacionRepository;
import com.moises.turism.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void notificarUsuario(Usuario usuario, String titulo, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacionRepository.save(notificacion);
    }

    @Transactional
    public void notificarAdministradores(String titulo, String mensaje) {
        List<Usuario> administradores = usuarioRepository.findAllByRolNombreRolIgnoreCase(ConstantesCatalogo.ROL_ADMINISTRADOR);
        for (Usuario administrador : administradores) {
            notificarUsuario(administrador, titulo, mensaje);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(Long idUsuario) {
        return notificacionRepository.findAllByUsuarioIdUsuarioOrderByFechaEnvioDesc(idUsuario)
                .stream()
                .map(NotificacionResponse::from)
                .toList();
    }
}
