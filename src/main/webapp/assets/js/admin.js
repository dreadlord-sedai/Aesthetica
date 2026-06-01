window.API_ROOT = window.API_ROOT || '/aesthetica';
let categoryDirectory = [];

document.addEventListener("DOMContentLoaded", () => {
  const adminTabs = document.querySelectorAll(".admin-tab");
  const sections = document.querySelectorAll(".content-section");

  adminTabs.forEach((tab) => {
    tab.addEventListener("click", function (e) {
      e.preventDefault();

      const targetId = this.getAttribute("data-target");
      const targetSection = document.getElementById(targetId);

      if (!targetSection) return;

      sections.forEach((section) => {
        section.classList.add("d-none");
      });

      targetSection.classList.remove("d-none");

      adminTabs.forEach((t) => t.classList.remove("active"));
      this.classList.add("active");

      if (targetId === "products") {
        populateCategories();
      } else if (targetId === "categories") {
        const categoryName = document.getElementById("categoryName");
        const categoryImage = document.getElementById("categoryImage");
        const categoryImageFileName = document.getElementById("categoryImageFileName");
        if (categoryName) categoryName.value = "";
        if (categoryImage) categoryImage.value = "";
        if (categoryImageFileName) categoryImageFileName.textContent = "No file selected";
      } else if (targetId === "productListing") {
        loadProductListings();
      }
    });
  });

  const categoryImageInput = document.getElementById("categoryImage");
  const categoryImageFileName = document.getElementById("categoryImageFileName");
  if (categoryImageInput && categoryImageFileName) {
    categoryImageInput.addEventListener("change", function () {
      const file = this.files && this.files.length > 0 ? this.files[0] : null;
      categoryImageFileName.textContent = file ? file.name : "No file selected";
    });
  }

  for (let i = 1; i <= 4; i++) {
    const fileInput = document.getElementById(`image${i}`);
    const imageDiv = document.getElementById(`image${i}div`);
    const span = imageDiv.querySelector("span");
    const icon = imageDiv.querySelector("i");

    imageDiv.addEventListener("click", () => {
      if (fileInput.files.length === 0) {
        fileInput.click();
      } else {
        clearImage(fileInput, imageDiv, span, icon);
      }
    });

    fileInput.addEventListener("change", function () {
      const file = this.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
          imageDiv.style.backgroundImage = `url(${e.target.result})`;
          imageDiv.classList.add("has-image");
          icon.className = "bi bi-trash fs-4 text-danger";
          span.style.display = "none";
        };
        reader.readAsDataURL(file);
      }
    });
  }
});

function clearImage(input, div, span, icon) {
  input.value = "";
  div.style.backgroundImage = "none";
  div.classList.remove("has-image");
  span.style.display = "block";
  icon.className = "bi bi-upload fs-4 text-secondary";
}

async function populateCategories() {
  const response = await fetch(API_ROOT + "/api/data/categories");

  if (response.ok) {
    const data = await response.json();

    if (data.success) {
      categoryDirectory = data.categories || [];

      const categorySelect = document.getElementById("categorySelect");
      if (categorySelect) {
        categorySelect.innerHTML = '<option value="0">Select Category</option>';

        categoryDirectory.forEach((category) => {
          const option = document.createElement("option");
          option.value = category.id;
          option.textContent = category.name;
          categorySelect.appendChild(option);
        });
      }
    }
  }
}

async function addCategory() {
  const nameInput = document.getElementById("categoryName");
  const imageInput = document.getElementById("categoryImage");

  const categoryName = nameInput ? nameInput.value.trim() : "";
  const imageFile = imageInput && imageInput.files ? imageInput.files[0] : null;

  if (!categoryName) {
    Notiflix.Notify.failure("Category name is required", {
      position: "center-top",
    });
    return;
  }

  if (!imageFile) {
    Notiflix.Notify.failure("Category image is required", {
      position: "center-top",
    });
    return;
  }

  const formData = new FormData();
  formData.append("name", categoryName);
  formData.append("image", imageFile);

  try {
    const optionsResponse = await fetch(API_ROOT + "/api/data/categories", {
      method: "OPTIONS",
    });
    const allowHeader = optionsResponse.headers.get("Allow") || "";
    if (!allowHeader.toUpperCase().includes("POST")) {
      Notiflix.Notify.failure(
        "Category upload endpoint is not deployed yet. Restart/redeploy the app and try again.",
        {
          position: "center-top",
          timeout: 4500,
        },
      );
      return;
    }
  } catch (error) {
    Notiflix.Notify.failure("Unable to verify category upload endpoint", {
      position: "center-top",
    });
    return;
  }

  Notiflix.Loading.standard("Saving category...", {
    clickToClose: false,
    svgColor: "#0284c7",
  });

  try {
    const response = await fetch(API_ROOT + "/api/data/categories", {
      method: "POST",
      body: formData,
    });

    const data = await response.json();

    if (response.ok && data.success) {
      Notiflix.Notify.success(data.message || "Category added successfully", {
        position: "center-top",
      });

      if (nameInput) {
        nameInput.value = "";
      }
      if (imageInput) {
        imageInput.value = "";
      }
      const categoryImageFileName = document.getElementById("categoryImageFileName");
      if (categoryImageFileName) {
        categoryImageFileName.textContent = "No file selected";
      }

      await populateCategories();
    } else {
      Notiflix.Notify.failure(data.message || "Failed to add category", {
        position: "center-top",
      });
    }
  } catch (error) {
    Notiflix.Notify.failure("Unable to add category right now", {
      position: "center-top",
    });
  } finally {
    Notiflix.Loading.remove();
  }
}

function triggerCategoryImageUpload() {
  const imageInput = document.getElementById("categoryImage");
  if (imageInput) {
    imageInput.click();
  }
}

async function addProduct() {
  Notiflix.Loading.standard("Uploading Product...", {
    clickToClose: false,
    svgColor: "#0284c7",
  });

  try {
    const title = document.getElementById("productTitle");
    const categorySelect = document.getElementById("categorySelect");
    const descriptionContent = quill.root.innerHTML;

    const productData = {
      title: title.value,
      category: parseInt(categorySelect.value),
      price: parseFloat(document.getElementById("unitPrice").value),
      qty: 100,
      description: descriptionContent,
    };

    const formData = new FormData();
    formData.append("product", JSON.stringify(productData));

    for (let i = 1; i <= 4; i++) {
      const fileInput = document.getElementById(`image${i}`);
      if (fileInput && fileInput.files.length > 0) {
        formData.append("images", fileInput.files[0]);
      }
    }

    const productResponse = await fetch(API_ROOT + "/api/product/addProduct", {
      method: "POST",
      body: formData,
    });

    const result = await productResponse.json();

    Notiflix.Loading.remove();

    if (productResponse.ok && result.status) {
      Notiflix.Notify.success("Product added successfully!");
      await loadProductListings();
    } else {
      Notiflix.Notify.failure(result.message || "Failed to add product.");
    }
  } catch (error) {
    Notiflix.Loading.remove();
    console.error("Error:", error);
    Notiflix.Notify.failure("An unexpected error occurred.");
  }
}

async function loadProductListings() {
  const listingContainer = document.getElementById("productListingContainer");
  if (!listingContainer) {
    return;
  }

  listingContainer.innerHTML =
    '<div class="text-muted">Loading listings...</div>';

  try {
    const response = await fetch(API_ROOT + "/api/product/product-data");
    if (!response.ok) {
      listingContainer.innerHTML =
        '<div class="text-danger">Failed to load product listings.</div>';
      return;
    }

    const data = await response.json();
    const productList = data.productList || [];

    if (!productList.length) {
      listingContainer.innerHTML =
        '<div class="text-muted">No products available yet.</div>';
      return;
    }

    listingContainer.innerHTML = "";

    productList.forEach((product) => {
      const productCard = document.createElement("div");
      productCard.className = "col";

      const rawImg = product.images && product.images.length > 0 ? product.images[0] : null;
      const image = rawImg
        ? (rawImg.startsWith('http') || rawImg.startsWith('/') ? rawImg : API_ROOT + '/' + rawImg)
        : "../assets/images/default-placeholder.png";

      const formattedPrice = Number(product.price || 0).toLocaleString("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      });

      productCard.innerHTML = `
        <div class="card h-100 product-card-hover border border-dark-subtle">
          <img src="${image}" class="card-img-top custom-card-img-shop" alt="${product.title}">
          <div class="card-body bg-body-tertiary">
            <h6 class="card-title mb-2 text-truncate">${product.title}</h6>
            <div class="d-flex justify-content-between align-items-center">
              <span class="fw-bold">Rs. ${formattedPrice}</span>
              <span class="badge" style="background-color: ${product.qty > 0 ? "#7e57c2" : "#f44336"}">${product.qty > 0 ? product.qty : "Out"}</span>
            </div>
          </div>
        </div>
      `;

      listingContainer.appendChild(productCard);
    });
  } catch (error) {
    console.error(error);
    listingContainer.innerHTML =
      '<div class="text-danger">Unable to load product listings.</div>';
  }
}
