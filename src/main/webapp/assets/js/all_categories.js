window.addEventListener("load", async () => {
  Notiflix.Loading.standard("", {
    clickToClose: false,
    svgColor: "#0284c7",
  });

  try {
    const response = await fetch("api/data/categories");
    const container = document.getElementById("all-category-container");

    if (!response.ok) {
      container.innerHTML =
        '<div class="col-12 text-center text-danger">Failed to load categories.</div>';
      return;
    }

    const data = await response.json();
    const categories = data.categories || [];

    if (!categories.length) {
      container.innerHTML =
        '<div class="col-12 text-center text-muted">No categories available.</div>';
      return;
    }

    container.innerHTML = "";

    categories.forEach((category) => {
      const column = document.createElement("div");
      column.className = "col";

      column.innerHTML = `
        <a href="shop.html?category=${encodeURIComponent(category.name)}" class="text-decoration-none">
          <div class="card h-100 text-center shadow-sm border border-dark-subtle">
            <div class="card-body d-flex flex-column justify-content-center align-items-center">
              <img src="${category.icon}" alt="${category.name}" style="max-width: 56px; max-height: 56px; object-fit: contain" />
              <h6 class="mt-3 mb-0">${category.name}</h6>
            </div>
          </div>
        </a>
      `;

      container.appendChild(column);
    });
  } catch (error) {
    const container = document.getElementById("all-category-container");
    container.innerHTML =
      '<div class="col-12 text-center text-danger">Unable to load categories.</div>';
  } finally {
    Notiflix.Loading.remove();
  }
});
