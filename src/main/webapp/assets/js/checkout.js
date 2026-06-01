window.addEventListener("load", async () => {
  await getCities();
  await loadUserData();
  await loadAddress();
  // If user arrived via Buy Now (quick-purchase), URL will contain productId & qty.
  // Wait for cart.js to load (addToCart function) and add the item to the cart so checkout
  // shows the purchased item immediately.
  try {
    const params = new URLSearchParams(window.location.search);
    const quickProductId = params.get("productId");
    const quickQty = params.get("qty") || 1;
    if (quickProductId) {
      // Wait up to 5s for addToCart to be available (cart.js is included after this script).
      const waitForAddToCart = () =>
        new Promise((resolve) => {
          const start = Date.now();
          const iv = setInterval(() => {
            if (typeof addToCart === "function") {
              clearInterval(iv);
              resolve(true);
            } else if (Date.now() - start > 5000) {
              clearInterval(iv);
              resolve(false);
            }
          }, 50);
        });

      const ok = await waitForAddToCart();
      if (ok) {
        // Add the quick-purchase item to cart. If user already had items, this will be added on top.
        await addToCart(quickProductId, quickQty);
      } else {
        console.warn("addToCart not available to perform quick-buy on checkout load");
      }
    }
  } catch (e) {
    console.error(e);
  }
});

let cityDirectory = [];

const isCurrentAddressWrap = document.getElementById("isCurrentAddressWrap");
const isCurrentAddressCheckbox = document.getElementById("isCurrentAddress");
const addressSection = document.getElementById("addressSection");
isCurrentAddressCheckbox.addEventListener("change", () => {
  if (isCurrentAddressCheckbox.checked) {
    isCurrentAddressWrap.classList.add("border-primary");
    addressSection.classList.remove("d-none");
  } else {
    isCurrentAddressWrap.classList.remove("border-primary");
    addressSection.classList.add("d-none");

    document.getElementById("lineOne").value = "";
    document.getElementById("lineTwo").value = "";
    document.getElementById("postalCode").value = "";
    document.getElementById("cityInput").value = "";
    document.getElementById("cityId").value = 0;
  }
});
isCurrentAddressWrap.addEventListener("click", (e) => {
  if (e.target !== isCurrentAddressCheckbox) {
    isCurrentAddressCheckbox.click();
  }
});

async function getCities() {
  try {
    const response = await fetch("api/data/cities");
    if (response.ok) {
      const data = await response.json();
      cityDirectory = data.cities || [];
      const cityInput = document.getElementById("cityInput");
      const cityList = document.getElementById("cityList");

      if (cityList) {
        cityList.innerHTML = "";
      }

      if (cityInput) {
        cityInput.addEventListener("change", () => {
          const foundCity = cityDirectory.find(
            (item) =>
              item.name.toLowerCase() === cityInput.value.trim().toLowerCase(),
          );
          document.getElementById("cityId").value = foundCity
            ? foundCity.id
            : 0;
        });
      }

      data.cities.forEach((city) => {
        if (cityList) {
          const option = document.createElement("option");
          option.value = city.name;
          cityList.appendChild(option);
        }
      });
    } else {
      Notiflix.Notify.failure("City Loading Failed!", {
        position: "center-top",
      });
    }
  } catch (e) {
    Notiflix.Notify.failure(e.message, {
      position: "center-top",
    });
  }
}

async function loadAddress() {
  try {
    const response = await fetch("api/profiles/addresses", {
      credentials: "include",
    });
    // If the request was redirected to sign-in, treat as guest: render no addresses
    if (response.redirected) {
      console.log("Guest user: addresses endpoint redirected to login");
      // Ensure guest can enter address details
      isCurrentAddressCheckbox.checked = false;
      isCurrentAddressWrap.classList.remove("border-primary");
      addressSection.classList.remove("d-none");
      // render no addresses
      renderAddresses([]);
      return;
    }
    if (response.ok) {
      const data = await response.json();
      renderAddresses(data.addresses);
      console.log(data.addresses);
    }
  } catch (e) {
    Notiflix.Notify.failure(e.message);
    console.error(e);
  }
}

function renderAddresses(addresses) {
  const list = document.getElementById("addressList");
  const inputField = document.createElement("address-radio");
  list.innerHTML = "";
  addresses.forEach((addr) => {
    const card = document.createElement("address-body");
    card.className =
      "card shadow-sm p-3 mb-3 " +
      (addr.isPrimary ? "border-primary-subtle" : "");
    inputField.setAttribute(
      "checked",
      addr.isPrimary ? "checked" : "un-checked",
    );
    card.style.backgroundColor = addr.isPrimary ? "#f8fbff" : "";

    if (addr.isPrimary) {
      document.getElementById("lineOne").value = addr.lineOne || "";
      document.getElementById("lineTwo").value = addr.lineTwo || "";

      document.getElementById("postalCode").value = addr.postalCode;

      const cityInput = document.getElementById("cityInput");
      const cityId = document.getElementById("cityId");
      const selectedCity = cityDirectory.find(
        (city) => city.id === addr.cityId,
      );
      if (cityInput) cityInput.value = selectedCity ? selectedCity.name : "";
      if (cityId) cityId.value = addr.cityId || 0;
    }

    card.innerHTML = ` 
 
                    <div class="wrapper-div" onclick="switchPrimary(${addr.id})" style="cursor: pointer;">
                        <div class="col-12 d-flex justify-content-between align-items-center">
                            <div class="col-8 d-flex justify-content-start align-items-center">
                                <div class="me-4">
                                    <input type="radio" name="isPrimary" class="form-check-input address-radio" 
                                        ${addr.isPrimary ? "checked" : "disabled"}>
                                </div>
                                <div>
                                    <span class="fs-6 fw-normal"><span>Address 1 : &nbsp;</span>${addr.lineOne}</span><br>
                                    <span class="fs-6 fw-normal"><span>Address 2 : &nbsp;</span>${addr.lineTwo ? addr.lineTwo : "Not set"}</span><br>
                                    <span>${addr.cityName}</span>
                                </div>
                            </div>
                        </div>
                    </div>
         `;
    list.appendChild(card);
  });
}

async function switchPrimary(id) {
  try {
    const response = await fetch(`api/profiles/set-primary-address/${id}`, {
      method: "PATCH",
      credentials: "include",
    });
    if (response.ok) {
      await loadAddress();
    }
  } catch (error) {}
}

async function loadUserData() {
  try {
    const response = await fetch("api/profiles/user-profile", {
      credentials: "include",
    });

    if (response.ok) {
      // If profile endpoint redirected to sign-in, treat as guest user and leave fields blank
      if (response.redirected) {
        console.log("Guest user: profile endpoint redirected to login");
        // allow guest to enter address details
        isCurrentAddressCheckbox.checked = false;
        isCurrentAddressWrap.classList.remove("border-primary");
        addressSection.classList.remove("d-none");
        return;
      }
      const data = await response.json();

      document.getElementById("firstName").value = data.user.firstName;
      document.getElementById("lastName").value = data.user.lastName;
      document.getElementById("mobile").value = data.user.mobile;
    } else {
      Notiflix.Notify.failure("Profile Data Loading Failed!", {
        position: "center-top",
      });
    }
  } catch (e) {
    Notiflix.Notify.failure(e.message, {
      position: "center-top",
    });
  }
}

async function checkout() {
  const deliveryType = document.querySelector('input[name="deliveryType"]:checked');
  const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked');

  let first_name = document.getElementById("firstName");
  let last_name = document.getElementById("lastName");
  let cityInput = document.getElementById("cityInput");
  let cityId = document.getElementById("cityId");
  let lineOne = document.getElementById("lineOne");
  let lineTwo = document.getElementById("lineTwo");
  let postalCode = document.getElementById("postalCode");
  let mobile = document.getElementById("mobile");
  let currentAddressTick = document.getElementById("isCurrentAddress");

  const selectedCity = cityDirectory.find(
    (city) => city.name.toLowerCase() === cityInput.value.trim().toLowerCase(),
  );
  cityId.value = selectedCity ? selectedCity.id : 0;

  let paymentDetails = {
    isCurrentAddress: currentAddressTick.checked,
    deliveryType: deliveryType.value,
    paymentMethod: paymentMethod.value,
    firstName: first_name.value,
    lastName: last_name.value,
    mobile: mobile.value,
    citySelect: cityId.value,
    lineOne: lineOne.value,
    lineTwo: lineTwo.value,
    postalCode: postalCode.value,
  };

  console.log(paymentDetails);

  try {
    const response = await fetch("api/checkout/checkout-process", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify(paymentDetails),
    });
    const data = await response.json();
    if (data.status) {
      console.log(data);

      Notiflix.Notify.success(data.message || "Checkout success", {
        position: "center-top",
      });
      payhere.startPayment(data.paymentDetails);
    } else {
      Notiflix.Notify.failure(data.message || "Checkout failed", {
        position: "center-top",
      });
      console.error(data);
    }
  } catch (error) {
    Notiflix.Notify.failure("Something went wrong. Please try again.", {
      position: "center-top",
    });
    console.error(error);
  }
}
