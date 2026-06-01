// Working AF
let cityDirectory = [];

window.addEventListener("load", async () => {
  try {
    await getCities();
    await loadUserData();
    await checkAdminStatus();
  } catch (e) {
    console.log(e);
  }
});
document.addEventListener("DOMContentLoaded", () => {
  const menuLinks = document.querySelectorAll(".menu-link");
  const sections = document.querySelectorAll(".content-section");

  menuLinks.forEach((link) => {
    link.addEventListener("click", function (e) {
      e.preventDefault();

      const targetId = this.getAttribute("data-target");
      const targetSection = document.getElementById(targetId);

      if (!targetSection) return;

      sections.forEach((section) => {
        section.classList.add("d-none");
      });

      targetSection.classList.remove("d-none");

      menuLinks.forEach((nav) => nav.classList.remove("active-nav"));
      this.classList.add("active-nav");
    });
  });
});
document
  .getElementById("address-anchor")
  .addEventListener("click", async () => {
    await loadAddress();
  });
document
  .getElementById("account-anchor")
  .addEventListener("click", async () => {
    await loadUserData();
  });
// User and Addresses section

async function getCities() {
  try {
    const response = await fetch("api/data/cities");
    if (response.ok) {
      const data = await response.json();
      cityDirectory = data.cities || [];

      const cityInput = document.getElementById("cityInput");
      const cityList = document.getElementById("cityList");
      const shopCitySelect = document.getElementById("shopCitySelect");
      const shopCitySelect2 = document.getElementById("shopCitySelect2");

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
        const option = document.createElement("option");
        option.value = city.id;
        option.innerHTML = city.name;

        if (cityList) {
          const suggestion = document.createElement("option");
          suggestion.value = city.name;
          cityList.appendChild(suggestion);
        }

        // Append CLONES to the others
        // .cloneNode(true) creates a deep copy of the element
        if (shopCitySelect) shopCitySelect.appendChild(option.cloneNode(true));
        if (shopCitySelect2)
          shopCitySelect2.appendChild(option.cloneNode(true));
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

function togglePassword(inputId, icon) {
  const input = document.getElementById(inputId);
  const isPassword = input.getAttribute("type") === "password";

  // 1. Toggle the input type (password <-> text)
  input.setAttribute("type", isPassword ? "text" : "password");

  // 2. Toggle the icon classes
  // Removes 'bi-eye-fill' and adds 'bi-eye' (or vice versa)
  icon.classList.toggle("bi-eye-slash");
  icon.classList.toggle("bi-eye");
}

async function loadAddress() {
  Notiflix.Loading.standard("Loading addresses...");
  try {
    const response = await fetch("api/profiles/addresses");
    if (response.ok) {
      const data = await response.json();
      document.getElementById("addName").innerHTML = `${data.name}`;
      renderAddresses(data.addresses);
    }
  } catch (e) {
    Notiflix.Notify.failure(e.message, {
      position: "center-top",
    });
    console.log(e);
  } finally {
    Notiflix.Loading.remove();
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
      (addr.isPrimary ? "border border-primary-subtle" : "");
    inputField.setAttribute(
      "checked",
      addr.isPrimary ? "checked" : "un-checked",
    );
    card.style.backgroundColor = addr.isPrimary ? "#f8fbff" : "";
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
                            <div>
                                <button class="btn" type="button" onclick="deleteAddress(${addr.id});" style="background-color: #fd6060; color: white"><i
                                        class="bi bi-trash-fill"></i></button>
                            </div>
                        </div>
                    </div>
         `;

    list.appendChild(card);
  });
}

async function loadUserData() {
  try {
    const response = await fetch("api/profiles/user-profile");

    if (response.ok) {
      if (response.redirected) {
        window.location.href = response.url;
        return;
      }
      const data = await response.json();

      document.getElementById("username").innerHTML =
        `Hello, ${data.user.firstName} ${data.user.lastName}`;

      let replacedText = String(data.user.sinceAt).replace("-", " ");

      let since = replacedText.split(" ");
      document.getElementById("since").innerHTML =
        `Aesthetica Member Since ${since[1]} ${since[0]}`;
      document.getElementById("firstName").value = data.user.firstName;
      document.getElementById("lastName").value = data.user.lastName;
      document.getElementById("lineOne").value = data.user.lineOne
        ? data.user.lineOne
        : "";
      document.getElementById("lineTwo").value = data.user.lineTwo
        ? data.user.lineTwo
        : "";
      document.getElementById("postalCode").value = data.user.postalCode
        ? data.user.postalCode
        : "";
      const matchedCity = cityDirectory.find(
        (city) => city.id === data.user.cityId,
      );
      document.getElementById("cityInput").value = matchedCity
        ? matchedCity.name
        : "";
      document.getElementById("cityId").value = data.user.cityId
        ? data.user.cityId
        : 0;
      document.getElementById("mobile").value = data.user.mobile;
      document.getElementById("currentPassword").value = data.user.password;
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

async function checkAdminStatus() {
  try {
    const resp = await fetch("api/auth/admin-status");
    if (resp.ok) {
      const data = await resp.json();
      const btn = document.getElementById("adminPanelBtn");
      const divider = document.getElementById("adminDivider");
      if (btn) {
        btn.classList.toggle("d-none", !data.isSeller);
      }
      if (divider) {
        divider.classList.toggle("d-none", !data.isSeller);
      }
    }
  } catch (e) {
    console.log("Admin status check failed", e);
  }
}

async function saveChanges() {
  Notiflix.Loading.standard("Saving Changes...", {
    clickToClose: false,
    svgColor: "#0284c7",
  });

  let firstName = document.getElementById("firstName");
  let lastName = document.getElementById("lastName");
  let lineOne = document.getElementById("lineOne");
  let lineTwo = document.getElementById("lineTwo");
  let postalCode = document.getElementById("postalCode");
  let cityInput = document.getElementById("cityInput");
  let cityId = document.getElementById("cityId");
  let mobile = document.getElementById("mobile");

  const selectedCity = cityDirectory.find(
    (city) => city.name.toLowerCase() === cityInput.value.trim().toLowerCase(),
  );
  cityId.value = selectedCity ? selectedCity.id : 0;

  const userObj = {
    firstName: firstName.value,
    lastName: lastName.value,
    lineOne: lineOne.value,
    lineTwo: lineTwo.value,
    postalCode: postalCode.value,
    cityId: parseInt(cityId.value),
    mobile: mobile.value,
  };
  try {
    const response = await fetch("api/profiles/update-profile", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userObj),
    });

    if (response.ok) {
      const data = await response.json();
      if (data.status) {
        Notiflix.Report.success(
          "Aesthetica",
          data.message,
          "Okay", //button title
          () => {
            window.location.reload();
          },
        );
        console.log("fetch for update profile success");
        await loadUserData();
      } else {
        Notiflix.Notify.failure(data.message, {
          position: "center-top",
        });
      }
    } else {
      Notiflix.Notify.failure("Profile Update Failed!", {
        position: "center-top",
      });
    }
  } catch (e) {
    Notiflix.Notify.failure(e.message, {
      position: "center-top",
    });
  } finally {
    Notiflix.Loading.remove(1000);
  }
}

async function updatePassword() {
  Notiflix.Loading.standard("Saving Changes...", {
    clickToClose: false,
    svgColor: "#0284c7",
  });

  let currentPassword = document.getElementById("currentPassword");
  let newPassword = document.getElementById("newPassword");
  let confirmPassword = document.getElementById("confirmPassword");

  const userObj = {
    password: currentPassword.value,
    newPassword: newPassword.value,
    confirmPassword: confirmPassword.value,
  };

  try {
    const response = await fetch("api/profiles/update-password", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userObj),
    });

    if (response.ok) {
      const data = await response.json();
      if (data.status) {
        Notiflix.Report.success(
          "Aesthetica",
          data.message,
          "Okay", //button title
          () => {
            window.location.reload();
          },
        );
        console.log("fetch for update profile success");
        await loadUserData();
      } else {
        Notiflix.Notify.failure(data.message, {
          position: "center-top",
        });
      }
    } else {
      Notiflix.Notify.failure("Profile Update Failed!", {
        position: "center-top",
      });
    }
  } catch (e) {
    Notiflix.Notify.failure(e.message, {
      position: "center-top",
    });
  } finally {
    Notiflix.Loading.remove(1000);
  }
}

async function deleteAddress(id) {
  Notiflix.Confirm.show(
    "Delete Address",
    "Are you sure you want to delete this address? This action cannot be undone.",
    "Yes",
    "No",
    async () => {
      // This runs if the user clicks "Yes"
      try {
        const response = await fetch(`api/profiles/delete-address/${id}`, {
          method: "DELETE",
        });

        if (response.ok) {
          Notiflix.Notify.success("Address deleted successfully");
          loadAddress();
        }
      } catch (error) {
        console.error("Failed to delete address:", error);
        Notiflix.Report.failure(
          "Delete Failed",
          "There was an error deleting the address. Please try again.",
          "Okay",
        );
      }
    },
    () => {
      // This runs if the user clicks "No"
      console.log("Deletion cancelled");
    },
    {
      // Optional styling to make the "Yes" button look like a danger button
      okButtonBackground: "#ff5555",
      titleColor: "#1e1e1e",
    },
  );
}

async function switchPrimary(id) {
  try {
    const response = await fetch(`api/profiles/set-primary-address/${id}`, {
      method: "PATCH",
    });
    if (response.ok) {
      loadAddress();
    }
  } catch (error) {}
}


