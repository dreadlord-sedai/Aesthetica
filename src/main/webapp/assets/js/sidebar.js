(function () {
  var sidebar = document.getElementById("sidebar");
  var overlay = document.getElementById("sidebarOverlay");

  if (!sidebar) return;

  function openSidebar() {
    sidebar.classList.add("open");
    if (overlay) overlay.classList.add("active");
  }

  function closeSidebar() {
    sidebar.classList.remove("open");
    if (overlay) overlay.classList.remove("active");
  }

  document.addEventListener("click", function (e) {
    var toggle = e.target.closest(".sidebar-toggle");
    if (toggle) {
      e.preventDefault();
      if (sidebar.classList.contains("open")) {
        closeSidebar();
      } else {
        openSidebar();
      }
      return;
    }

    var close = e.target.closest(".sidebar-close");
    if (close) {
      closeSidebar();
      return;
    }

    if (overlay && e.target === overlay) {
      closeSidebar();
    }
  });
})();
