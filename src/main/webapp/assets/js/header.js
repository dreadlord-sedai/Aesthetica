window.API_ROOT = window.API_ROOT || '/aesthetica';

async function signOut() {
  Notiflix.Confirm.show(
    "Aesthetica",
    "Are you sure you want to log out?",
    "Yes, Log out",
    "Cancel",
    // FIX: Added 'async' keyword here
    async () => {
      try {
        // Now you can use await here
        const response = await fetch(API_ROOT + "/api/users/logout", {
          method: "GET",
          credentials: "include",
        });

        if (response.status === 202 || response.redirected) {
          // 202 Accepted
          window.location = "sign_in.html";
        } else {
          Notiflix.Notify.failure("Logout failed! Please try again.", {
            position: "center-top",
          });
        }
      } catch (e) {
        Notiflix.Notify.failure(e.message, {
          position: "center-top",
        });
      }
    },
    () => {
      // Cancel callback
    },
    {
      titleColor: "#e63946",
      okButtonBackground: "#e63946",
    },
  );
}

async function isLoggedInUser() {
  try {
    const response = await fetch(API_ROOT + "/api/profiles/user-profile", {
      credentials: "include",
    });
    return response.ok && !response.redirected;
  } catch (error) {
    return false;
  }
}

function toggleLogoutControls(isGuest) {
  document.querySelectorAll('[onclick="signOut()"]')?.forEach((element) => {
    element.classList.toggle("d-none", isGuest);
    const wrapper = element.closest("li");
    if (wrapper) {
      wrapper.classList.toggle("d-none", isGuest);
    }
  });
}

function toggleAuthControls(isGuest) {
  document.getElementById("auth-menu")?.classList.toggle("d-none", isGuest);
  document.getElementById("guest-auth-menu")?.classList.toggle("d-none", !isGuest);
}

toggleLogoutControls(true);
toggleAuthControls(true);

document.addEventListener("DOMContentLoaded", async () => {
  const guest = !(await isLoggedInUser());
  toggleLogoutControls(guest);
  toggleAuthControls(guest);
});

const addToCartButton = document.getElementById("addToCart");
if (addToCartButton) {
  addToCartButton.addEventListener("click", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const qtyInput = document.getElementById("qtyInput");
    const qty = qtyInput ? qtyInput.value : 1;
    await addToCart(urlParams.get("id"), qty);
  });
}
