document.addEventListener('DOMContentLoaded', function() {
    const modalElement = document.getElementById('crearCursoModal');

    // La lógica de Thymeleaf garantiza que este script solo se carga
    // si el controlador envió un error de validación o el flag showModal.
    if (modalElement) {
        // Inicializar y mostrar el modal de Bootstrap 5
        const modal = new bootstrap.Modal(modalElement);
        modal.show();
    }
});