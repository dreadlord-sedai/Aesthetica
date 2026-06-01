(function () {
const API_ROOT = '/aesthetica';

window.addEventListener("load", async () => {
  try {
    await loadCartPage();
  } finally {
    Notiflix.Loading.remove();
  }
});

async function loadCartPage() {
  try {
    const container = document.getElementById("cart-page-items");
    const summarySubtotal = document.getElementById("summary-subtotal");
    const summaryShipping = document.getElementById("summary-shipping");
    const summaryTotal = document.getElementById("summary-total");

    const response = await fetch(API_ROOT + "/api/user-carts/load-cart", {
      credentials: "include",
    });

    if (!response.ok) {
      if (container) container.innerHTML = "";
      return;
    }

    const data = await response.json();

    if (!data.status) {
      if (container) {
        container.innerHTML = `
          <div class="cart-empty-state">
            <i class="bi bi-cart3"></i>
            <h4>Your cart is empty</h4>
            <p class="mb-4">Looks like you haven't added anything yet.</p>
            <a href="shop.html" class="btn btn-lg px-5" style="background-color: #5e35b1; color: white;">Start Shopping</a>
          </div>`;
      }
      if (summarySubtotal) summarySubtotal.textContent = "$0.00";
      if (summaryShipping) summaryShipping.textContent = "$0.00";
      if (summaryTotal) summaryTotal.textContent = "$0.00";
      updateBadges(0, "0.00");
      return;
    }

    const cartList = Array.isArray(data.cartList) ? data.cartList : [];
    let subTotal = 0;
    let totalQty = 0;

    if (container) {
      container.innerHTML = "";

      if (cartList.length === 0) {
        container.innerHTML = `
          <div class="cart-empty-state">
            <i class="bi bi-cart3"></i>
            <h4>Your cart is empty</h4>
            <p class="mb-4">Looks like you haven't added anything yet.</p>
            <a href="shop.html" class="btn btn-lg px-5" style="background-color: #5e35b1; color: white;">Start Shopping</a>
          </div>`;
      } else {
        cartList.forEach((cart) => {
          const lineTotal = Number(cart.qty) * parseFloat(cart.price);
          subTotal += lineTotal;
          totalQty += Number(cart.qty);

          const priceStr = formatPrice(cart.price);
          const lineTotalStr = formatPrice(lineTotal);
          const thumb = Array.isArray(cart.images) && cart.images.length > 0
            ? (cart.images[0].startsWith('http') || cart.images[0].startsWith('/') ? cart.images[0] : API_ROOT + '/' + cart.images[0])
            : API_ROOT + "/assets/images/placeholder.webp";

          container.innerHTML += `
            <div class="cart-item-card p-3 p-md-4 mb-3">
              <div class="row g-3 align-items-center">
                <div class="col-12 col-sm-4 col-md-3">
                  <img src="${thumb}" alt="${escapeHtml(cart.title)}" class="cart-item-thumb w-100" />
                </div>
                <div class="col-12 col-sm-8 col-md-9">
                  <div class="d-flex flex-column gap-2">
                    <div class="d-flex justify-content-between align-items-start">
                      <h6 class="cart-item-title mb-0">${escapeHtml(cart.title)}</h6>
                      <button class="cart-remove-btn" onclick="pageRemoveCartItem(${cart.cartId})" title="Remove item">
                        <i class="bi bi-x-lg"></i>
                      </button>
                    </div>
                    <span class="cart-item-price">${priceStr}</span>
                    <div class="d-flex justify-content-between align-items-center mt-1">
                      <div class="d-inline-flex align-items-center">
                        <button class="cart-qty-btn rounded-start" onclick="pageChangeCartQty(${cart.cartId}, -1)">−</button>
                        <input type="text" class="cart-qty-input" value="${cart.qty}" readonly />
                        <button class="cart-qty-btn rounded-end" onclick="pageChangeCartQty(${cart.cartId}, 1)">+</button>
                      </div>
                      <span class="cart-item-total">${lineTotalStr}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>`;
        });
      }
    }

    if (summarySubtotal) summarySubtotal.textContent = formatPrice(subTotal);
    if (summaryTotal) summaryTotal.textContent = formatPrice(subTotal);

    updateBadges(totalQty, formatPrice(subTotal));
  } catch (error) {
    Notiflix.Notify.failure(error.message, { position: "center-top" });
  }
}

async function pageRemoveCartItem(cartId) {
  const response = await fetch(
    API_ROOT + `/api/user-carts/remove?cartItemId=${encodeURIComponent(cartId)}`,
    { credentials: "include" },
  );

  if (response.ok) {
    const data = await response.json();
    if (data.status) {
      Notiflix.Notify.success("Item removed", { position: "center-top" });
      await loadCartPage();
      return;
    }
    Notiflix.Notify.failure(data.message, { position: "center-top" });
  } else {
    Notiflix.Notify.failure("Failed to remove item.", { position: "center-top" });
  }
}

async function pageChangeCartQty(cartId, delta) {
  const response = await fetch(
    API_ROOT + `/api/user-carts/update-qty?cartItemId=${encodeURIComponent(cartId)}&delta=${encodeURIComponent(delta)}`,
    { credentials: "include" },
  );

  if (response.ok) {
    const data = await response.json();
    if (data.status) {
      await loadCartPage();
      return;
    }
    Notiflix.Notify.failure(data.message, { position: "center-top" });
  } else {
    Notiflix.Notify.failure("Failed to update quantity.", { position: "center-top" });
  }
}

function formatPrice(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
    minimumFractionDigits: 2,
  }).format(value);
}

function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

function updateBadges(qty, price) {
  const badgeEl = document.getElementById("shop-icon-badge");
  if (badgeEl) badgeEl.textContent = String(qty);

  const badgeMobileEl = document.getElementById("shop-icon-badge-mobile");
  if (badgeMobileEl) badgeMobileEl.textContent = String(qty);

  const navPriceEl = document.getElementById("nav-cart-price");
  if (navPriceEl) navPriceEl.textContent = price;
}

window.pageRemoveCartItem = pageRemoveCartItem;
window.pageChangeCartQty = pageChangeCartQty;
})();
