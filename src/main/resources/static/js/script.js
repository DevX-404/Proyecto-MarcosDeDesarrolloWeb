document.addEventListener("DOMContentLoaded", function () {
    const toggle = document.querySelector('.menu-toggle');
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');
    const overlay = document.getElementById('overlay');
    const links = sidebar.querySelectorAll("a");

    toggle.addEventListener('click', () => {
      sidebar.classList.toggle('collapsed');
      content.classList.toggle('collapsed');
      if (!sidebar.classList.contains("collapsed")) {
        overlay.classList.add("active");
      } else {
        overlay.classList.remove("active");
      }
    });

    overlay.addEventListener("click", () => {
      sidebar.classList.add("collapsed");
      content.classList.add("collapsed");
      overlay.classList.remove("active");
    });

    links.forEach(link => {
      link.addEventListener("click", () => {
        sidebar.classList.add("collapsed");
        content.classList.add("collapsed");
        overlay.classList.remove("active");
      });
    });
});
