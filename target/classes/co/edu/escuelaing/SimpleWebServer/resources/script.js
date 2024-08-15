function loadGetMsg() {
    let nameVar = document.getElementById("name").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        document.getElementById("getrespmsg").innerHTML = this.responseText;
    }
    xhttp.open("GET", "/api/greeting?name=" + nameVar, true);
    xhttp.send();
}

function loadPostMsg() {
    let nameVar = document.getElementById("postname").value;
    const xhttp = new XMLHttpRequest();
    xhttp.onload = function() {
        document.getElementById("postrespmsg").innerHTML = this.responseText;
    }
    xhttp.open("POST", "/api/greeting", true);
    xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhttp.send("name=" + nameVar);
}
