window.API_ROOT = window.API_ROOT || '/aesthetica';

window.addEventListener("load", async () => {
  Notiflix.Loading.standard("", {
    clickToClose: false,
    svgColor: "#0284c7",
  });

  try {
    const response = await fetch(`${API_ROOT}/api/data/categories`);
    const container = document.getElementById("all-category-container");

    if (!response.ok) {
      container.innerHTML =
        '<div class="col-12 text-center py-5 text-danger">Failed to load categories.</div>';
      return;
    }

    const data = await response.json();
    const categories = data.categories || [];

    if (!categories.length) {
      container.innerHTML =
        '<div class="col-12 text-center py-5 text-muted">No categories available.</div>';
      return;
    }

    container.innerHTML = "";

    categories.forEach((category) => {
      const col = document.createElement("div");
      col.className = "col-12 col-lg-6";

      const iconUrl = category.icon
        ? (category.icon.startsWith("http") ? category.icon : `${API_ROOT}/${category.icon}`)
        : `${API_ROOT}/assets/images/placeholder.jpg`;

      col.innerHTML = `
        <a href="shop.html?category=${encodeURIComponent(category.name)}" class="text-decoration-none">
          <div class="category-hero">
            <img class="category-hero-img" src="${iconUrl}" alt="${category.name}" loading="lazy" />
            <div class="category-hero-content">
              <h2 class="category-hero-name">${category.name}</h2>
              <span class="category-hero-count">Browse collection</span>
            </div>
          </div>
        </a>
      `;

      container.appendChild(col);
    });
  } catch (error) {
    const container = document.getElementById("all-category-container");
    container.innerHTML =
      '<div class="col-12 text-center py-5 text-danger">Unable to load categories.</div>';
  } finally {
    Notiflix.Loading.remove();
  }
});
