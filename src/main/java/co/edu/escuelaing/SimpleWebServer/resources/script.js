function loadGetMsg() {
    const nameVar = document.getElementById("name").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        const contentType = this.getResponseHeader("Content-Type");
        
        // Si el contenido es una imagen
        if (contentType.startsWith("image/")) {
            const imgElement = document.createElement("img");
            imgElement.src = "/webroot/" + nameVar;
            imgElement.alt = "Image";
            imgElement.style.maxWidth = "100%"; 
            document.getElementById("getrespmsg").innerHTML = "";
            document.getElementById("getrespmsg").appendChild(imgElement);
        } 
        // Si el contenido es JSON
        else if (contentType === "application/json") {
            const jsonResponse = JSON.parse(this.responseText);
            const tableElement = document.createElement("table");
            tableElement.className = "table table-striped";
            tableElement.innerHTML = jsonToHtmlTable(jsonResponse);
            document.getElementById("getrespmsg").innerHTML = "";
            document.getElementById("getrespmsg").appendChild(tableElement);
        } 
        // Para otro tipo de contenido
        else {
            document.getElementById("getrespmsg").innerHTML = this.responseText;
        }
    }
    xhttp.open("GET", "/webroot/" + nameVar, true);
    xhttp.send();
}

function jsonToHtmlTable(json) {
    let html = `<table class="table table-bordered table-hover table-responsive">
                    <thead class="table-dark">
                        <tr>
                            <th scope="col">Key</th>
                            <th scope="col">Value</th>
                        </tr>
                    </thead>
                    <tbody>`;

    function processJson(value) {
        if (Array.isArray(value)) {
            // Manejar arrays
            return `<table class="table table-bordered table-hover table-responsive">
                        <thead class="table-dark">
                            <tr>
                                <th scope="col">Index</th>
                                <th scope="col">Value</th>
                            </tr>
                        </thead>
                        <tbody>` +
                        value.map((item, index) => {
                            return `<tr>
                                        <td>${index}</td>
                                        <td>${processJson(item)}</td>
                                    </tr>`;
                        }).join('') +
                    `</tbody></table>`;
        } else if (typeof value === "object" && value !== null) {
            // Manejar objetos
            return `<table class="table table-bordered table-hover table-responsive">
                        <thead class="table-dark">
                            <tr>
                                <th scope="col">Key</th>
                                <th scope="col">Value</th>
                            </tr>
                        </thead>
                        <tbody>` +
                        Object.keys(value).map(key => {
                            return `<tr>
                                        <td><strong>${key}</strong></td>
                                        <td>${processJson(value[key])}</td>
                                    </tr>`;
                        }).join('') +
                    `</tbody></table>`;
        } else {
            // Manejar valores primitivos
            return value;
        }
    }

    html += processJson(json);
    html += `</tbody></table>`;
    return html;
}

function loadPostMsg() {
    const postnameVar = document.getElementById("postname").value;
    const url = "/webroot";
    const params = "name=" + postnameVar;

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: params
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById("postrespmsg").innerHTML = data;
    });
}

function previewImage(event, querySelector) {
    const input = event.target;
    $imgPreview = document.querySelector(querySelector);
    if(!input.files.length) return;
    
    const file = input.files[0];
    const objectURL = URL.createObjectURL(file);
    $imgPreview.src = objectURL;                
}
