window.addEventListener("load", async () => {
  try {
    await loadCartItems();
  } finally {
    Notiflix.Loading.remove();
  }
});

async function addToCart(productId, productQty) {
  const response = await fetch(
    "api/user-carts/cart?productId=" + productId + "&qty=" + productQty,
    { credentials: "include" },
  );

  if (response.ok) {
    const data = await response.json();
    if (data.status) {
      Notiflix.Notify.success(data.message, { position: "center-top" });
      await loadCartItems();
      return true;
    } else {
      Notiflix.Notify.failure(data.message, { position: "center-top" });
    }
  } else {
    Notiflix.Notify.failure("Something went wrong. Please try again.", {
      position: "center-top",
    });
  }

  return false;
}

async function updateCartQuantity(cartId, delta) {
  const response = await fetch(
    `api/user-carts/update-qty?cartItemId=${encodeURIComponent(cartId)}&delta=${encodeURIComponent(delta)}`,
    { credentials: "include" },
  );

  if (response.ok) {
    const data = await response.json();
    if (data.status) {
      await loadCartItems();
      return true;
    }
    Notiflix.Notify.failure(data.message, { position: "center-top" });
  } else {
    Notiflix.Notify.failure("Something went wrong. Please try again.", {
      position: "center-top",
    });
  }

  return false;
}

async function buyNow(productId, productQty = 1) {
  const added = await addToCart(productId, productQty);
  if (added) {
    // Pass product info to checkout so the checkout page can optionally pre-select or highlight
    // this quick-purchase item. Note: backend still uses cart contents; this query param is
    // provided for UI convenience.
    window.location = `checkout.html?productId=${productId}&qty=${productQty}`;
  }
}

async function loadCartItems() {
  try {
    let cartItemContainer = document.getElementById("cart-content");
    if (cartItemContainer) {
      cartItemContainer.innerHTML = "";
    }
    const response = await fetch("api/user-carts/load-cart", {
      credentials: "include",
    });
    if (response.ok) {
      const data = await response.json();
      if (data.status) {
        let subTotal = 0;
        let totalQty = 0;

        if (cartItemContainer) {
          cartItemContainer.innerHTML = "";
        }

        const cartList = Array.isArray(data.cartList) ? data.cartList : [];

        cartList.forEach((cart) => {
          let total = Number(cart.qty) * parseFloat(cart.price);
          subTotal += total;
          totalQty += Number(cart.qty);

          if (!cartItemContainer) {
            return;
          }

          cartItemContainer.innerHTML += `
                        <div class="card mb-3 border-0 shadow-sm">
                <div class="row g-0 align-items-center">
                    <div class="col-md-3 p-2">
                        <img src="${cart.images[0]}"
                             class="img-fluid rounded-start" alt="Product Image"
                             style="height: 80px; width: 80px; object-fit: cover;">
                    </div>
                    <div class="col-md-9">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="card-title mb-0">${cart.title}</h6>
                                <button class="btn tex" onclick="removeCartItem(${cart.cartId});"><i class="bi bi-x-lg"></i></button>
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="fw-bold" style="color: #5e35b1">${new Intl.NumberFormat(
                                  "en-US",
                                  {
                                    minimumFractionDigits: 2,
                                  },
                                ).format(cart.price)}</span>
                                <div class="input-group input-group-sm w-50">
                                    <button class="btn btn-outline-dark" type="button" onclick="changeCartQty(${cart.cartId}, -1)">-</button>
                                    <input type="text" class="form-control text-center" value="${cart.qty}" readonly>
                                    <button class="btn btn-outline-dark" type="button" onclick="changeCartQty(${cart.cartId}, 1)">+</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
                        `;
        });

        const totalQtyElement = document.getElementById("order-total-quantity");
        if (totalQtyElement) totalQtyElement.innerHTML = String(totalQty);

        const badgeElement = document.getElementById("shop-icon-badge");
        if (badgeElement) badgeElement.innerHTML = String(totalQty);

        const totalAmountElement = document.getElementById("order-total-amount");
        if (totalAmountElement) {
          totalAmountElement.innerHTML = String(
            new Intl.NumberFormat("en-US", {
              minimumFractionDigits: 2,
            }).format(subTotal),
          );
        }

        const navPriceElement = document.getElementById("nav-cart-price");
        if (navPriceElement) {
          navPriceElement.innerHTML = String(
            new Intl.NumberFormat("en-US", {
              minimumFractionDigits: 2,
            }).format(subTotal),
          );
        }
      } else {
        const totalQtyElement = document.getElementById("order-total-quantity");
        if (totalQtyElement) totalQtyElement.innerHTML = "0";
        const badgeElement = document.getElementById("shop-icon-badge");
        if (badgeElement) badgeElement.innerHTML = "0";
        const totalAmountElement = document.getElementById("order-total-amount");
        if (totalAmountElement) totalAmountElement.innerHTML = "0.00";
        const navPriceElement = document.getElementById("nav-cart-price");
        if (navPriceElement) navPriceElement.innerHTML = "0.00";

        if (data.message === "Cart is empty") {
          // keep cart silently empty without console noise
        } else {
          Notiflix.Notify.failure(data.message, {
            position: "center-top",
          });
        }
      }
    } else {
      Notiflix.Notify.failure("Failed to load cart!", {
        position: "center-top",
      });
    }
  } catch (error) {
    Notiflix.Notify.failure(error.message, {
      position: "center-top",
    });
  }
}

async function removeCartItem(cartId) {
  const response = await fetch(
    `api/user-carts/remove?cartItemId=${encodeURIComponent(cartId)}`,
    { credentials: "include" },
  );


  if (response.ok) {
    const data = await response.json();
    if (data.status) {
      await loadCartItems();
      return true;
    }
    Notiflix.Notify.failure(data.message, { position: "center-top" });
  } else {
    Notiflix.Notify.failure("Failed to remove cart item.", { position: "center-top" });
  }

  return false;
}

async function changeCartQty(cartId, delta) {
  await updateCartQuantity(cartId, delta);
}
