document.addEventListener('DOMContentLoaded', function() {
    
    // Función genérica para intentar reabrir un modal si hay errores de validación
    function reopenModalIfErrors(modalId) {
        const modalElement = document.getElementById(modalId);
        
        if (modalElement) {
            // Verifica si el formulario dentro de este modal contiene algún campo con la clase 'is-invalid'
            const formHasVisibleErrors = modalElement.querySelector('.is-invalid');

            // Si hay errores (gracias al BindingResult de Thymeleaf), abrimos el modal
            if (formHasVisibleErrors) {
                const modal = new bootstrap.Modal(modalElement);
                modal.show();
                return true; 
            }
        }
        return false;
    }

    // --- Intentar reabrir el modal de Curso ---
    // Esto se ejecutará si el script fue cargado (condición th:if de cursos.html)
    if (reopenModalIfErrors('crearCursoModal')) {
        return; // Detener si ya abrimos el modal de Curso
    }

    // --- Intentar reabrir el modal de Tarea (Si el script es compartido y el error viene de Tareas) ---
    reopenModalIfErrors('crearTareaModal');

});