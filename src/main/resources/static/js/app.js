const TOKEN_KEY = "jwt";

// ---------- Token ----------

function guardarToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

function obtenerToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function borrarToken() {
    localStorage.removeItem(TOKEN_KEY);
}

// Lee el contenido (payload) del token. Solo sirve para decidir qué mostrar:
// el backend es quien valida de verdad el token en cada petición.
function leerPayload(token) {
    const parteCentral = token.split(".")[1];
    const base64 = parteCentral.replace(/-/g, "+").replace(/_/g, "/");
    const bytes = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
    return JSON.parse(new TextDecoder().decode(bytes));
}

// ---------- Mensajes ----------

function mostrarMensaje(texto, tipo) {
    const mensaje = document.getElementById("mensaje");
    mensaje.textContent = texto;
    mensaje.className = tipo; // "error" o "ok"
}

function ocultarMensaje() {
    document.getElementById("mensaje").className = "oculto";
}

// Convierte el cuerpo de error de la API en un texto legible
function textoDeError(datos, estado) {
    if (datos && datos.error) {
        return datos.error;
    }
    if (datos && Object.keys(datos).length > 0) {
        return Object.values(datos).join(". "); // errores de validación
    }
    return "Error " + estado;
}
// ---------- Llamadas a la API ----------

// Todas las peticiones a rutas protegidas pasan por aquí
async function llamarApi(url, opciones = {}) {
    const cabeceras = { ...(opciones.headers || {}) };

    const token = obtenerToken();
    if (token) {
        cabeceras["Authorization"] = "Bearer " + token;
    }
    if (opciones.body) {
        cabeceras["Content-Type"] = "application/json";
    }

    let respuesta;
    try {
        respuesta = await fetch(url, { ...opciones, headers: cabeceras });
    } catch (e) {
        mostrarMensaje("No se pudo conectar con el servidor.", "error");
        return { ok: false, estado: 0, datos: null };
    }

    const datos = await respuesta.json().catch(() => null);

    if (respuesta.status === 401) {
        // Token ausente, inválido o caducado: la sesión ya no sirve
        borrarToken();
        actualizarVista();
        mostrarMensaje("Tu sesión no es válida o ha caducado. Inicia sesión de nuevo.", "error");
    } else if (respuesta.status === 403) {
        // Estás autenticado, pero tu rol no permite esta operación
        mostrarMensaje("No tienes permisos para realizar esta operación.", "error");
    } else if (!respuesta.ok) {
        mostrarMensaje(textoDeError(datos, respuesta.status), "error");
    }

    return { ok: respuesta.ok, estado: respuesta.status, datos: datos };
}
// ---------- Incidencias ----------

function crearFila(incidencia) {
    const fila = document.createElement("tr");
    const valores = [
        incidencia.id,
        incidencia.titulo,
        incidencia.autorNombre + " (" + incidencia.autorEmail + ")",
        new Date(incidencia.fechaCreacion).toLocaleString("es-ES"),
        incidencia.estado
    ];

    valores.forEach(valor => {
        const celda = document.createElement("td");
        celda.textContent = valor; // textContent: el texto se muestra tal cual, nunca como HTML
        fila.appendChild(celda);
    });

    const celdaAcciones = document.createElement("td");

    const botonVer = document.createElement("button");
    botonVer.textContent = "Ver";
    botonVer.addEventListener("click", () => verIncidencia(incidencia.id));
    celdaAcciones.appendChild(botonVer);

    fila.appendChild(celdaAcciones);
    return fila;
}

async function cargarIncidencias() {
    const resultado = await llamarApi("/incidencias");
    if (!resultado.ok) {
        return;
    }

    const cuerpo = document.getElementById("lista-incidencias");
    cuerpo.replaceChildren();
    resultado.datos.forEach(incidencia => cuerpo.appendChild(crearFila(incidencia)));

    document.getElementById("sin-incidencias")
        .classList.toggle("oculto", resultado.datos.length > 0);
}

async function crearIncidencia(evento) {
    evento.preventDefault();
    ocultarMensaje();

    const resultado = await llamarApi("/incidencias", {
        method: "POST",
        body: JSON.stringify({
            titulo: document.getElementById("inc-titulo").value,
            descripcion: document.getElementById("inc-descripcion").value
        })
    });

    if (resultado.ok) {
        mostrarMensaje("Incidencia creada.", "ok");
        document.getElementById("form-incidencia").reset();
        cargarIncidencias();
    }
}

function mostrarDetalle(incidencia) {
    document.getElementById("detalle-titulo").textContent = incidencia.titulo;
    document.getElementById("detalle-meta").textContent =
        "Incidencia #" + incidencia.id
        + " · Estado: " + incidencia.estado
        + " · Autor: " + incidencia.autorNombre + " (" + incidencia.autorEmail + ")"
        + " · " + new Date(incidencia.fechaCreacion).toLocaleString("es-ES");
    document.getElementById("detalle-descripcion").textContent = incidencia.descripcion;
    document.getElementById("detalle").classList.remove("oculto");
}

function ocultarDetalle() {
    document.getElementById("detalle").classList.add("oculto");
}

async function verIncidencia(id) {
    ocultarMensaje();

    const resultado = await llamarApi("/incidencias/" + id);

    if (resultado.ok) {
        mostrarDetalle(resultado.datos);
    } else {
        ocultarDetalle();
    }
}

function buscarIncidencia(evento) {
    evento.preventDefault();
    verIncidencia(document.getElementById("buscar-id").value);
}

// ---------- Vista ----------

function actualizarVista() {
    const token = obtenerToken();
    let payload = null;

    if (token) {
        try {
            payload = leerPayload(token);
            if (payload.exp * 1000 < Date.now()) { // exp está en segundos
                borrarToken();
                payload = null;
                mostrarMensaje("Tu sesión ha caducado. Inicia sesión de nuevo.", "error");
            }
        } catch (e) {
            borrarToken();
            payload = null;
        }
    }

    const hayssesion = payload !== null;
    document.getElementById("seccion-auth").classList.toggle("oculto", hayssesion);
    document.getElementById("seccion-app").classList.toggle("oculto", !hayssesion);

    if (hayssesion) {
        document.getElementById("info-usuario").textContent =
            payload.nombre + " (" + payload.sub + ") - " + payload.roles.join(", ");
        cargarIncidencias();
    }
}

// ---------- Registro, login y logout ----------

async function registrar(evento) {
    evento.preventDefault();
    ocultarMensaje();

    const respuesta = await fetch("/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            nombre: document.getElementById("registro-nombre").value,
            email: document.getElementById("registro-email").value,
            password: document.getElementById("registro-password").value
        })
    });

    const datos = await respuesta.json().catch(() => null);

    if (respuesta.ok) {
        mostrarMensaje("Cuenta creada. Ya puedes iniciar sesión.", "ok");
        document.getElementById("form-registro").reset();
    } else {
        mostrarMensaje(textoDeError(datos, respuesta.status), "error");
    }
}

async function iniciarSesion(evento) {
    evento.preventDefault();
    ocultarMensaje();

    const respuesta = await fetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            email: document.getElementById("login-email").value,
            password: document.getElementById("login-password").value
        })
    });

    const datos = await respuesta.json().catch(() => null);

    if (respuesta.ok) {
        guardarToken(datos.token);
        document.getElementById("form-login").reset();
        actualizarVista();
    } else {
        mostrarMensaje(textoDeError(datos, respuesta.status), "error");
    }
}

function cerrarSesion() {
    borrarToken();
    ocultarMensaje();
    document.getElementById("lista-incidencias").replaceChildren();
    ocultarDetalle();
    actualizarVista();
}

// ---------- Arranque ----------

document.getElementById("form-registro").addEventListener("submit", registrar);
document.getElementById("form-login").addEventListener("submit", iniciarSesion);
document.getElementById("form-incidencia").addEventListener("submit", crearIncidencia);
document.getElementById("btn-logout").addEventListener("click", cerrarSesion);
document.getElementById("form-buscar").addEventListener("submit", buscarIncidencia);

actualizarVista();