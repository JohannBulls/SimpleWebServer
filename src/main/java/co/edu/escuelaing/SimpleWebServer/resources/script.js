function loadGetMsg() {
    const nameVar = document.getElementById("name").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        const contentType = this.getResponseHeader("Content-Type");
        
        // Si el contenido es una imagen
        if (contentType.startsWith("image/")) {
            const imgElement = document.createElement("img");
            imgElement.src = nameVar;
            imgElement.alt = "Image";
            imgElement.style.maxWidth = "100%"; 
            document.getElementById("getrespmsg").innerHTML = "";
            document.getElementById("getrespmsg").appendChild(imgElement);
        } 
        else {
            document.getElementById("getrespmsg").innerHTML = this.responseText;
        }
    }
    xhttp.open("GET", nameVar, true);
    xhttp.send();
}

function loadPostMsg(name) {
    let url =  name.value;
    fetch(url, { method: 'POST' })
        .then(response => response.text())
        .then(data => document.getElementById("postrespmsg").innerHTML = data);
}

function previewImage(event, querySelector) {
    const input = event.target;
    const imgPreview = document.querySelector(querySelector);
    if (!input.files.length) return;
    const file = input.files[0];
    const objectURL = URL.createObjectURL(file);
    imgPreview.src = objectURL;
}