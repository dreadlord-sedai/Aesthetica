window.API_ROOT = window.API_ROOT || '/aesthetica';
let params = new URLSearchParams(window.location.search);
window.addEventListener("load",async ()=>{
    const buyNowButton = document.getElementById("buyNowButton");
    if (buyNowButton) {
        buyNowButton.addEventListener("click", async () => {
            const productId = params.get("id");
            if (!productId) {
                Notiflix.Notify.failure("Invalid product", { position: "center-top" });
                return;
            }
            const qtyInput = document.getElementById("qtyInput");
            const parsedQty = qtyInput ? Number.parseInt(qtyInput.value, 10) : 1;
            const qty = Number.isFinite(parsedQty) && parsedQty > 0 ? parsedQty : 1;

            // Primary flow uses cart.js helper; fallback directly calls API if helper is unavailable.
            if (typeof buyNow === "function") {
                await buyNow(productId, qty);
                return;
            }

            const response = await fetch(
                API_ROOT + `/api/user-carts/cart?productId=${encodeURIComponent(productId)}&qty=${encodeURIComponent(qty)}`,
            );
            const data = response.ok ? await response.json() : { status: false, message: "Unable to add to cart" };
            if (data.status) {
                window.location = `checkout.html?productId=${productId}&qty=${qty}`;
            } else {
                Notiflix.Notify.failure(data.message || "Unable to proceed with Buy Now", {
                    position: "center-top",
                });
            }
        });
    }

    await loadSingleProduct(params.get("id"));
});

let imageIconContainer = document.getElementById("image-icon-container");

async function loadSingleProduct(productID){
    const response = await fetch(`api/product/single-product?id=${productID}`, {method:"GET"});
    if (response.ok) {
        const data = await response.json();
        console.log(data);
        document.getElementById("productTitle").innerHTML = `${data.title}`;
        document.getElementById("productPrice").innerHTML = `${data.stockList[0].price} Rs/-`;
        document.getElementById("productDescription").innerHTML = `${data.description}`;
        if (data.stockList[0].quantity <= 0){
            document.getElementById("qty").innerHTML= ``;
            document.getElementById("productPrice").style = "color: #a5a5a5";
            document.getElementById("buyNowButton").classList.add("d-none");
            document.getElementById("addToCart").classList.add("d-none");
            document.getElementById("availabilityBadge").style = "background-color: #FF001AFF";
        }
        document.getElementById("qty").innerHTML= `${data.stockList[0].quantity}`;

        const thumbnailContainer = document.getElementById("thumbnail-container");
        const mainImage = document.getElementById("main-image");

        thumbnailContainer.innerHTML = "";
        if (data.images && data.images.length > 0) {
            data.images.forEach((imgUrl, index) => {
                const imgDiv = document.createElement("div");
                imgDiv.classList.add("img-icon", "bg-secondary-subtle", "rounded-3", "m-2", "border");
                imgDiv.style.backgroundImage = `url('${imgUrl}')`;
                imgDiv.onclick = function() {
                    mainImage.style.backgroundImage = `url('${imgUrl}')`;
                };
                thumbnailContainer.appendChild(imgDiv);
                if (index === 0) {
                    mainImage.style.backgroundImage = `url('${imgUrl}')`;
                }
            });
        }else {
            mainImage.innerHTML = "No Image";
        }



    } else {
        console.log("Error:", response.status);
    }
}

function toggleDescription() {
    const container = document.getElementById('desc-container');
    const fadeOverlay = document.getElementById('desc-fade-overlay');
    const btn = document.getElementById('toggleDescBtn');

    // The height you want when collapsed (must match CSS)
    const collapsedHeight = "200px";

    if (container.style.maxHeight === collapsedHeight || container.style.maxHeight === "") {
        // EXPAND: Set max-height to the scrollHeight (actual height of content)
        container.style.maxHeight = container.scrollHeight + "px";
        fadeOverlay.classList.add('fade-hidden');
        btn.innerText = "Show Less";
    } else {
        // COLLAPSE: Set max-height back to fixed pixel amount
        container.style.maxHeight = collapsedHeight;
        fadeOverlay.classList.remove('fade-hidden');
        btn.innerText = "Read More";
    }
}

function changeQty(amount) {
    const qtyInput = document.getElementById("qtyInput");
    let currentQty = parseInt(qtyInput.value);

    let newQty = currentQty + amount;

    // Minimum quantity check
    if (newQty >= 1) {
        qtyInput.value = newQty;
    }
}