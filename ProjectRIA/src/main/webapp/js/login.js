/**
 * Login management
 */
(function() {
    // Collect references to ui elements in the local scope
    let login_button = document.getElementById("login_button");
    let register_button = document.getElementById("register_button");
    let login_warning_div = document.getElementById("login_warning_msg");
    let register_warning_div = document.getElementById("register_warning_msg");

	// Get a form response callback for given error div
	var callback = function(error_div) {
		return function(request) {
			if (request.readyState == XMLHttpRequest.DONE) {
				switch(request.status){
	                case 200: //Okay, parse and save data to local storage and go to home
	                    let user = JSON.parse(request.responseText);
	                    localStorage.setItem("id", user.id);
	                    localStorage.setItem("name", user.name);
	                    window.location.href = "home.html"; 
	                    break;
	                case 400: // bad request  (fallthrough)
	                case 401: // unauthorized       |
	                case 500: // server error       v
	                    error_div.textContent = request.responseText;
	                    error_div.style.display = "block"; // Show error div
	                    break;
	                default: //Error
	                    error_div.textContent = "Request reported status " + request.status;
	                    error_div.style.display = "block"; // Show error div
	    		}
			}
		};
	}
	
	// Attach submit listeners to the forms
    attachForm(login_button, login_warning_div, "Login", callback(login_warning_div));
    attachForm(register_button, register_warning_div, "Register", callback(register_warning_div));
})();
