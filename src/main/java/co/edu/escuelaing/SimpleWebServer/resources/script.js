function loadGetMsg() {
    const nameVar = document.getElementById("name").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        document.getElementById("getrespmsg").innerHTML = this.responseText;
    }
    xhttp.open("GET", "/webroot/" + nameVar, true);
    xhttp.send();
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